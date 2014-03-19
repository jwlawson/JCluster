/**
 * Copyright 2014 John Lawson
 * 
 * VariableCompletionHandler.java is part of JCluster. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class VariableCompletionHandler<V> extends CompletionHandler<V> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Number of results waiting to be handled. */
	private int numUnhandled;
	/**
	 * True if the handler should wait for more results when there are no results to handle
	 * immediately. Set to true by default, so that once the handler is started it won't return
	 * straight away.
	 */
	private boolean waitIfEmpty = true;
	/** True if waiting on this object. */
	private boolean waiting = false;

	/**
	 * Set whether the handler should wait for more results if there are no more results to handle
	 * immediately. If set to false and the handler is currently waiting then this will wake the
	 * handler.
	 * 
	 * @param waitIfEmpty true if should wait for more results
	 */
	public void setWaitIfEmpty(boolean waitIfEmpty) {
		this.waitIfEmpty = waitIfEmpty;
		log.debug("Handler set to wait: {}. Currently waiting: {}", waitIfEmpty, waiting);
		synchronized (this) {
			if (waiting && !waitIfEmpty) {
				log.trace("Waking handler");
				notify();
			}
		}
	}

	/**
	 * Inform the handler that a new result will be available once computed.
	 * <p>
	 * If the handler is waiting for a result then this will wake it.
	 */
	public void taskAdded() {

		synchronized (this) {
			numUnhandled++;
			if (waiting) {
				log.debug("Waking handler as new task produced");
				notify();
			}
		}
		log.debug("Task added");
	}

	@Override
	public void run() {
		while (numUnhandled > 0 || waitIfEmpty) {
			log.trace("Running. unhandled:{} Wait:{}", numUnhandled, waitIfEmpty);

			if (numUnhandled == 0) {
				log.trace("Waiting for new tasks");

				synchronized (this) {
					try {
						waiting = true;
						wait();
						log.trace("Handler woken");
						waiting = false;
					} catch (InterruptedException e) {
						log.info("Thread {} interrupted", Thread.currentThread().getName(), e);
					}
				}
			} else {
				log.debug("Handling task");
				handleNextTask();
				log.debug("Task handled");

				synchronized (this) {
					numUnhandled--;
				}
			}
		}
		log.debug("Handler finished");

	}

}
