/**
 * Copyright 2014 John Lawson
 * 
 * MultiwayPoolAdaptor.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import uk.co.jwlawson.jcluster.Pools.QuiverKey;

import com.github.benmanes.multiway.LoadingMultiwayPool;

/**
 * @author John Lawson
 * 
 */
public class MultiwayPoolAdaptor<T extends QuiverMatrix> implements Pool<T> {

	private final LoadingMultiwayPool<QuiverKey<T>, T> pool;
	private final QuiverKey<T> key;

	public MultiwayPoolAdaptor(LoadingMultiwayPool<QuiverKey<T>, T> pool, QuiverKey<T> key) {
		this.pool = pool;
		this.key = key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.co.jwlawson.jcluster.Pool#getObj()
	 */
	public T getObj() {
		System.out.println("Taking object");
		return pool.borrow(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.co.jwlawson.jcluster.Pool#returnObj(java.lang.Object)
	 */
	public void returnObj(T obj) {
		obj.reset();
		try {
			pool.release(obj);
		} catch (IllegalArgumentException e) {
			System.out.println("Error Returning " + obj);
			throw new RuntimeException(e);
		}
		System.out.println("Returned " + obj);
	}

}
