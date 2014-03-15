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
package uk.co.jwlawson.jcluster;

import java.util.HashMap;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolSettings;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to provide an ObjectPool of QuiverMatrix objects. Caches the instances so that
 * the pools are always reused.
 * 
 * @author John Lawson
 * 
 */
public class Pools {

	private final static Logger logger = LoggerFactory.getLogger(Pools.class);

	private static HashMap<QuiverKey<?>, Pool<?>> sQuiverPoolMap =
			new HashMap<QuiverKey<?>, Pool<?>>();
	private static HashMap<HolderKey<?>, Pool<? extends LinkHolder<?>>> sHolderPoolMap =
			new HashMap<HolderKey<?>, Pool<? extends LinkHolder<?>>>();
	private static Pool<IntMatrixPair> sMatrixPairPool;

	/**
	 * Get the instance of {@link ObjectPool} which provides objects which extend {@link QuiverMatrix}
	 * . The class provided will be the type of objects returned by the pool.
	 * 
	 * @param rows Number of rows in the matrices
	 * @param cols Number of columns in the matrices
	 * @param clazz Type of QuiverMatrix to return
	 * @return {@link ObjectPool} which provides objects of class {@code clazz}.
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <T extends QuiverMatrix> Pool<T> getQuiverMatrixPool(int rows,
			int cols, Class<T> clazz) {
		int id = getId(rows, cols);
		QuiverKey<T> key = new QuiverKey<T>(id, clazz);
		if (sQuiverPoolMap.containsKey(key)) {
			return (Pool<T>) sQuiverPoolMap.get(key);
		} else {
			logger.info("Creating new {} pool {}x{}", clazz.getSimpleName(), rows, cols);
			PoolSettings<T> settings =
					new PoolSettings<T>(new QuiverMatrixPoolableObject<T>(rows, cols, clazz));
			settings.max(-1);
			settings.maxIdle(500000);
			PoolSettings.timeBetweenTwoControls(600);
			Pool<T> pool = getPool(settings.pool());
			sQuiverPoolMap.put(key, pool);
			return pool;
		}
	}

	/**
	 * Get an instance of {@link ObjectPool} which provides a source for {@link LinkHolder}. The class
	 * {@code clazz} specifies which type of {@link QuiverMatrix} the {@link LinkHolder} expects as
	 * its matrix.
	 * 
	 * @param size Number of links in each holder
	 * @param quiverClass Type of {@link QuiverMatrix} expected to be held in each holder
	 * @return Pool of {@link LinkHolder} objects
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <T extends QuiverMatrix> Pool<LinkHolder<T>> getHolderPool(int size,
			Class<T> quiverClass) {
		HolderKey<T> key = new HolderKey<T>(size, quiverClass);
		if (sHolderPoolMap.containsKey(key)) {
			return (Pool<LinkHolder<T>>) sHolderPoolMap.get(key);
		} else {
			logger.info("Creating new Holder pool of size {}", size);
			PoolSettings<LinkHolder<T>> settings =
					new PoolSettings<LinkHolder<T>>(new LinkHolderPoolableObject<T>(size));
			settings.max(-1);
			settings.maxIdle(500000);
			PoolSettings.timeBetweenTwoControls(600);
			Pool<LinkHolder<T>> pool = getPool(settings.pool());
			sHolderPoolMap.put(key, pool);
			return pool;
		}
	}

	/**
	 * Get an instance of an {@link ObjectPool} which provides {@link IntMatrixPair} ojects.
	 * 
	 * @return Pool of {@link IntMatrixPair} objects
	 */
	public static synchronized Pool<IntMatrixPair> getIntMatrixPairPool() {
		if (sMatrixPairPool == null) {
			PoolSettings<IntMatrixPair> settings =
					new PoolSettings<IntMatrixPair>(new IntMatrixPairPoolableObject());
			settings.max(-1);
			settings.maxIdle(100);
			sMatrixPairPool = getPool(settings.pool());
		}
		return sMatrixPairPool;
	}

	/*
	 * Szudzik's function. See http://szudzik.com/ElegantPairing.pdf
	 */
	private static int getId(int a, int b) {
		return a >= b ? a * a + a + b : a + b * b;
	}

	private static <T> Pool<T> getPool(ObjectPool<T> pool) {
		Pool<T> result = new FuriousPoolAdaptor<T>(pool);
		return result;
	}

	/**
	 * Pools should not be instantiated itself. Just use the getInstance method to get the required
	 * ObjectPool.
	 */
	private Pools() {}

	/**
	 * Key object used to store the {@link QuiverMatrix} pools in the cache.
	 * 
	 * @author John Lawson
	 * 
	 * @param <V> Type of QuiverMatrix provided by the pool
	 */
	private static class QuiverKey<V> {
		private final int id;
		private final Class<V> clazz;

		public QuiverKey(int id, Class<V> clazz) {
			this.id = id;
			this.clazz = clazz;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (this == obj)
				return true;
			if (obj.getClass() != getClass())
				return false;
			QuiverKey<?> rhs = (QuiverKey<?>) obj;
			return (id == rhs.id) && (clazz == rhs.clazz);
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(57, 97).append(id).append(clazz).toHashCode();
		}
	}

	/**
	 * Key object used to lookup {@link LinkHolder} pools in the cache
	 * 
	 * @author John Lawson
	 * 
	 * @param <V> Type of {@link QuiverMatrix} expected by the holders in the pool
	 */
	private static class HolderKey<V> {
		private final int size;
		private final Class<V> clazz;

		public HolderKey(int size, Class<V> clazz) {
			this.size = size;
			this.clazz = clazz;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (this == obj)
				return true;
			if (obj.getClass() != getClass())
				return false;
			HolderKey<?> rhs = (HolderKey<?>) obj;
			return (size == rhs.size) && (clazz == rhs.clazz);
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(57, 293).append(size).append(clazz).toHashCode();
		}
	}
}
