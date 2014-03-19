/**
 * Copyright 2014 John Lawson
 * 
 * FuriousPoolFactory.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
package uk.co.jwlawson.jcluster.pool.furious;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.PoolableObject;
import uk.co.jwlawson.jcluster.data.HolderKey;
import uk.co.jwlawson.jcluster.data.IntMatrixPair;
import uk.co.jwlawson.jcluster.data.LinkHolder;
import uk.co.jwlawson.jcluster.data.QuiverKey;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;
import uk.co.jwlawson.jcluster.pool.PoolFactory;

/**
 * @author John Lawson
 * 
 */
public class FuriousPoolFactory implements PoolFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.co.jwlawson.jcluster.PoolFactory#createQuiverPool(uk.co.jwlawson.jcluster.pool.Pools.QuiverKey)
	 */
	@Override
	public <T extends QuiverMatrix> Pool<T> createQuiverPool(QuiverKey<T> key) {
		PoolableObject<T> poolable =
				new QuiverMatrixPoolableObject<T>(key.getRows(), key.getCols(), key.getClassObject());
		return createPool(poolable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.co.jwlawson.jcluster.PoolFactory#createLinkHolderPool(uk.co.jwlawson.jcluster.pool.Pools.HolderKey
	 * )
	 */
	@Override
	public <T extends QuiverMatrix> Pool<LinkHolder<T>> createLinkHolderPool(HolderKey<T> key) {
		PoolableObject<LinkHolder<T>> poolable = new LinkHolderPoolableObject<T>(key.getSize());
		return createPool(poolable);
	}

	@Override
	public Pool<IntMatrixPair> createIntMatrixPairPool() {
		return createPool(new IntMatrixPairPoolableObject());
	}

	private <T> Pool<T> createPool(PoolableObject<T> poolable) {
		PoolSettings<T> settings = new PoolSettings<T>(poolable);
		settings.max(-1);
		settings.maxIdle(500000);
		PoolSettings.timeBetweenTwoControls(600);
		Pool<T> pool = wrapPool(settings.pool());
		return pool;
	}

	private <T> Pool<T> wrapPool(ObjectPool<T> pool) {
		Pool<T> result = new FuriousPoolAdaptor<T>(pool);
		return result;
	}

}
