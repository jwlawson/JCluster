/**
 * Copyright 2014 John Lawson
 * 
 * MultiwayPoolFactory.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
package uk.co.jwlawson.jcluster.pool.multiway;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.HolderKey;
import uk.co.jwlawson.jcluster.data.IntMatrixPair;
import uk.co.jwlawson.jcluster.data.LinkHolder;
import uk.co.jwlawson.jcluster.data.QuiverKey;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;
import uk.co.jwlawson.jcluster.pool.PoolFactory;

import com.github.benmanes.multiway.LoadingMultiwayPool;
import com.github.benmanes.multiway.MultiwayPoolBuilder;

/**
 * @author John Lawson
 * 
 */
public class MultiwayPoolFactory implements PoolFactory {

	private static final int MAX_SIZE = 30000;
	private static final int EXPIRE_SEC = 30;

	private final Logger log = LoggerFactory.getLogger(getClass());
	private LoadingMultiwayPool<? extends QuiverKey<?>, ? extends QuiverMatrix> quiverPool;
	private LoadingMultiwayPool<? extends HolderKey<?>, ? extends LinkHolder<?>> holderPool;
	private LoadingMultiwayPool<Integer, IntMatrixPair> pairPool;

	@Override
	public <T extends QuiverMatrix> Pool<T> createQuiverPool(QuiverKey<T> key) {
		LoadingMultiwayPool<QuiverKey<T>, T> backingPool = getBackingMatrixPool();
		return wrapMatrixPool(backingPool, key);
	}

	@Override
	public <T extends QuiverMatrix> Pool<LinkHolder<T>> createLinkHolderPool(HolderKey<T> key) {
		LoadingMultiwayPool<HolderKey<T>, LinkHolder<T>> backingPool = getBackingHoldingPool();
		return wrapHolderPool(backingPool, key);
	}

	@Override
	public Pool<IntMatrixPair> createIntMatrixPairPool() {
		LoadingMultiwayPool<Integer, IntMatrixPair> pool = getPairPool();
		return new IntMatrixPairMultiwayPoolAdaptor(pool);
	}

	@SuppressWarnings("unchecked")
	private synchronized <T extends QuiverMatrix> LoadingMultiwayPool<QuiverKey<T>, T> getBackingMatrixPool() {
		if (quiverPool == null) {
			quiverPool =
					MultiwayPoolBuilder.newBuilder().expireAfterAccess(EXPIRE_SEC, TimeUnit.SECONDS)
							.maximumSize(MAX_SIZE).build(new QuiverMatrixResourceLoader<T>());
			log.info("New {} created", quiverPool.getClass().getSimpleName());
		}
		return (LoadingMultiwayPool<QuiverKey<T>, T>) quiverPool;
	}

	private <T extends QuiverMatrix> Pool<T> wrapMatrixPool(
			LoadingMultiwayPool<QuiverKey<T>, T> pool, QuiverKey<T> key) {

		return new QuiverMatrixMultiwayPoolAdaptor<T>(pool, key);
	}

	@SuppressWarnings("unchecked")
	private synchronized <T extends QuiverMatrix> LoadingMultiwayPool<HolderKey<T>, LinkHolder<T>> getBackingHoldingPool() {
		if (holderPool == null) {
			holderPool =
					MultiwayPoolBuilder.newBuilder().expireAfterAccess(EXPIRE_SEC, TimeUnit.SECONDS)
							.maximumSize(MAX_SIZE).build(new LinkHolderResourceLoader<T>());
			log.info("New {} created", holderPool.getClass().getSimpleName());
		}
		return (LoadingMultiwayPool<HolderKey<T>, LinkHolder<T>>) holderPool;
	}

	private <T extends QuiverMatrix> Pool<LinkHolder<T>> wrapHolderPool(
			LoadingMultiwayPool<HolderKey<T>, LinkHolder<T>> pool, HolderKey<T> key) {

		return new LinkHolderMultiWayPoolAdaptor<T>(pool, key);
	}

	private synchronized LoadingMultiwayPool<Integer, IntMatrixPair> getPairPool() {
		if (pairPool == null) {
			pairPool =
					MultiwayPoolBuilder.newBuilder().maximumSize(10)
							.expireAfterAccess(EXPIRE_SEC, TimeUnit.SECONDS)
							.build(new IntMatrixPairResourceLoader());
			log.info("New {} created", pairPool.getClass().getSimpleName());
		}
		return pairPool;
	}

}
