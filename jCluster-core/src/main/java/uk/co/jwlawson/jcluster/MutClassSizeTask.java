/**
 * Copyright 2014 John Lawson
 * 
 * MutClassSizeTask.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
package uk.co.jwlawson.jcluster;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import uk.co.jwlawson.jcluster.pool.Pool;
import nf.fr.eraasoft.pool.PoolException;

/**
 * Base class which finds the number of matrices in the mutation class of the initial matrix. This
 * is fast for small matrices, but larger matrices with larger mutation classes require a huge
 * amount of memory, as each matrix in the class is stored.
 * 
 * @author John Lawson
 * 
 */
public class MutClassSizeTask<T extends QuiverMatrix> extends AbstractMutClassSizeTask<T> implements
		Callable<Integer> {

	public MutClassSizeTask(T matrix) {
		super(matrix);
		setIterationsBetweenStats(50000);
	}

	@Override
	protected void handleUnseenMatrix(Map<T, LinkHolder<T>> matrixSet, Queue<T> incompleteQuivers,
			Pool<LinkHolder<T>> holderPool, T mat, T newMatrix, int i) throws PoolException {
		incompleteQuivers.add(newMatrix);
		LinkHolder<T> newHolder = holderPool.getObj();
		newHolder.setMatrix(newMatrix);
		newHolder.setLinkAt(i);
		LinkHolder<T> oldHolder = matrixSet.get(mat);
		oldHolder.setLinkAt(i);
		matrixSet.put(newMatrix, newHolder);
	}

	@Override
	protected void handleSeenMatrix(Map<T, LinkHolder<T>> matrixSet, T mat, T newMatrix, int i) {
		LinkHolder<T> newHolder = matrixSet.get(newMatrix);
		newHolder.setLinkAt(i);
		LinkHolder<T> oldHolder = matrixSet.get(mat);
		oldHolder.setLinkAt(i);
	}

	@Override
	protected Map<T, LinkHolder<T>> getMatrixMap(int size) {
		return new ConcurrentHashMap<T, LinkHolder<T>>((int) Math.pow(2, 3 * size - 3), 0.7f);
	}

	@Override
	protected boolean matrixSeenBefore(T newMatrix, Map<T, LinkHolder<T>> matrixSet) {
		return matrixSet.containsKey(newMatrix);
	}

	@Override
	protected void teardown(Pool<T> quiverPool, Pool<LinkHolder<T>> holderPool,
			Map<T, LinkHolder<T>> matrixSet) {

		for (T matrix : matrixSet.keySet()) {
			LinkHolder<T> holder = matrixSet.remove(matrix);
			holderPool.returnObj(holder);
			quiverPool.returnObj(matrix);
		}
	}
}
