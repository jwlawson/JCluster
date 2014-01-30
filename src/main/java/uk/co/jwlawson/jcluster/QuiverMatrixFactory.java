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

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author John Lawson
 * 
 */
public class QuiverMatrixFactory extends BasePooledObjectFactory<QuiverMatrix> {

	private int rows;
	private int cols;

	public QuiverMatrixFactory(int rows, int cols) {
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
