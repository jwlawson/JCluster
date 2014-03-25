/**
 * Copyright 2014 John Lawson
 * 
 * ThreadPoolCache.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import java.util.concurrent.ExecutorService;

/**
 * Cache the ThreadPools depending on which task should be run on each pool.
 * 
 * @author John Lawson
 * 
 */
public interface ThreadPoolCache {

	/**
	 * Get the ThreadPool which should be used to run any tasks submitted by this task.
	 * 
	 * @param task Task to get pool for
	 * @return ThreadPool
	 */
	ExecutorService getThreadPool(MatrixTask<?> task);

	/**
	 * Shutdown all threads in the cache.
	 */
	void shutdownAll();

}
