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

import javax.annotation.Nullable;

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

	/** Default size of the queue. */
	private static final int DEF_SIZE = 100;
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
		try {
			queue.put(result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the next result from the queue or null if the thread is interrupted.
	 * <p>
	 * Will block if no results are available.
	 * 
	 * @return The next result from the queue.
	 */
	@Override
	@Nullable
	public T popResult() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
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


}
