/**
 * Copyright 2014 John Lawson
 * 
 * BlockingThreadPool.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class is a specialized extension of the ThreadPoolExecutor class.
 * 
 * The execute method of the ThreadPoolExecutor will block in case the queue is full and only
 * unblock when the queue is dequeued - that is a task that is currently in the queue is removed and
 * handled by the ThreadPoolExecutor.
 * 
 * This subclass of ThreadPoolExecutor also takes away the max threads capabilities of the
 * ThreadPoolExecutor superclass and internally sets the amount of maximum threads to be the size of
 * the core threads. This is done since threads over the core size and under the max are
 * instantiated only once the queue is full, but the NotifyingBlockingThreadPoolExecutor will block
 * once the queue is full.
 * 
 * @see <a href="https://today.java.net/pub/a/today/2008/10/23/creating-a-notifying-blocking
 *      -thread-pool-executor.html
 *      ">https://today.java.net/pub/a/today/2008/10/23/creating-a-notifying-blocking-thread-pool-executor.html
 *      < / a >
 * 
 * @author Yaneeve Shekel & Amir Kirsh
 * @author John Lawson
 */
public class BlockingThreadPool extends ThreadPoolExecutor {

	/**
	 * This constructor is used in order to maintain the first functionality specified above. It does
	 * so by using an ArrayBlockingQueue and the BlockThenRunPolicy that is defined in this class.
	 * This constructor allows to give a timeout for the wait on new task insertion and to react upon
	 * such a timeout if occurs.
	 * 
	 * @param poolSize is the amount of threads that this pool may have alive at any given time
	 * @param queueSize is the size of the queue. This number should be at least as the pool size to
	 *        make sense (otherwise there are unused threads), thus if the number sent is smaller, the
	 *        poolSize is used for the size of the queue. Recommended value is twice the poolSize.
	 * @param keepAliveTime is the amount of time after which an inactive thread is terminated
	 * @param keepAliveTimeUnit is the unit of time to use with the previous parameter
	 * @param maxBlockingTime is the maximum time to wait on the queue of tasks before calling the
	 *        BlockingTimeout callback
	 * @param maxBlockingTimeUnit is the unit of time to use with the previous parameter
	 * @param blockingTimeCallback is the callback method to call when a timeout occurs while blocking
	 *        on getting a new task, the return value of this Callable is Boolean, indicating whether
	 *        to keep blocking (true) or stop (false). In case false is returned from the
	 *        blockingTimeCallback, this executer will throw a RejectedExecutionException
	 */
	public BlockingThreadPool(int poolSize, int queueSize, long keepAliveTime,
			TimeUnit keepAliveTimeUnit, long maxBlockingTime, TimeUnit maxBlockingTimeUnit,
			Callable<Boolean> blockingTimeCallback) {

		super(poolSize, poolSize, keepAliveTime, keepAliveTimeUnit, new ArrayBlockingQueue<Runnable>(
				Math.max(poolSize, queueSize)), new BlockThenRunPolicy(maxBlockingTime,
				maxBlockingTimeUnit, blockingTimeCallback));

		super.allowCoreThreadTimeOut(true);
	}

	/**
	 * Internally calls on super's setCorePoolSize and setMaximumPoolSize methods with the given
	 * method argument.
	 * 
	 * @see java.util.concurrent.ThreadPoolExecutor#setCorePoolSize(int)
	 */
	@Override
	public void setCorePoolSize(int corePoolSize) {
		super.setCorePoolSize(corePoolSize);
		super.setMaximumPoolSize(corePoolSize);
	}

	/**
	 * Does Nothing!
	 * 
	 * @throws UnsupportedOperationException in any event
	 * @see java.util.concurrent.ThreadPoolExecutor#setMaximumPoolSize(int)
	 */
	@Override
	public void setMaximumPoolSize(int maximumPoolSize) {
		throw new UnsupportedOperationException("setMaximumPoolSize is not supported.");
	}

	/**
	 * Does Nothing! MUST NOT CHANGE OUR BUILT IN RejectedExecutionHandler
	 * 
	 * @throws UnsupportedOperationException in any event
	 * @see java.util.concurrent.ThreadPoolExecutor#setRejectedExecutionHandler(RejectedExecutionHandler)
	 */
	@Override
	public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
		throw new UnsupportedOperationException(
				"setRejectedExecutionHandler is not allowed on this class.");
	}

	/**
	 * This Policy class enforces the blocking feature of the NotifyingBlockingThreadPoolExecutor. It
	 * does so by invoking the BlockingQueue's put method (instead of the offer method that is used by
	 * the standard implementation of the ThreadPoolExecutor - see the opened Java 6 source code).
	 */
	private static class BlockThenRunPolicy implements RejectedExecutionHandler {

		private final long maxBlockingTime;
		private final TimeUnit maxBlockingTimeUnit;
		private final Callable<Boolean> blockingTimeCallback;

		public BlockThenRunPolicy(long maxBlockingTime, TimeUnit maxBlockingTimeUnit,
				Callable<Boolean> blockingTimeCallback) {
			this.maxBlockingTime = maxBlockingTime;
			this.maxBlockingTimeUnit = maxBlockingTimeUnit;
			this.blockingTimeCallback = blockingTimeCallback;
		}

		/**
		 * When this method is invoked by the ThreadPoolExecutor's reject method it simply asks for the
		 * Executor's Queue and calls on its put method which will Block (at least for the
		 * ArrayBlockingQueue).
		 * 
		 * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(Runnable,
		 *      ThreadPoolExecutor)
		 */
		@Override
		public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {

			BlockingQueue<Runnable> workQueue = executor.getQueue();
			boolean taskSent = false;

			while (!taskSent) {

				if (executor.isShutdown()) {
					throw new RejectedExecutionException(
							"ThreadPoolExecutor has shutdown while attempting to offer a new task.");
				}

				try {
					// put on the queue and block if no room is available, with a timeout
					// the result of the call to offer says whether the task was accepted or not
					if (workQueue.offer(task, maxBlockingTime, maxBlockingTimeUnit)) {
						taskSent = true;
					} else {
						if (shouldRetry()) {
							continue;
						} else {
							throw new RejectedExecutionException(
									"User decided to stop waiting for task insertion");
						}
					}
				} catch (InterruptedException e) {
					// someone woke us up and we need to go back to the offer/put call...
				}
			}
		}

		private Boolean shouldRetry() {
			Boolean result = null;
			try {
				result = blockingTimeCallback.call();
			} catch (Exception e) {
				throw new RejectedExecutionException(e);
			}
			return result;
		}

	}
}
