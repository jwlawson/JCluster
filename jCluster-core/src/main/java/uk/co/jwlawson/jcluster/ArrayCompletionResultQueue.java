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
 * @author John Lawson
 * 
 */
public class ArrayCompletionResultQueue<T> implements CompletionResultQueue<T> {

	private final static int DEF_SIZE = 100;
	private final BlockingQueue<T> queue;

	public ArrayCompletionResultQueue() {
		this(DEF_SIZE);
	}

	public ArrayCompletionResultQueue(int size) {
		queue = new ArrayBlockingQueue<T>(size);
	}

	public void pushResult(T result) {
		try {
			queue.put(result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Nullable
	public T popResult() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean hasResult() {
		return !queue.isEmpty();
	}

}
