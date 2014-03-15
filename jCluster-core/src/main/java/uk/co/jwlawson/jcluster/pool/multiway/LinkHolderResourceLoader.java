/**
 * Copyright 2014 John Lawson
 * 
 * LinkHolderResourceLoader.java is part of JCluster. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster.pool.multiway;

import uk.co.jwlawson.jcluster.HolderKey;
import uk.co.jwlawson.jcluster.LinkHolder;
import uk.co.jwlawson.jcluster.QuiverMatrix;

import com.github.benmanes.multiway.ResourceLoader;

/**
 * @author John Lawson
 * 
 */
public class LinkHolderResourceLoader<T extends QuiverMatrix> implements
		ResourceLoader<HolderKey<T>, LinkHolder<T>> {

	public LinkHolder<T> load(HolderKey<T> key) throws Exception {
		return new LinkHolder<T>(key.getSize());
	}

}
