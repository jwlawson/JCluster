/**
 * Copyright 2014 John Lawson
 * 
 * ThreadCacheImpl.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author John Lawson
 * 
 */
public class ThreadCacheImpl implements ThreadPoolCache {

	private final LoadingCache<Class<?>, ExecutorService> mThreadCache;
	private final ThreadPoolFactory mFactory;

	/**
	 * Create a new cache which creates new threadPools from the supplied factory.
	 * 
	 * @param factory Factory providing new ThreadPools
	 */
	public ThreadCacheImpl(ThreadPoolFactory factory) {
		mThreadCache = getCache();
		mFactory = factory;
	}

	@Override
	public ExecutorService getThreadPool(MatrixTask<?> task) {
		return mThreadCache.getUnchecked(task.getClass());
	}

	@Override
	public void shutdownAll() {
		for (ExecutorService exec : mThreadCache.asMap().values()) {
			exec.shutdownNow();
		}
	}

	private LoadingCache<Class<?>, ExecutorService> getCache() {
		return CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, ExecutorService>() {
			private final Logger log = LoggerFactory.getLogger(getClass());

			@Override
			public ExecutorService load(Class<?> key) throws Exception {
				log.debug("Creating new ThreadPool for MatrixTask {}", key.getSimpleName());
				return mFactory.createThreadPool();
			}
		});
	}

}
