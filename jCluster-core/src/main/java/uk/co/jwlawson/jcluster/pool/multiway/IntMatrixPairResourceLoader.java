/**
 * Copyright 2014 John Lawson
 * 
 * IntMatrixPairResourceLoader.java is part of JCluster. Licensed under the Apache License, Version
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
package uk.co.jwlawson.jcluster.pool.multiway;

import uk.co.jwlawson.jcluster.data.IntMatrixPair;

import com.github.benmanes.multiway.ResourceLoader;

/**
 * @author John Lawson
 * 
 */
public class IntMatrixPairResourceLoader implements ResourceLoader<Integer, IntMatrixPair> {

	@Override
	public IntMatrixPair load(Integer key) throws Exception {
		return new IntMatrixPair();
	}

}
