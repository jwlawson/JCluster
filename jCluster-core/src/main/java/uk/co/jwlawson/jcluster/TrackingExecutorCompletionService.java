/**
 * Copyright 2014 John Lawson
 * 
 * TrackingExecutorCompletionService.java is part of JCluster. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author John Lawson
 * 
 */
public class TrackingExecutorCompletionService<V> extends ExecutorCompletionService<V> {

	private final AtomicInteger numToTake;

	public TrackingExecutorCompletionService(Executor exec) {
		super(exec);
		numToTake = new AtomicInteger();
	}

	@Override
	public Future<V> submit(Callable<V> task) {
		try {
			return super.submit(task);
		} finally {
			numToTake.incrementAndGet();
		}
	}

	@Override
	public Future<V> poll() {
		Future<V> result = super.poll();
		if (result != null) {
			numToTake.decrementAndGet();
		}
		return result;
	}

	@Override
	public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
		Future<V> result = super.poll(timeout, unit);
		if (result != null) {
			numToTake.decrementAndGet();
		}
		return result;
	}

	@Override
	public Future<V> take() throws InterruptedException {
		Future<V> result = super.take();
		numToTake.decrementAndGet();
		return result;
	}

	public boolean hasResult() {
		return numToTake.get() != 0;
	}

}
