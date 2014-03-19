/**
 * Copyright 2014 John Lawson
 * 
 * RunOnSubmatricesTask.java is part of JCluster. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * @param <T> Type of QuiverMatrix handled by tasks
 * 
 */
public abstract class RunMultipleTask<T extends QuiverMatrix> implements MatrixTask<T> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private CompletionResultQueue<MatrixInfo> mQueue;
	private MatrixInfoResultHandler mResultHandler;
	private VariableCompletionHandler<MatrixInfo> mHandler;
	private ExecutorService mExecutor;
	private ExecutorCompletionService<MatrixInfo> mService;
	private Collection<MatrixTaskFactory<T>> mFactorys;
	private boolean mShouldStop = false;

	public final void setExecutor(ExecutorService executor) {
		this.mExecutor = executor;
	}

	public final void setHandler(VariableCompletionHandler<MatrixInfo> handler) {
		this.mHandler = handler;
	}

	public final void setQueue(CompletionResultQueue<MatrixInfo> queue) {
		this.mQueue = queue;
	}

	public final void setResultHandler(MatrixInfoResultHandler resultHandler) {
		this.mResultHandler = resultHandler;
		mResultHandler.setTask(this);
		mResultHandler.setQueue(mQueue);
	}

	public final void addTaskFactory(MatrixTaskFactory<T> factory) {
		if (mFactorys == null) {
			mFactorys = new ArrayList<MatrixTaskFactory<T>>();
		}
		mFactorys.add(factory);
	}

	@Override
	public final MatrixInfo call() throws Exception {
		mService = new ExecutorCompletionService<MatrixInfo>(mExecutor);

		mHandler.setService(mService);
		mHandler.setQueue(mQueue);
		Thread handlerThread = new Thread(mHandler);
		handlerThread.start();
		log.debug("Handler thread started");

		ExecutorService resultThread = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> resultFuture = resultThread.submit(mResultHandler);
		log.debug("Result thread started");

		submitTasks();
		log.debug("Tasks submitted");
		try {
			handlerThread.join();
		} catch (InterruptedException e) {
			log.error("Interrupted in thread {}", Thread.currentThread().getName());
		}
		mResultHandler.allResultsQueued();

		log.debug("All results queued for handling. Waiting for result.");

		if (Thread.currentThread().isInterrupted()) {
			log.debug("The current thread {} is interrupted?! Clearing: {} {}", Thread.currentThread()
					.getName(), Thread.interrupted(), Thread.interrupted());
		}
		MatrixInfo result = null;
		try {
			result = resultFuture.get();
		} catch (InterruptedException e) {
			log.debug("Caught interrupt in thread {}. Info: {}", Thread.currentThread().getName(), result);
		}
		return result;
	}

	@Override
	public final void requestStop() {
		mHandler.setWaitIfEmpty(false);
		mShouldStop = true;
		mExecutor.shutdown();
	}

	@Override
	public final void reset() {
		mExecutor.shutdownNow();
	}

	/**
	 * Submit tasks to be run on the initial matrix.
	 */
	private final void submitTasks() {
		mHandler.setWaitIfEmpty(true);
		submitAllTasks();
		mHandler.setWaitIfEmpty(false);
	}

	/**
	 * Submit all tasks that should be run on the initial matrix.
	 * 
	 * <p>
	 * Before each task is submitted {@link RunMultipleTask#shouldSubmitTask()} should be checked to
	 * ensure that the task has not been requested to stop.
	 * 
	 * <p>
	 * Tasks are submitted using the {@link RunMultipleTask#submitTaskFor(QuiverMatrix)} method, which
	 * passes the matrix to each {@link MatrixTaskFactory} provided through the
	 * {@link RunMultipleTask#addTaskFactory(MatrixTaskFactory)} method.
	 */
	protected abstract void submitAllTasks();

	protected final void submitTaskFor(T matrix) {
		for (MatrixTaskFactory<T> fac : mFactorys) {
			MatrixTask<T> task = fac.getTask(matrix);
			submitTask(task);
		}
	}

	/**
	 * Submit the provided task to the executor service and tell the completion handler.
	 * 
	 * @param task Task to execute
	 */
	private final void submitTask(MatrixTask<T> task) {
		mService.submit(task);
		mHandler.taskAdded();
	}

	/**
	 * Check whether further tasks should be submitted to be executed.
	 * 
	 * @return true if more tasks can be submitted
	 */
	protected final boolean shouldSubmitTask() {
		return !mShouldStop;
	}

	protected RunMultipleTask(Builder<T, ?> builder) {
		this.mQueue = builder.mQueue;
		this.mResultHandler = builder.mResultHandler;
		this.mHandler = builder.mHandler;
		this.mExecutor = builder.mExecutor;
		this.mFactorys = builder.mFactorys;
	}

	public static abstract class Builder<T extends QuiverMatrix, A extends Builder<T, A>> {

		private static final int DEF_NUM_THREAD = 2;
		private static final long DEF_KEEP_ALIVE_SEC = 0;
		private static final int DEF_QUEUE_SIZE = 10;

		private CompletionResultQueue<MatrixInfo> mQueue;
		private MatrixInfoResultHandler mResultHandler;
		private VariableCompletionHandler<MatrixInfo> mHandler;
		private ExecutorService mExecutor;
		private final Collection<MatrixTaskFactory<T>> mFactorys =
				new ArrayList<MatrixTaskFactory<T>>();

		protected abstract A self();


		public A withQueue(CompletionResultQueue<MatrixInfo> mQueue) {
			this.mQueue = mQueue;
			return self();
		}

		public A withResultHandler(MatrixInfoResultHandler mResultHandler) {
			this.mResultHandler = mResultHandler;
			return self();
		}

		public A withHandler(VariableCompletionHandler<MatrixInfo> mHandler) {
			this.mHandler = mHandler;
			return self();
		}

		public A withExecutor(ExecutorService mExecutor) {
			this.mExecutor = mExecutor;
			return self();
		}

		public A addFactory(MatrixTaskFactory<T> factory) {
			this.mFactorys.add(factory);
			return self();
		}


		protected Builder<T, A> validate() {
			if (mQueue == null) {
				mQueue = new ArrayCompletionResultQueue<MatrixInfo>();
			}

			if (mHandler == null) {
				mHandler = new VariableCompletionHandler<MatrixInfo>();
			}

			if (mExecutor == null) {
				mExecutor =
						new ThreadPoolExecutor(DEF_NUM_THREAD, DEF_NUM_THREAD, DEF_KEEP_ALIVE_SEC,
								TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(DEF_QUEUE_SIZE),
								new ThreadPoolExecutor.CallerRunsPolicy());
			}
			mHandler.setQueue(mQueue);
			return self();
		}

		public static <T extends QuiverMatrix> Builder<T, ?> builder() {
			return new Builder2<T>();
		}

		private static class Builder2<T extends QuiverMatrix> extends Builder<T, Builder2<T>> {

			@Override
			protected Builder2<T> self() {
				return this;
			}

		}
	}


}
