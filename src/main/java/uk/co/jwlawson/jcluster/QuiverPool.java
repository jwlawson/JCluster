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

import nf.fr.eraasoft.pool.PoolSettings;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class QuiverPool {

	private static HashMap<Integer, ObjectPool<QuiverMatrix>> sPoolMap = new HashMap<Integer, ObjectPool<QuiverMatrix>>();

	private final static Logger logger = LoggerFactory.getLogger(QuiverPool.class);

	public static synchronized ObjectPool<QuiverMatrix> getInstance(int rows, int cols) {
		int id = getId(rows, cols);
		if (sPoolMap.containsKey(id)) {
			return sPoolMap.get(id);
		} else {
			logger.info("Creating new pool {}x{}", rows, cols);
			ObjectPool<QuiverMatrix> pool = new SoftReferenceObjectPool<QuiverMatrix>(
					QuiverMatrixFactory.getInstance(rows, cols));
			sPoolMap.put(id, pool);
			return pool;
		}
	}
	
	private static HashMap<Integer, nf.fr.eraasoft.pool.ObjectPool<QuiverMatrix>> sObjPoolMap = new HashMap<Integer, nf.fr.eraasoft.pool.ObjectPool<QuiverMatrix>>();

	public static synchronized nf.fr.eraasoft.pool.ObjectPool<QuiverMatrix> getPool(int rows, int cols) {
		int id = getId(rows, cols);
		if (sObjPoolMap.containsKey(id)) {
			return sObjPoolMap.get(id);
		} else {
			logger.info("Creating new pool {}x{}", rows, cols);
			PoolSettings<QuiverMatrix> settings = new PoolSettings<QuiverMatrix>(QuiverMatrixPoolableObject.getInstance(rows, cols));
			nf.fr.eraasoft.pool.ObjectPool<QuiverMatrix> pool = settings.pool();
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

}
