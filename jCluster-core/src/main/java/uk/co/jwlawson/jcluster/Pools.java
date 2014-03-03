/**
 * Copyright 2014 John Lawson
 * 
 * QuiverPool.java is part of JCluster.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.jwlawson.jcluster;

import java.util.HashMap;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to provide an ObjectPool of QuiverMatrix objects.
 * Caches the instances so that the pools are always reused.
 * 
 * @author John Lawson
 * 
 */
public class Pools {

	private final static Logger logger = LoggerFactory.getLogger(Pools.class);

	private static HashMap<Integer, ObjectPool<QuiverMatrix>> sQuiverPoolMap = new HashMap<Integer, ObjectPool<QuiverMatrix>>();
	private static HashMap<Integer, ObjectPool<LinkHolder>> sHolderPoolMap = new HashMap<Integer, ObjectPool<LinkHolder>>();

	public static synchronized ObjectPool<QuiverMatrix> getQuiverMatrixPool(int rows, int cols) {
		int id = getId(rows, cols);
		if (sQuiverPoolMap.containsKey(id)) {
			return sQuiverPoolMap.get(id);
		} else {
			logger.info("Creating new pool {}x{}", rows, cols);
			PoolSettings<QuiverMatrix> settings = new PoolSettings<QuiverMatrix>(
					new QuiverMatrixPoolableObject(rows, cols));
			settings.max(-1);
			settings.maxIdle(500000);
			PoolSettings.timeBetweenTwoControls(600);
			ObjectPool<QuiverMatrix> pool = settings.pool();
			sQuiverPoolMap.put(id, pool);
			return pool;
		}
	}

	public static synchronized ObjectPool<LinkHolder> getHolderPool(int size) {
		if (sHolderPoolMap.containsKey(size)) {
			return sHolderPoolMap.get(size);
		} else {
			logger.info("Creating new Holder pool of size {}", size);
			PoolSettings<LinkHolder> settings = new PoolSettings<LinkHolder>(
					new LinkHolderPoolableObject(size));
			settings.max(-1);
			settings.maxIdle(500000);
			PoolSettings.timeBetweenTwoControls(600);
			ObjectPool<LinkHolder> pool = settings.pool();
			sHolderPoolMap.put(size, pool);
			return pool;
		}
	}

	/*
	 * Szudzik's function. See http://szudzik.com/ElegantPairing.pdf
	 */
	private static int getId(int a, int b) {
		return a >= b ? a * a + a + b : a + b * b;
	}

	/**
	 * QuiverPool should not be instantiated itself.
	 * Just use the getInstance method to get the ObjectPool.
	 */
	private Pools() {
	}
}
