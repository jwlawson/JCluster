/**
 * Copyright 2014 John Lawson
 * 
 * ArrayCompletionResultQueue.java is part of JCluster. Licensed under the Apache License, Version
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queue containing results calculated by a CompletionService.
 * 
 * <p>
 * The queue is blocking, so this can be used to wait for results to be calculated.
 * 
 * @param <T> Type of object in the queue
 * @author John Lawson
 * 
 */
public class ArrayCompletionResultQueue<T> implements CompletionResultQueue<T> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Default size of the queue. */
	private static final int DEF_SIZE = 10;
	/** Backing queue to store results in. */
	private final BlockingQueue<T> queue;

	/** Create a new queue of default size (100). */
	public ArrayCompletionResultQueue() {
		this(DEF_SIZE);
	}

	/**
	 * Create a new queue of specified size.
	 * 
	 * @param size Size of queue
	 */
	public ArrayCompletionResultQueue(final int size) {
		queue = new ArrayBlockingQueue<T>(size);
	}

	/**
	 * Put a new result in the queue.
	 * <p>
	 * Will block if the queue is full.
	 * 
	 * @param result Result to put on queue.
	 */
	@Override
	public void pushResult(final T result) {
		if (result == null) {
			log.error("Null result pushed to queue");
			return;
		}
		try {
			queue.put(result);
		} catch (InterruptedException e) {
			log.error("Thread interrupted {}", Thread.currentThread().getName(), e);
		}
	}

	/**
	 * Get the next result from the queue.
	 * <p>
	 * Will block if no results are available.
	 * 
	 * @return The next result from the queue.
	 * @throws InterruptedException if thread get interrupted
	 */
	@Override
	@Nullable
	public T popResult() throws InterruptedException {
		return queue.poll(1, TimeUnit.SECONDS);
	}

	/**
	 * Check whether the queue has any results waiting.
	 * 
	 * @return true if the queue is not empty
	 */
	@Override
	public boolean hasResult() {
		return !queue.isEmpty();
	}

	@Override
	public void allResultsQueued() {}


}
