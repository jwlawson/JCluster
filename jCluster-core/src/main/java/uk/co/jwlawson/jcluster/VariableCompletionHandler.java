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

	private int numUnhandled;
	private boolean waitIfEmpty = false;

	public void setWaitIfEmpty(boolean waitIfEmpty) {
		this.waitIfEmpty = waitIfEmpty;
	}

	public void taskAdded() {

		synchronized (this) {
			numUnhandled++;
			if (numUnhandled == 1) {
				notify();
			}
		}
		log.trace("Task added");
	}

	@Override
	public void run() {
		while (numUnhandled > 0 || waitIfEmpty) {

			if (numUnhandled == 0) {
				log.debug("Waiting for new tasks");

				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					log.error("Interrupt", e);
				}
			}

			handleNextTask();

			synchronized (this) {
				numUnhandled--;
			}
			log.trace("Task handled");
		}

	}

}
