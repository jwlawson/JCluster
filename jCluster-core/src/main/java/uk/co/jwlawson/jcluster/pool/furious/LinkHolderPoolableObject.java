/**
 * Copyright 2014 John Lawson
 * 
 * LinkHolderPoolableObject.java is part of JCluster. Licensed under the Apache License, Version 2.0
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
package uk.co.jwlawson.jcluster.pool.furious;

import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolableObject;
import nf.fr.eraasoft.pool.PoolableObjectBase;
import uk.co.jwlawson.jcluster.data.LinkHolder;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * {@link PoolableObject} which provides a factory method for new {@link LinkHolder} objects.
 * 
 * @author John Lawson
 * 
 */
public class LinkHolderPoolableObject<T extends QuiverMatrix> extends
		PoolableObjectBase<LinkHolder<T>> {

	private final int mSize;

	public LinkHolderPoolableObject(int size) {
		mSize = size;
	}

	public LinkHolder<T> make() throws PoolException {
		return new LinkHolder<T>(mSize);
	}

	public void activate(LinkHolder<T> holder) throws PoolException {}

	@Override
	public void passivate(LinkHolder<T> holder) {
		holder.clear();
	}

}
