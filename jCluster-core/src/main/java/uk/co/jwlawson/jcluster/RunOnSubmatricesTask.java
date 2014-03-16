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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author John Lawson
 * 
 */
public class RunOnSubmatricesTask implements Callable<MatrixInfo> {

	private final MatrixTaskFactory mFactory;
	private final QuiverMatrix mMatrix;
	private final CompletionResultQueue<MatrixInfo> mQueue;
	private final MatrixInfoResultHandler mResultHandler;
	private final CompletionHandler<MatrixInfo> mHandler;
	private final Executor mExecutor;

	public MatrixInfo call() throws Exception {
		CompletionService<MatrixInfo> exec = new ExecutorCompletionService<MatrixInfo>(mExecutor);

		mHandler.setService(exec);
		mHandler.setQueue(mQueue);
		Thread handlerThread = new Thread(mHandler);
		handlerThread.start();

		ExecutorService resultThread = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> resultFuture = resultThread.submit(mResultHandler);

		submitTasks(exec);

		handlerThread.join();
		mResultHandler.allResultsQueued();

		return resultFuture.get();
	}

	protected void submitTasks(CompletionService<MatrixInfo> exec) {

		for (int i = 0; i < mMatrix.getNumRows(); i++) {
			for (int j = 0; j < mMatrix.getNumCols(); j++) {
				MatrixTask task = mFactory.getTask(mMatrix.submatrix(i, j));
				exec.submit(task);
			}
		}
	}

	public static class Builder {
		private MatrixTaskFactory mFactory;
		private QuiverMatrix mMatrix;
		private CompletionResultQueue<MatrixInfo> mQueue;
		private MatrixInfoResultHandler mResultHandler;
		private CompletionHandler<MatrixInfo> mHandler;
		private Executor mExecutor;

		public Builder withFactory(MatrixTaskFactory mFactory) {
			this.mFactory = mFactory;
			return this;
		}

		public Builder withMatrix(QuiverMatrix mMatrix) {
			this.mMatrix = mMatrix;
			return this;
		}

		public Builder withQueue(CompletionResultQueue<MatrixInfo> mQueue) {
			this.mQueue = mQueue;
			return this;
		}

		public Builder withResultHandler(MatrixInfoResultHandler mResultHandler) {
			this.mResultHandler = mResultHandler;
			return this;
		}

		public Builder withHandler(CompletionHandler<MatrixInfo> mHandler) {
			this.mHandler = mHandler;
			return this;
		}

		public Builder withExecutor(Executor mExecutor) {
			this.mExecutor = mExecutor;
			return this;
		}

		public RunOnSubmatricesTask build() {
			return new RunOnSubmatricesTask(this);
		}
	}

	private RunOnSubmatricesTask(Builder builder) {
		this.mFactory = builder.mFactory;
		this.mMatrix = builder.mMatrix;
		this.mQueue = builder.mQueue;
		this.mResultHandler = builder.mResultHandler;
		this.mHandler = builder.mHandler;
		this.mExecutor = builder.mExecutor;
	}
}