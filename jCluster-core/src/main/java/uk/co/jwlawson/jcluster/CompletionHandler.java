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
 * @author John Lawson
 * 
 */
public abstract class CompletionHandler<V> implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private CompletionService<V> service;
	private CompletionResultQueue<V> queue;

	public void setQueue(CompletionResultQueue<V> queue) {
		this.queue = queue;
	}

	public void setService(CompletionService<V> service) {
		this.service = service;
	}

	protected void handleNextTask() {
		Future<V> future;
		try {
			future = service.take();
			queue.pushResult(future.get());
		} catch (InterruptedException e) {
			log.error("{} interrupted", getClass().getSimpleName(), e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			log.error("Exception in executing task", e);
			throw new RuntimeException("Exception in executing task", e);
		}
	}

}
