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

import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
	private CompletionHandler<MatrixInfo> mHandler;
	private Executor mExecutor;

	public void setExecutor(Executor executor) {
		this.mExecutor = executor;
	}

	public void setHandler(CompletionHandler<MatrixInfo> handler) {
		this.mHandler = handler;
	}

	public void setQueue(CompletionResultQueue<MatrixInfo> queue) {
		this.mQueue = queue;
	}

	public void setResultHandler(MatrixInfoResultHandler resultHandler) {
		this.mResultHandler = resultHandler;
		mResultHandler.setTask(this);
	}

	@Override
	public MatrixInfo call() throws Exception {
		CompletionService<MatrixInfo> exec = new ExecutorCompletionService<MatrixInfo>(mExecutor);

		mHandler.setService(exec);
		mHandler.setQueue(mQueue);
		Thread handlerThread = new Thread(mHandler);
		handlerThread.start();
		log.debug("Handler thread started");

		ExecutorService resultThread = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> resultFuture = resultThread.submit(mResultHandler);
		log.debug("Result thread started");

		submitTasks(exec);
		log.debug("Tasks submitted");

		handlerThread.join();
		mResultHandler.allResultsQueued();
		log.debug("Thread interrupting");
		resultThread.shutdownNow();
		log.debug("All results queued for handling. Waiting for result.");

		return resultFuture.get();
	}

	protected abstract void submitTasks(CompletionService<MatrixInfo> exec);


}
