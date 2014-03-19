/**
 * Copyright 2014 John Lawson
 * 
 * IntMatrixPairPoolableObject.java is part of JCluster. Licensed under the Apache License, Version
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
package uk.co.jwlawson.jcluster.pool.furious;

import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolableObject;
import nf.fr.eraasoft.pool.PoolableObjectBase;
import uk.co.jwlawson.jcluster.data.IntMatrixPair;

/**
 * {@link PoolableObject} which provides a factory method for creating new {@link IntMatrixPair}
 * objects.
 * 
 * @author John Lawson
 * 
 */
public class IntMatrixPairPoolableObject extends PoolableObjectBase<IntMatrixPair> {

	public IntMatrixPair make() throws PoolException {
		return new IntMatrixPair();
	}

	public void activate(IntMatrixPair t) throws PoolException {
		t.reset();
	}

}
