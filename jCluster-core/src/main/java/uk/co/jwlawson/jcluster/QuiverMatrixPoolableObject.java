/**
 * Copyright 2014 John Lawson
 * 
 * QuiverMatrixPoolableObject.java is part of JCluster. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolableObjectBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object factory required for pooling QuiverMatrix objects.
 * 
 * @author John Lawson
 * 
 */
public class QuiverMatrixPoolableObject<T extends QuiverMatrix> extends PoolableObjectBase<T> {

	private final int rows;
	private final int cols;
	private final Class<T> clazz;
	private Constructor<T> constructor;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Create a new PoolableObject. This provides a factory method to get new objects for the pool.
	 * The class is required to be able to construct the correct class as otherwise the type is erased
	 * and the only class returned is {@link QuiverMatrix}.
	 * 
	 * @param rows Number of rows in each matrix
	 * @param cols Number of columns in each matrix
	 * @param clazz Class to be created
	 */
	public QuiverMatrixPoolableObject(int rows, int cols, Class<T> clazz) {
		this.rows = rows;
		this.cols = cols;
		this.clazz = clazz;

		try {
			constructor = clazz.getConstructor(Integer.TYPE, Integer.TYPE);
		} catch (NoSuchMethodException e) {
			logger.error("No constructor found for class ", e);
			throw new RuntimeException("No constructor found for class " + clazz, e);
		} catch (SecurityException e) {
			logger.error("Cannot access constructor for class ", e);
			throw new RuntimeException("Cannot access constructor for class " + clazz, e);
		}
	}

	public T make() throws PoolException {
		try {
			T inst = constructor.newInstance(rows, cols);
			return inst;
		} catch (InstantiationException e) {
			throw new RuntimeException("Error instantiating class " + clazz, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error instantiating class " + clazz, e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Error instantiating class " + clazz, e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error instantiating class " + clazz, e);
		}
	}

	public void activate(T quiver) throws PoolException {
		quiver.reset();
	}

	@Override
	public void passivate(T quiver) {
		quiver.reset();
	}

}
