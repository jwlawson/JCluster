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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * @author John Lawson
 * @param <T> Type of QuiverMatrix handled by tasks
 * 
 */
public abstract class RunMultipleTask<T extends QuiverMatrix> implements MatrixTask<T> {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private TECSResultHandler mResultHandler;
	private ExecutorService mExecutor;
	private TrackingExecutorCompletionService<MatrixInfo> mService;
	private Collection<MatrixTaskFactory<T>> mFactories;
	private boolean mShouldStop = false;


	public final void setResultHandler(TECSResultHandler resultHandler) {
		this.mResultHandler = resultHandler;
		mResultHandler.setTask(this);
	}

	public final void addTaskFactory(MatrixTaskFactory<T> factory) {
		if (mFactories == null) {
			mFactories = new ArrayList<MatrixTaskFactory<T>>();
		}
		mFactories.add(factory);
	}

	@Override
	public final MatrixInfo call() throws Exception {
		if (mExecutor == null) {
			setExecutor();
		}
		mService = new TrackingExecutorCompletionService<MatrixInfo>(mExecutor);
		mResultHandler.setCompletionService(mService);

		ExecutorService resultThread = Threads.getResultThreadPoolForTask(this);
		Future<MatrixInfo> resultFuture = resultThread.submit(mResultHandler);
		log.debug("Result thread started");

		submitAllTasks();
		log.debug("Tasks submitted");

		mResultHandler.allTasksSubmitted();

		log.debug("All results queued for handling. Waiting for result.");
		return resultFuture.get();
	}

	@Override
	public final void requestStop() {
		mShouldStop = true;
	}

	@Override
	public final void reset() {}

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
		for (MatrixTaskFactory<T> fac : mFactories) {
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
		if (builder.resultHandler != null) {
			setResultHandler(builder.resultHandler);
		}
		this.mFactories = builder.factories;
		if (builder.executor != null) {
			this.mExecutor = builder.executor;
		}
	}

	private void setExecutor() {
		mExecutor = Threads.getThreadPoolForTask(this);
	}

	public static abstract class Builder<T extends QuiverMatrix, A extends Builder<T, A>> {

		private TECSResultHandler resultHandler;
		private ExecutorService executor;
		private final Collection<MatrixTaskFactory<T>> factories =
				new ArrayList<MatrixTaskFactory<T>>();

		protected abstract A self();

		public A withResultHandler(TECSResultHandler handler) {
			this.resultHandler = handler;
			return self();
		}

		public A withExecutor(ExecutorService exec) {
			this.executor = exec;
			return self();
		}

		public A addFactory(MatrixTaskFactory<T> fac) {
			this.factories.add(fac);
			return self();
		}

		protected Builder<T, A> validate() {
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
