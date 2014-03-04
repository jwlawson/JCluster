/**
 * Copyright 2014 John Lawson
 * 
 * MutClassSizeTask.java is part of JCluster.
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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import nf.fr.eraasoft.pool.ObjectPool;

/**
 * @author John Lawson
 * 
 */
public class MutClassSizeTask<T extends QuiverMatrix> extends
		AbstractMutClassSizeTask<T> implements Callable<Integer> {

	public MutClassSizeTask(T matrix) {
		super(matrix);
	}

	@Override
	protected Map<T, LinkHolder<T>> getMatrixMap(int size) {
		return new ConcurrentHashMap<T, LinkHolder<T>>((int) Math.pow(2,
				3 * size - 3), 0.7f);
	}

	@Override
	protected boolean matrixSeenBefore(T newMatrix,
			Map<T, LinkHolder<T>> matrixSet) {
		return matrixSet.containsKey(newMatrix);
	}

	@Override
	protected void removeQuiver(T remove, ObjectPool<T> quiverPool,
			ObjectPool<LinkHolder<T>> holderPool,
			Map<T, LinkHolder<T>> mMatrixSet) {
		LinkHolder<T> holder = mMatrixSet.get(remove);
		if (holder != null && holder.isComplete()) {
			T key = holder.getQuiverMatrix();
			holder = mMatrixSet.remove(remove);
			if (key != remove) {
				// So remove.equals(key), but they are not the same object
				quiverPool.returnObj(key);
			}
			holderPool.returnObj(holder);
		}
		quiverPool.returnObj(remove);
	}

	@Override
	protected void teardown(ObjectPool<T> quiverPool,
			ObjectPool<LinkHolder<T>> holderPool,
			Map<T, LinkHolder<T>> matrixSet) {
		// Do nothing
	}

	@Override
	protected ObjectPool<T> getQuiverPool() {
		return Pools
				.getQuiverMatrixPool(getRows(), getCols(), getMatrixClass());
	}

	@Override
	protected ObjectPool<LinkHolder<T>> getHolderPool(int size) {
		return Pools.getHolderPool(size, getMatrixClass());
	}
}
