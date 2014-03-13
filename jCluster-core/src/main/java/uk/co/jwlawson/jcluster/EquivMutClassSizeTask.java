/**
 * Copyright 2014 John Lawson
 * 
 * EquivMutClassSizeTask.java is part of JCluster. Licensed under the Apache License, Version 2.0
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
package uk.co.jwlawson.jcluster;

import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;

/**
 * Task to find the number of quivers in the mutation class of the initial quiver up to permutations
 * of the vertices of the quivers. This uses much less memory than {@link MutClassSizeTask} but can
 * be slower as the check for equivalence is slow.
 * 
 * @author John Lawson
 * 
 */
public class EquivMutClassSizeTask extends MutClassSizeTask<EquivQuiverMatrix> {

	private final Set<EquivQuiverMatrix> mList;

	public EquivMutClassSizeTask(EquivQuiverMatrix matrix) {
		super(matrix);
		mList = new HashSet<EquivQuiverMatrix>();
		mList.add(matrix);
		setIterationsBetweenStats(100);
	}

	public EquivMutClassSizeTask(QuiverMatrix matrix) {
		this(new EquivQuiverMatrix(matrix));
	}

	@Override
	protected boolean matrixSeenBefore(EquivQuiverMatrix newMatrix,
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet) {
		boolean result = false;
		result = result || mList.contains(newMatrix);
		result = result || super.matrixSeenBefore(newMatrix, matrixSet);
		return result;
	}

	@Override
	protected void handleSeenMatrix(
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet, EquivQuiverMatrix mat,
			EquivQuiverMatrix newMatrix, int i) {
		LinkHolder<EquivQuiverMatrix> newHolder = matrixSet.get(newMatrix);
		LinkHolder<EquivQuiverMatrix> oldHolder = matrixSet.get(mat);
		oldHolder.setLinkAt(i);
		if (newHolder == null) {
			// Matrix seen before, but no longer in map as all links mutated
			return;
		}
		if (IntMatrix.areEqual(newMatrix, newHolder.getQuiverMatrix())) {
			// Matrices are equal, not just equivalent so update the stored matrix link
			newHolder.setLinkAt(i);
		}
	}

	@Override
	protected void checkRemoveQuiver(EquivQuiverMatrix remove,
			ObjectPool<EquivQuiverMatrix> quiverPool,
			ObjectPool<LinkHolder<EquivQuiverMatrix>> holderPool,
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> mMatrixSet) {
		boolean removeQ = true;
		LinkHolder<EquivQuiverMatrix> holder = mMatrixSet.get(remove);
		if (holder != null && holder.isComplete()) {
			EquivQuiverMatrix key = holder.getQuiverMatrix();
			holder = mMatrixSet.remove(remove);
			if (key == remove) {
				removeQ = false;
			}
			holderPool.returnObj(holder);
		}
		if (removeQ) {
			quiverPool.returnObj(remove);
		}
	}

	@Override
	protected void handleUnseenMatrix(
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet,
			Queue<EquivQuiverMatrix> incompleteQuivers,
			ObjectPool<LinkHolder<EquivQuiverMatrix>> holderPool, EquivQuiverMatrix mat,
			EquivQuiverMatrix newMatrix, int i) throws PoolException {
		mList.add(newMatrix);
		super.handleUnseenMatrix(matrixSet, incompleteQuivers, holderPool, mat, newMatrix, i);
	}

	@Override
	protected void teardown(ObjectPool<EquivQuiverMatrix> quiverPool,
			ObjectPool<LinkHolder<EquivQuiverMatrix>> holderPool,
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet) {
		for (EquivQuiverMatrix m : mList) {
			quiverPool.returnObj(m);
		}
		mList.clear();
		super.teardown(quiverPool, holderPool, matrixSet);
	}
}
