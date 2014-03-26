/**
 * Copyright 2014 John Lawson
 * 
 * RunMutationClass.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.MutClassSizeTask.NewMatrixSeenListener;
import uk.co.jwlawson.jcluster.data.EquivQuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;
import uk.co.jwlawson.jcluster.pool.Pools;

import com.google.common.base.Preconditions;

/**
 * @author John Lawson
 * 
 */
public class RunMutationClass extends RunMultipleTask<EquivQuiverMatrix> implements
		NewMatrixSeenListener<EquivQuiverMatrix> {

	private final ThreadFactory factory = new NamingThreadFactory(getClass().getSimpleName()
			+ "-mutator");

	private final Logger log = LoggerFactory.getLogger(getClass());

	private int count = 0;

	public static RunMutationClass getInstance(EquivQuiverMatrix matrix) {
		return Builder.builder().withInitial(matrix).build();
	}

	private EquivQuiverMatrix mMatrix;
	private final EquivMutClassSizeTask mTask;
	private final Pool<EquivQuiverMatrix> mPool;
	private Thread mSubmittingThread;
	private final BlockingQueue<EquivQuiverMatrix> mQueue;
	private final AtomicBoolean mRunning = new AtomicBoolean(true);
	private final AtomicBoolean mWaiting = new AtomicBoolean(false);
	private final AtomicInteger mQueueStall = new AtomicInteger(0);

	protected RunMutationClass(Builder<?> builder) {
		super(builder);
		mMatrix = builder.mInitial;
		mPool = builder.mPool;
		mQueue = new ArrayBlockingQueue<EquivQuiverMatrix>(100);

		mTask = new EquivMutClassSizeTask(mMatrix);
		mTask.addNewMatrixListener(this);
	}

	@Override
	public void setMatrix(EquivQuiverMatrix matrix) {
		mMatrix = matrix;
		mTask.setMatrix(new EquivQuiverMatrix(matrix));
	}

	@Override
	protected void submitAllTasks() {
		mSubmittingThread = Thread.currentThread();

		ExecutorService calcThread = Executors.newSingleThreadExecutor(factory);
		calcThread.submit(mTask);

		while (!mQueue.isEmpty() || mRunning.get()) {
			try {
				mWaiting.set(true);
				EquivQuiverMatrix mat = mQueue.poll(1, TimeUnit.SECONDS);
				mWaiting.set(false);
				if (mat == null) {
					if (count++ > 10) {
						// Don't want to be stuck in a loop forever
						log.error("Pooling fell through 10 times. Giving up.");
						break;
					}
					log.warn("Polling for matrix fell through. Queue empty: {} Running: {}",
							mQueue.isEmpty(), mRunning.get());
					continue;
				}
				count = 0;
				submitTaskFor(mat);
			} catch (InterruptedException e) {
				// No more matrices to wait for
				log.error("Caught interrupt in thread {}. Queue empty: {}", Thread.currentThread()
						.getName(), mQueue.isEmpty());
			}
		}
		calcThread.shutdownNow();
	}

	@Override
	public void newMatrixSeen(EquivQuiverMatrix matrix) {

		EquivQuiverMatrix m = mPool.getObj();
		m.set(matrix);
		if (!mRunning.get()) {
			log.error("Trying to submit a new matrix after all have been seen.");
		}
		queueMatrix(m);
		mQueueStall.set(0);
	}

	/**
	 * Runs on the mutation class finding thread. Blocks until the matrix can be queued, therefore no
	 * further matrices will be computed until this can be queued.
	 * 
	 * @param m Matrix to queue
	 */
	private void queueMatrix(EquivQuiverMatrix m) {
		try {
			if (!mQueue.offer(m, 5, TimeUnit.SECONDS)) {
				if (mQueueStall.incrementAndGet() > 100) {
					log.error("Tried to insert matrix into queue 100 times and failed. Giving up.");
					return;
				}
				log.warn("Cannot put new matrix into queue, trying again.");
				queueMatrix(m);
			}
		} catch (InterruptedException e) {
			log.debug("Caught interrupt in thread {}", Thread.currentThread().getName());
		}
	}

	@Override
	public void allMatricesSeen() {
		mRunning.set(false);
		if (mWaiting.get()) {
			log.error("Interrupting thread {} from thread {}", mSubmittingThread.getName(), Thread
					.currentThread().getName());
			mSubmittingThread.interrupt();
		}
	}


	public abstract static class Builder<A extends Builder<A>> extends
			RunMultipleTask.Builder<EquivQuiverMatrix, A> {

		protected EquivQuiverMatrix mInitial;
		private Pool<EquivQuiverMatrix> mPool;

		@Override
		protected abstract A self();

		public A withInitial(EquivQuiverMatrix initial) {
			this.mInitial = initial;
			return self();
		}

		public A withPool(Pool<EquivQuiverMatrix> pool) {
			this.mPool = pool;
			return self();
		}

		@Override
		protected Builder<A> validate() {
			super.validate();
			Preconditions.checkNotNull(mInitial, "mInitial may not be null");

			if (mPool == null) {
				Pool<EquivQuiverMatrix> pool =
						Pools.getQuiverMatrixPool(mInitial.getNumRows() - 1, mInitial.getNumCols() - 1,
								EquivQuiverMatrix.class);
				mPool = pool;
			}
			return this;
		}

		public RunMutationClass build() {
			validate();
			return new RunMutationClass(this);
		}

		/*
		 * Not too sure why this throws a check warning. The implementation is overloading a method
		 * which returns Builder<V,S> but here the V is fixed, so we lose a type.
		 */
		@SuppressWarnings("unchecked")
		public static Builder<?> builder() {
			return new Builder2();
		}

		private static class Builder2 extends Builder<Builder2> {

			@Override
			protected Builder2 self() {
				return this;
			}

		}
	}

}
