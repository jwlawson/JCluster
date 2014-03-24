/**
 * Copyright 2014 John Lawson
 * 
 * MatrixInfoResultHandler.java is part of JCluster. Licensed under the Apache License, Version 2.0
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

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.MatrixInfo;

/**
 * @author John Lawson
 * 
 */
public abstract class MatrixInfoResultHandler implements Callable<MatrixInfo> {

	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Queue that the new results are being left in. */
	private CompletionResultQueue<MatrixInfo> mQueue;
	/** True if the task is still creating new results. */
	private boolean running = true;
	/** True if currently blocking. */
	private boolean waiting = false;
	/** Initial matrix. */
	private final MatrixInfo mInitial;

	private Thread mHandlingThread;

	private final String thisstr = this.toString();

	/** Task creating the results. */
	@Nullable
	private MatrixTask<?> task;

	/**
	 * Create a new instance to combine results from the tasks into the supplied MatrixInfo object.
	 * 
	 * @param initial Info about the matrix to store the final result
	 */
	public MatrixInfoResultHandler(MatrixInfo initial) {
		mInitial = initial;
	}

	/**
	 * Set the queue which holds the results of the calculations which are to be handled.
	 * 
	 * @param queue Queue of results
	 */
	public void setQueue(CompletionResultQueue<MatrixInfo> queue) {
		mQueue = queue;
	}

	/**
	 * Get the original MatrixInfo object. Used by subclasses to combine the information from the
	 * results with the original info.
	 */
	protected MatrixInfo getInitial() {
		return mInitial;
	}

	/**
	 * Set the main task which is being run and which is relying on the results from this.
	 * 
	 * @param task Task which is waiting for the results from this
	 */
	public void setTask(MatrixTask<?> task) {
		this.task = task;
	}

	/**
	 * Request that the overlying task stops if it has been set.
	 * <p>
	 * This should be used if the results from the calculations carried out so far has determined
	 * the final information required, so no further calculations are required.
	 */
	protected void requestStop() {
		if (task != null) {
			task.requestStop();
		}
	}

	/**
	 * Handle the new result.
	 * 
	 * @param matrix New result
	 */
	protected abstract void handleResult(MatrixInfo matrix);

	/**
	 * Get the final MatrixInfo object after processing all the results.
	 * 
	 * @return Final MatrixInfo object
	 */
	protected abstract MatrixInfo getFinal();

	/**
	 * Inform the handler that all results have been queued, so once the queue is empty there will
	 * be no more.
	 */
	public void allResultsQueued() {
		synchronized (this) {
			running = false;
		}
		mQueue.allResultsQueued();

		if (mHandlingThread != null) {
//			log.debug("Interrupting handling thread {} from thread {}. {}", mHandlingThread,
//					Thread.currentThread(), thisstr);
			mHandlingThread.interrupt();
		}
	}

	@Override
	public MatrixInfo call() throws Exception {
		mHandlingThread = Thread.currentThread();
		boolean run;
		synchronized (this) {
			run = mQueue.hasResult() || running;
		}
		while (run) {

			try {
				waiting = true;
				MatrixInfo info = mQueue.popResult();
				waiting = false;
				if (info != null) {
					handleResult(info);
				} else {
					log.debug("Queue pop result timed out. hasResult(): {} Running: {} {}",
							mQueue.hasResult(), running, thisstr);
					break;
				}
			} catch (InterruptedException e) {
				log.debug("Caught interrupt in thread {}. Running: {}", Thread.currentThread()
						.getName(), running);
			}
			synchronized (this) {
				run = mQueue.hasResult() || running;
			}
		}
		return getFinal();
	}

	public boolean isWaiting() {
		return waiting;
	}

}
