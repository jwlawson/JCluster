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
 * @author John Lawson
 * 
 */
public class QuiverPool {

	private final static Logger logger = LoggerFactory.getLogger(QuiverPool.class);
	
	private static HashMap<Integer, ObjectPool<QuiverMatrix>> sObjPoolMap = new HashMap<Integer, ObjectPool<QuiverMatrix>>();

	public static synchronized ObjectPool<QuiverMatrix> getInstance(int rows, int cols) {
		int id = getId(rows, cols);
		if (sObjPoolMap.containsKey(id)) {
			return sObjPoolMap.get(id);
		} else {
			logger.info("Creating new pool {}x{}", rows, cols);
			PoolSettings<QuiverMatrix> settings = new PoolSettings<QuiverMatrix>(QuiverMatrixPoolableObject.getInstance(rows, cols));
			settings.max(Integer.MAX_VALUE);
			ObjectPool<QuiverMatrix> pool = settings.pool();
			sObjPoolMap.put(id, pool);
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
	private QuiverPool(){
	}
}
