/**
 * Copyright 2014 John Lawson
 * 
 * Threads.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
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
 * Get ThreadPools for the specific task that is to be run.
 * 
 * @author John Lawson
 * 
 */
public class Threads {

	/**
	 * Get the ThreadPool which should be used to run any tasks submitted by the specified task.
	 * 
	 * @param task Task to get the ThreadPool for
	 * @return ThreadPool
	 */
	public static ExecutorService getThreadPoolForTask(MatrixTask<?> task) {
		return mCache.getThreadPool(task);
	}

	/**
	 * Send {@link ExecutorService#shutdownNow()} signal to all cached threads.
	 */
	public static void shutdownAll() {
		mCache.shutdownAll();
	}

	private static ThreadPoolCache mCache = new ThreadCacheImpl(new BlockingThreadPoolFactory());

}
