/**
 * Copyright 2014 John Lawson
 * 
 * AlSubFiniteCheck.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.co.jwlawson.jcluster.pool.Pool;

import com.google.common.base.Preconditions;

/**
 * @author John Lawson
 * 
 */
public class AllSubFiniteCheck<T extends QuiverMatrix> extends RunMultipleTask<T> {

	private final MatrixTaskFactory<QuiverMatrix> mFactory;
	private T mInitial;
	private final ExecutorService mService;
	private final MatrixInfoResultHandler mResultHandler;
	private final VariableCompletionHandler<MatrixInfo> mHandler;
	private Pool<T> mPool;

	public AllSubFiniteCheck(T matrix) {
		this(new Builder<T>().withInitial(matrix).validate());
	}

	@Override
	public void setMatrix(T matrix) {
		mInitial = matrix;
	}

	@Override
	public void reset() {}

	@Override
	public void requestStop() {
		mService.shutdownNow();
	}

	@Override
	protected void submitTasks(CompletionService<MatrixInfo> exec) {
		mHandler.setWaitIfEmpty(true);
		for (int i = 0; i < mInitial.getNumRows(); i++) {
			for (int j = 0; j < mInitial.getNumCols(); j++) {
				MatrixTask<QuiverMatrix> task =
						mFactory.getTask(mInitial.submatrix(i, j, mPool.getObj()));
				exec.submit(task);
				mHandler.taskAdded();
			}
		}
		mHandler.setWaitIfEmpty(false);
	}

	private AllSubFiniteCheck(Builder<T> builder) {
		mFactory = new FiniteCheckTaskFactory();
		this.mInitial = builder.mInitial;
		this.mService = builder.mService;
		this.mPool = builder.mPool;
		setExecutor(mService);
		setQueue(builder.mQueue);
		this.mResultHandler = builder.mResultHandler;
		setResultHandler(builder.mResultHandler);
		this.mHandler = builder.mHandler;
		setHandler(builder.mHandler);
	}

	public static class Builder<T extends QuiverMatrix> {

		private MatrixInfoResultHandler mResultHandler;
		private static final int DEF_NUM_THREAD = 2;
		private static final long DEF_KEEP_ALIVE_SEC = 0;
		private static final int DEF_QUEUE_SIZE = 10;
		private T mInitial;
		private ExecutorService mService;
		private CompletionResultQueue<MatrixInfo> mQueue;
		private VariableCompletionHandler<MatrixInfo> mHandler;
		private Pool<T> mPool;

		public <S extends QuiverMatrix> Builder<S> withInitial(S initial) {
			@SuppressWarnings("unchecked")
			Builder<S> self = (Builder<S>) this;
			self.mInitial = initial;
			return self;
		}

		public Builder<?> withService(ExecutorService service) {
			this.mService = service;
			return this;
		}

		public Builder<?> withQueue(CompletionResultQueue<MatrixInfo> queue) {
			this.mQueue = queue;
			return this;
		}

		public Builder<?> withHandler(VariableCompletionHandler<MatrixInfo> handler) {
			this.mHandler = handler;
			return this;
		}

		public Builder<?> withResultHandler(MatrixInfoResultHandler resultHandler) {
			this.mResultHandler = resultHandler;
			return this;
		}

		public <S extends QuiverMatrix> Builder<S> withPool(Pool<S> pool) {
			@SuppressWarnings("unchecked")
			Builder<S> self = (Builder<S>) this;
			self.mPool = pool;
			return self;
		}

		public AllSubFiniteCheck<T> build() {
			validate();
			return new AllSubFiniteCheck<T>(this);
		}

		private Builder<T> validate() {
			Preconditions.checkNotNull(mInitial, "mInitial may not be null");

			if (mQueue == null) {
				mQueue = new ArrayCompletionResultQueue<MatrixInfo>();
			}

			if (mHandler == null) {
				mHandler = new VariableCompletionHandler<MatrixInfo>();
			}

			if (mService == null) {
				mService =
						new ThreadPoolExecutor(DEF_NUM_THREAD, DEF_NUM_THREAD, DEF_KEEP_ALIVE_SEC,
								TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(DEF_QUEUE_SIZE),
								new ThreadPoolExecutor.CallerRunsPolicy());
			}
			if (mPool == null) {
				@SuppressWarnings("unchecked")
				Pool<T> pool =
						(Pool<T>) Pools.getQuiverMatrixPool(mInitial.getNumRows() - 1,
								mInitial.getNumCols() - 1, mInitial.getClass());
				mPool = pool;
			}
			if (mResultHandler == null) {
				mResultHandler = new AllFiniteResultHandler(new MatrixInfo(mInitial));
			}
			mHandler.setQueue(mQueue);
			mResultHandler.setQueue(mQueue);
			return this;
		}
	}


}
