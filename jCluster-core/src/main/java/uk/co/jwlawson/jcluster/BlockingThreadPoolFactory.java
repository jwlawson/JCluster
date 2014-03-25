/**
 * Copyright 2014 John Lawson
 * 
 * BlockingThreadPoolFactory.java is part of JCluster. Licensed under the Apache License, Version
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolFactory which provides BlockingThreadPool pools.
 * 
 * @author John Lawson
 * 
 */
public class BlockingThreadPoolFactory implements ThreadPoolFactory {

	private static final int DEF_NUM_THREADS = 4;
	private static final int DEF_NUM_QUEUE = 15;
	private static final long DEF_KEEP_ALIVE = 1;
	private static final TimeUnit DEF_KEEP_TIMEUNIT = TimeUnit.SECONDS;
	private static final long DEF_BLOCKING_TIMEOUT = 100;
	private static final TimeUnit DEF_BLOCKING_TIMEUNIT = TimeUnit.MILLISECONDS;

	@Override
	public ExecutorService createThreadPool() {
		return new BlockingThreadPool(DEF_NUM_THREADS, DEF_NUM_QUEUE, DEF_KEEP_ALIVE,
				DEF_KEEP_TIMEUNIT, DEF_BLOCKING_TIMEOUT, DEF_BLOCKING_TIMEUNIT, new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						return true;
					}
				});
	}


}
