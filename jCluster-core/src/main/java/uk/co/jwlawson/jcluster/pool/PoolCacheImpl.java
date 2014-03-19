/**
 * Copyright 2014 John Lawson
 * 
 * PoolCacheImpl.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
package uk.co.jwlawson.jcluster.pool;

import uk.co.jwlawson.jcluster.data.HolderKey;
import uk.co.jwlawson.jcluster.data.IntMatrixPair;
import uk.co.jwlawson.jcluster.data.LinkHolder;
import uk.co.jwlawson.jcluster.data.QuiverKey;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author John Lawson
 * 
 */
public class PoolCacheImpl implements PoolCache {

	private final static int QUIVER_POOL_MAX = 10;
	private final static int HOLDER_POOL_MAX = 10;
	private final static int PAIR_POOL_MAX = 1;
	private final static int PAIR_KEY = 0;

	private final PoolFactory poolFactory;

	private final LoadingCache<? extends QuiverKey<?>, ? extends Pool<?>> quiverPoolCache;
	private final LoadingCache<? extends HolderKey<?>, ? extends Pool<? extends LinkHolder<?>>> holderPoolCache;
	private final LoadingCache<Integer, Pool<IntMatrixPair>> pairPoolCache;

	public PoolCacheImpl(PoolFactory factory) {
		quiverPoolCache = createQuiverCache();
		holderPoolCache = createHolderCache();
		pairPoolCache = createPairCache();
		poolFactory = factory;
	}

	@Override
	public <T extends QuiverMatrix> Pool<T> getQuiverMatrixPool(QuiverKey<T> key) {
		@SuppressWarnings("unchecked")
		LoadingCache<QuiverKey<T>, Pool<T>> cache =
				(LoadingCache<QuiverKey<T>, Pool<T>>) quiverPoolCache;
		return cache.getUnchecked(key);
	}

	@Override
	public <T extends QuiverMatrix> Pool<LinkHolder<T>> getHolderPool(HolderKey<T> key) {
		@SuppressWarnings("unchecked")
		LoadingCache<HolderKey<T>, Pool<LinkHolder<T>>> cache =
				(LoadingCache<HolderKey<T>, Pool<LinkHolder<T>>>) holderPoolCache;
		return cache.getUnchecked(key);
	}

	@Override
	public Pool<IntMatrixPair> getIntMatrixPairPool() {
		return pairPoolCache.getUnchecked(PAIR_KEY);
	}


	private <T extends QuiverMatrix> LoadingCache<QuiverKey<T>, Pool<T>> createQuiverCache() {
		return CacheBuilder.newBuilder().maximumSize(QUIVER_POOL_MAX)
				.build(new CacheLoader<QuiverKey<T>, Pool<T>>() {

					@Override
					public Pool<T> load(QuiverKey<T> key) throws Exception {
						return poolFactory.createQuiverPool(key);
					}
				});
	}

	private <T extends QuiverMatrix> LoadingCache<HolderKey<T>, Pool<LinkHolder<T>>> createHolderCache() {
		return CacheBuilder.newBuilder().maximumSize(HOLDER_POOL_MAX)
				.build(new CacheLoader<HolderKey<T>, Pool<LinkHolder<T>>>() {

					@Override
					public Pool<LinkHolder<T>> load(HolderKey<T> key) throws Exception {
						return poolFactory.createLinkHolderPool(key);
					}
				});
	}

	private <T extends QuiverMatrix> LoadingCache<Integer, Pool<IntMatrixPair>> createPairCache() {
		return CacheBuilder.newBuilder().maximumSize(PAIR_POOL_MAX)
				.build(new CacheLoader<Integer, Pool<IntMatrixPair>>() {

					@Override
					public Pool<IntMatrixPair> load(Integer key) throws Exception {
						return poolFactory.createIntMatrixPairPool();
					}

				});
	}
}
