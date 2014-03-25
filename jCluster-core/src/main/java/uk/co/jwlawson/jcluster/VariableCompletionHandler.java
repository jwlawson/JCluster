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

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class VariableCompletionHandler<V> extends CompletionHandler<V> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Number of results waiting to be handled. */
	private final Semaphore semaphore;
	/**
	 * True if the handler should wait for more results when there are no results to handle
	 * immediately. Set to true by default, so that once the handler is started it won't return
	 * straight away.
	 */
	private final AtomicBoolean waitIfEmpty = new AtomicBoolean(true);
	private final AtomicBoolean waiting = new AtomicBoolean(false);
	private Thread handlingThread;

	public VariableCompletionHandler() {
		semaphore = new Semaphore(100);
		semaphore.drainPermits();
	}

	/**
	 * Set whether the handler should wait for more results if there are no more results to handle
	 * immediately. If set to false and the handler is currently waiting then this will wake the
	 * handler.
	 * 
	 * @param wait true if should wait for more results
	 */
	public void setWaitIfEmpty(boolean wait) {
		this.waitIfEmpty.set(wait);
		log.debug("Handler set to wait: {}.", wait);
		synchronized (this) {
			if (!wait && waiting.get()) {
				log.debug("Waking handler on thread {}", handlingThread.getName());
				handlingThread.interrupt();
			}
		}
	}

	/**
	 * Inform the handler that a new result will be available once computed.
	 * <p>
	 * If the handler is waiting for a result then this will wake it.
	 */
	public void taskAdded() {
		semaphore.release();
		log.debug("Task added");
	}

	@Override
	public void run() {
		handlingThread = Thread.currentThread();
		while (semaphore.availablePermits() > 0 || waitIfEmpty.get()) {
			log.trace("Running. unhandled:{} Wait:{}", semaphore.availablePermits(), waitIfEmpty.get());

			log.trace("Waiting for new tasks");
			try {
				waiting.set(true);
				semaphore.acquire();
				waiting.set(false);
			} catch (InterruptedException e) {
				waiting.set(false);
				log.info("Thread {} interrupted", Thread.currentThread().getName());
				continue;
			}
			log.debug("Handling task");
			boolean handled = handleNextTask();
			if (!handled) {
				log.error("Result not handled");
				// Put back so we can try again
				semaphore.release();
			}
		}
		log.debug("Handler finished");
	}

}
