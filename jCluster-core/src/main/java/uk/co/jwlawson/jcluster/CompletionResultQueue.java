/**
 * Copyright 2014 John Lawson
 * 
 * CompletionResultQueue.java is part of JCluster. Licensed under the Apache License, Version 2.0
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

/**
 * Queue containing results calculated by a CompletionService.
 * 
 * @param <T> Type of object in the queue
 * @author John Lawson
 * 
 */
public interface CompletionResultQueue<T> {
	/**
	 * Put a new result in the queue.
	 * 
	 * @param result Result to put on queue.
	 */
	void pushResult(T result);

	/**
	 * Get the next result from the queue or null if the thread is interrupted.
	 * 
	 * @return The next result from the queue.
	 * @throws InterruptedException
	 */
	T popResult() throws InterruptedException;

	/**
	 * Check whether the queue has any results waiting.
	 * 
	 * @return true if the queue is not empty
	 */
	boolean hasResult();

	/**
	 * Inform the queue that all possible results have been queued up and no further results will be
	 * pushed onto it.
	 */
	void allResultsQueued();

}
