/**
 * Copyright 2014 John Lawson
 * 
 * EquivMutClassSizeTask.java is part of JCluster.
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

import nf.fr.eraasoft.pool.ObjectPool;

/**
 * @author John Lawson
 * 
 */
public class EquivMutClassSizeTask extends MutClassSizeTask<EquivQuiverMatrix> {

	public EquivMutClassSizeTask(EquivQuiverMatrix matrix) {
		super(matrix);
	}

	public EquivMutClassSizeTask(QuiverMatrix matrix) {
		this(new EquivQuiverMatrix(matrix));
	}

	@Override
	protected ObjectPool<LinkHolder<EquivQuiverMatrix>> getHolderPool(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ObjectPool<EquivQuiverMatrix> getQuiverPool() {
		// TODO Auto-generated method stub
		return null;
	}

}
