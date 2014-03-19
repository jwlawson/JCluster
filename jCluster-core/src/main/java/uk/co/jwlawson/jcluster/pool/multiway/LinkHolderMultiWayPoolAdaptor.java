/**
 * Copyright 2014 John Lawson
 * 
 * LinkHolderMultiWayPoolAdaptor.java is part of JCluster. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster.pool.multiway;

import uk.co.jwlawson.jcluster.data.HolderKey;
import uk.co.jwlawson.jcluster.data.LinkHolder;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;

import com.github.benmanes.multiway.LoadingMultiwayPool;

/**
 * @author John Lawson
 * 
 */
public class LinkHolderMultiWayPoolAdaptor<T extends QuiverMatrix> implements Pool<LinkHolder<T>> {

	private final LoadingMultiwayPool<HolderKey<T>, LinkHolder<T>> pool;
	private final HolderKey<T> key;

	public LinkHolderMultiWayPoolAdaptor(LoadingMultiwayPool<HolderKey<T>, LinkHolder<T>> pool,
			HolderKey<T> key) {
		this.pool = pool;
		this.key = key;
	}

	public LinkHolder<T> getObj() {
		return pool.borrow(key);
	}

	public void returnObj(LinkHolder<T> obj) {
		if (obj == null) {
			// obj could be null when the matrix is complete and removed from the map and then handled
			// later.
			return;
		}
		obj.clear();
		try {
			pool.release(obj);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("LinkHolder " + obj + " not taken from pool in this thread", e);
		}
	}

}
