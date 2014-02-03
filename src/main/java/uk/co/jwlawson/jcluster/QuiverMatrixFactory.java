/**
 * Copyright 2014 John Lawson
 * 
 * QuiverMatrixFactory.java is part of JCluster.
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

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class QuiverMatrixFactory extends BasePooledObjectFactory<QuiverMatrix> {

	private static HashMap<Integer, QuiverMatrixFactory> sFactoryMap = new HashMap<Integer, QuiverMatrixFactory>();

	private final int rows;
	private final int cols;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static QuiverMatrixFactory getInstance(int rows, int cols) {
		int id = getId(rows, cols);
		if (sFactoryMap.containsKey(id)) {
			return sFactoryMap.get(id);
		} else {
			QuiverMatrixFactory factory = new QuiverMatrixFactory(rows, cols);
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

	private QuiverMatrixFactory(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.pool2.BasePooledObjectFactory#create()
	 */
	@Override
	public QuiverMatrix create() throws Exception {
		logger.debug("New QuiverMatrix created {}x{}", rows, cols);
		return new QuiverMatrix(rows, cols);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.pool2.BasePooledObjectFactory#wrap(java.lang.Object)
	 */
	@Override
	public PooledObject<QuiverMatrix> wrap(QuiverMatrix arg0) {
		return new DefaultPooledObject<QuiverMatrix>(arg0);
	}

}
