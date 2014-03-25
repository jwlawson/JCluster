/**
 * Copyright 2014 John Lawson
 * 
 * TECSResultHandler.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.MatrixInfo;

/**
 * Callable which handles results from a CompletionService.
 * 
 * <p>
 * Implementations should handle each result as it is computed and provide a final MatrixInfo object
 * once all results have been considered.
 * 
 * @author John Lawson
 * 
 */
public abstract class TECSResultHandler implements Callable<MatrixInfo> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** CompletionService which gives the futures once completed. */
	private TrackingExecutorCompletionService<MatrixInfo> mService;

	/** True if the completion service is running and has tasks being submitted. */
	private final AtomicBoolean mRunning;

	/** Initial MatrixInfo object containing the original matrix and any info known about it. */
	private final MatrixInfo mInitial;

	/** Lock to check whether the hanlder is waiting for a mTask. */
	private final ReentrantLock mLock;

	/** Reference to thread which is handling results. */
	private final AtomicReference<Thread> mHandlingThread;

	/** Task creating the results. */
	@Nullable
	private MatrixTask<?> mTask;

	/**
	 * Create a new instance with the specified initial MatrixInfo object.
	 * 
	 * @param initial Initial matrix
	 */
	public TECSResultHandler(MatrixInfo initial) {
		mInitial = initial;
		mRunning = new AtomicBoolean(true);
		mHandlingThread = new AtomicReference<Thread>();
		mLock = new ReentrantLock();
	}

	/**
	 * Set the CompletionService to take results from.
	 * 
	 * @param mService CompletionService
	 */
	public void setCompletionService(TrackingExecutorCompletionService<MatrixInfo> mService) {
		this.mService = mService;
	}

	/**
	 * Set the main mTask which is being run and which is relying on the results from this.
	 * 
	 * @param mTask Task which is waiting for the results from this
	 */
	public void setTask(MatrixTask<?> task) {
		this.mTask = task;
	}

	/**
	 * Request that the overlying mTask stops if it has been set.
	 * <p>
	 * This should be used if the results from the calculations carried out so far has determined the
	 * final information required, so no further calculations are required.
	 */
	protected void requestStop() {
		if (mTask != null) {
			mTask.requestStop();
		}
	}

	/**
	 * Inform the result handler that all tasks have been submitted to the CompletionService, so once
	 * the service has no further results to provide then the results handler should return its final
	 * result.
	 */
	public final void allTasksSubmitted() {
		mRunning.set(false);
		if (mLock.isLocked()) {
			log.debug("Interrupting thread {} from thread {} as it is waiting for mTask", mHandlingThread
					.get().getName(), Thread.currentThread().getName());
			mHandlingThread.get().interrupt();
		}
	}

	@Override
	public final MatrixInfo call() throws Exception {
		mHandlingThread.set(Thread.currentThread());
		while (mService.hasResult() || mRunning.get()) {
			Future<MatrixInfo> future = null;
			mLock.lock();
			try {
				future = mService.poll(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.debug("Polling for result was interrupted in thread {}", mHandlingThread.get()
						.getName());
				continue;
			} finally {
				mLock.unlock();
			}
			if (future == null) {
				log.debug("TECS has no results. Poll fell through.");
				continue;
			}
			MatrixInfo result = future.get();
			handleResult(result);
		}
		return getFinal();
	}

	/**
	 * Handle the computed result.
	 * 
	 * @param result New result
	 */
	protected abstract void handleResult(MatrixInfo result);

	/**
	 * Get the final result to return once all results have been handled.
	 * 
	 * @return Final MatrixInfo result
	 */
	protected abstract MatrixInfo getFinal();

	/**
	 * Get the original MatrixInfo object. Used by subclasses to combine the information from the
	 * results with the original info.
	 * 
	 * @return Initial MatrixInfo object
	 */
	protected final MatrixInfo getInitial() {
		return mInitial;
	}

}
