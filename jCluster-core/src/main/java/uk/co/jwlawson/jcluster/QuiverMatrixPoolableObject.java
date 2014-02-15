/**
 * Copyright 2014 John Lawson
 * 
 * QuiverMatrixPoolableObject.java is part of JCluster.
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

import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolableObjectBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object factory required for pooling QuiverMatrix objects.
 * 
 * @author John
 *
 */
public class QuiverMatrixPoolableObject extends PoolableObjectBase<QuiverMatrix> {
	private static HashMap<Integer, QuiverMatrixPoolableObject> sFactoryMap = new HashMap<Integer, QuiverMatrixPoolableObject>();

	private final int rows;
	private final int cols;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public synchronized static QuiverMatrixPoolableObject getInstance(int rows, int cols) {
		int id = getId(rows, cols);
		if (sFactoryMap.containsKey(id)) {
			return sFactoryMap.get(id);
		} else {
			QuiverMatrixPoolableObject factory = new QuiverMatrixPoolableObject(rows, cols);
			sFactoryMap.put(id, factory);
			return factory;
		}
	}

	/*
	 * Szudzik's function. See http://szudzik.com/ElegantPairing.pdf
	 */
	private static int getId(int a, int b) {
		return a >= b ? a * a + a + b : a + b * b;
	}

	private QuiverMatrixPoolableObject(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
	}

	public QuiverMatrix make() throws PoolException {
		logger.debug("New QuiverMatrix created {}x{}", rows, cols);
		return new QuiverMatrix(rows, cols);
	}

	public void activate(QuiverMatrix arg0) throws PoolException {
		
	}
	
	@Override
	public void passivate(QuiverMatrix quiver) {
		quiver.reset();
	}

}
