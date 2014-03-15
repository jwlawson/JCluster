/**
 * Copyright 2014 John Lawson
 * 
 * DummyPairPool.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import uk.co.jwlawson.jcluster.IntMatrixPair;
import uk.co.jwlawson.jcluster.pool.Pool;

/**
 * @author John Lawson
 * 
 */
public class DummyPairPool implements Pool<IntMatrixPair> {

	public IntMatrixPair getObj() {
		return new IntMatrixPair();
	}

	public void returnObj(IntMatrixPair obj) {
		obj = null;
	}
}
