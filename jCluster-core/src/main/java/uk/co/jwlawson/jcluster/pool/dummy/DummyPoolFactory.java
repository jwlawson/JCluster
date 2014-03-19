/**
 * Copyright 2014 John Lawson
 * 
 * DummyPoolFactory.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
package uk.co.jwlawson.jcluster.pool.dummy;

import uk.co.jwlawson.jcluster.data.HolderKey;
import uk.co.jwlawson.jcluster.data.IntMatrixPair;
import uk.co.jwlawson.jcluster.data.LinkHolder;
import uk.co.jwlawson.jcluster.data.QuiverKey;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;
import uk.co.jwlawson.jcluster.pool.PoolFactory;

/**
 * @author John Lawson
 * 
 */
public class DummyPoolFactory implements PoolFactory {

	@Override
	public <T extends QuiverMatrix> Pool<T> createQuiverPool(QuiverKey<T> key) {
		return new DummyMatrixPool<T>(key);
	}

	@Override
	public <T extends QuiverMatrix> Pool<LinkHolder<T>> createLinkHolderPool(HolderKey<T> key) {
		return new DummyHolderPool<T>(key);
	}

	@Override
	public Pool<IntMatrixPair> createIntMatrixPairPool() {
		return new DummyPairPool();
	}

}
