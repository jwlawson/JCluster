/**
 * Copyright 2014 John Lawson
 * 
 * CompletionHandler.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author John Lawson
 * 
 * @param <V>
 */
public abstract class CompletionHandler<V> implements Runnable {

	/** Logger. */
	private final Logger log = LoggerFactory.getLogger(getClass());
	/** Service to execute tasks. */
	private CompletionService<V> service;
	/** Queue to leave results in. */
	private CompletionResultQueue<V> queue;

	/**
	 * Set the queue.
	 * 
	 * @param q Queue to store results in
	 */
	public void setQueue(final CompletionResultQueue<V> q) {
		this.queue = q;
	}

	/**
	 * Set the service.
	 * 
	 * @param s Service to execute tasks
	 */
	public void setService(final CompletionService<V> s) {
		this.service = s;
	}

	/**
	 * Process the next task and queue its result.
	 */
	protected void handleNextTask() {
		Future<V> future;
		try {
			log.debug("Waiting for new result to queue up");
			future = service.take();
			V result = future.get();
			log.debug("Result pushed to queue {}", result);
			queue.pushResult(result);

		} catch (InterruptedException e) {
			log.error("Caught interrupt in thread {}", Thread.currentThread().getName(), e);
		} catch (ExecutionException e) {
			log.error("Exception in executing task", e);
			throw new RuntimeException("Exception in executing task", e);
		}
	}
}
