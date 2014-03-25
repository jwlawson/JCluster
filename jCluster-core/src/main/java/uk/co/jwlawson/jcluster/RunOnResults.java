/**
 * Copyright 2014 John Lawson
 * 
 * RunOnResults.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

import com.google.common.base.Preconditions;

/**
 * Cross between a task and a result handler. Takes results from a submitting task and then feeds to
 * another MultipleTask to run computations on. The submitting task should be started before this
 * one, and they should be in different threads.
 * 
 * <p>
 * This reduces the generics to just handling QuiverMatrix objects as the generics are causing
 * problems with constructors and builders with many subclasses.
 * 
 * @author John Lawson
 * 
 */
public abstract class RunOnResults extends RunMultipleTask<QuiverMatrix> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final RunMultipleTask<?> mSubmittingTask;
	private final RunOnResultsHandler mSubmittingHandler;
	private boolean mAllResultsReceived = false;


	protected RunOnResults(Builder<?> builder) {
		super(builder);
		this.mSubmittingTask = builder.mSubmittingTask;
		this.mSubmittingHandler = getSubmittingResultsHandler(null);
		this.mSubmittingTask.setResultHandler(mSubmittingHandler);
	}

	/**
	 * Get an instance of a {@link RunOnResultsHandler} which checks whether to submit a task for each
	 * result.
	 * 
	 * @return Handler instance
	 */
	protected abstract RunOnResultsHandler getSubmittingResultsHandler(@Nullable MatrixInfo initial);

	/*
	 * Block until all results from the submitting task have been handled and their new tasks have
	 * been submitted. (non-Javadoc)
	 * 
	 * @see uk.co.jwlawson.jcluster.RunMultipleTask#submitAllTasks()
	 */
	@Override
	protected void submitAllTasks() {
		while (!mAllResultsReceived) {
			try {
				synchronized (this) {
					wait();
					log.debug("All tasks submitted and object woken up in {}.", Thread.currentThread()
							.getName());
				}
			} catch (InterruptedException e) {
				log.debug("Caught interrupt in thread {}", Thread.currentThread().getName());
			}
		}
	}

	private void wakeup() {
		synchronized (this) {
			notify();
		}
	}

	protected abstract class RunOnResultsHandler extends TECSResultHandler {


		public RunOnResultsHandler(MatrixInfo initial) {
			super(initial);
		}

		protected abstract boolean shouldSubmit(MatrixInfo matrix);

		@Override
		protected final void handleResult(MatrixInfo matrix) {
			if (shouldSubmit(matrix)) {
				log.debug("Submitting result for new task");
				submitTaskFor(matrix.getMatrix());
			}
		}

		@Override
		protected final MatrixInfo getFinal() {
			log.debug("All results received. Waking submitting thread.");
			mAllResultsReceived = true;
			wakeup();
			log.debug("Notify to wake submitting thread sent from thread {}", Thread.currentThread()
					.getName());
			return getInitial();
		}

	}

	public static abstract class Builder<A extends Builder<A>> extends
			RunMultipleTask.Builder<QuiverMatrix, A> {

		private RunMultipleTask<?> mSubmittingTask;

		@Override
		protected abstract A self();

		public A withSubmittingTask(RunMultipleTask<?> submittingTask) {
			this.mSubmittingTask = submittingTask;
			return self();
		}

		@Override
		protected Builder<A> validate() {
			super.validate();
			Preconditions.checkNotNull(mSubmittingTask, "Submitting task cannot be null.");
			return self();
		}

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
