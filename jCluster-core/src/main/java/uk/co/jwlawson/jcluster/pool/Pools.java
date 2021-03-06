/**
 * Copyright 2014 John Lawson
 * 
 * QuiverPool.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import nf.fr.eraasoft.pool.ObjectPool;
import uk.co.jwlawson.jcluster.data.HolderKey;
import uk.co.jwlawson.jcluster.data.IntMatrixPair;
import uk.co.jwlawson.jcluster.data.LinkHolder;
import uk.co.jwlawson.jcluster.data.QuiverKey;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.dummy.DummyPoolFactory;

/**
 * Convenience class to provide an ObjectPool of QuiverMatrix objects. Caches the instances so that
 * the pools are always reused.
 * 
 * @author John Lawson
 * 
 */
public class Pools {

	private static PoolCache poolCache = new PoolCacheImpl(new DummyPoolFactory());

	/**
	 * Get the instance of {@link ObjectPool} which provides objects which extend {@link QuiverMatrix}
	 * . The class provided will be the type of objects returned by the pool.
	 * 
	 * <p>
	 * The pool is thread local, so only return objects which have been borrowed in the same thread.
	 * 
	 * @param rows Number of rows in the matrices
	 * @param cols Number of columns in the matrices
	 * @param clazz Type of QuiverMatrix to return
	 * @return {@link Pool} which provides objects of class {@code clazz}.
	 */
	public static <T extends QuiverMatrix> Pool<T> getQuiverMatrixPool(int rows, int cols,
			Class<T> clazz) {

		QuiverKey<T> key = new QuiverKey<T>(rows, cols, clazz);
		return poolCache.getQuiverMatrixPool(key);
	}

	/**
	 * Get an instance of {@link Pool} which provides a source for {@link LinkHolder}. The class
	 * {@code clazz} specifies which type of {@link QuiverMatrix} the {@link LinkHolder} expects as
	 * its matrix.
	 * 
	 * @param size Number of links in each holder
	 * @param quiverClass Type of {@link QuiverMatrix} expected to be held in each holder
	 * @return Pool of {@link LinkHolder} objects
	 */
	public static <T extends QuiverMatrix> Pool<LinkHolder<T>> getHolderPool(int size,
			Class<T> quiverClass) {

		HolderKey<T> key = new HolderKey<T>(size, quiverClass);
		return poolCache.getHolderPool(key);
	}

	/**
	 * Get an instance of an {@link ObjectPool} which provides {@link IntMatrixPair} ojects.
	 * 
	 * @return Pool of {@link IntMatrixPair} objects
	 */
	public static Pool<IntMatrixPair> getIntMatrixPairPool() {
		return poolCache.getIntMatrixPairPool();
	}

	/**
	 * Pools should not be instantiated itself. Just use the getInstance method to get the required
	 * ObjectPool.
	 */
	private Pools() {}

}
