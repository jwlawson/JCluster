/**
 * Copyright 2014 John Lawson
 * 
 * LinkHolderPoolableObject.java is part of JCluster.
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

import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolableObjectBase;

/**
 * @author John Lawson
 *
 */
public class LinkHolderPoolableObject extends PoolableObjectBase<LinkHolder> {
	
	private final int mSize;
	
	public LinkHolderPoolableObject(int size) {
		mSize = size;
	}

	public LinkHolder make() throws PoolException {
		return new LinkHolder(mSize);
	}

	public void activate(LinkHolder holder) throws PoolException {
	}
	
	@Override
	public void passivate(LinkHolder holder) {
		holder.clear();
	}

}
