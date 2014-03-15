/**
 * Copyright 2014 John Lawson
 * 
 * FuriousPoolAdaptor.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
import nf.fr.eraasoft.pool.PoolException;
import uk.co.jwlawson.jcluster.pool.Pool;

/**
 * @author John Lawson
 * @param <T>
 * 
 */
public class FuriousPoolAdaptor<T> implements Pool<T> {

	private final ObjectPool<T> pool;

	public FuriousPoolAdaptor(ObjectPool<T> pool) {
		this.pool = pool;
	}

	public T getObj() {
		try {
			return pool.getObj();
		} catch (PoolException e) {
			throw new RuntimeException("Error getting pool object", e);
		}
	}

	public void returnObj(T obj) {
		pool.returnObj(obj);
	}

}
