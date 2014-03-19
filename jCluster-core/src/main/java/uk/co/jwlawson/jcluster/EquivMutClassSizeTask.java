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

import uk.co.jwlawson.jcluster.data.EquivQuiverMatrix;
import uk.co.jwlawson.jcluster.data.IntMatrix;
import uk.co.jwlawson.jcluster.data.LinkHolder;
import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;

/**
 * Task to find the number of quivers in the mutation class of the initial quiver up to permutations
 * of the vertices of the quivers. This uses much less memory than {@link MutClassSizeTask} but can
 * be slower as the check for equivalence is slow.
 * 
 * @author John Lawson
 * 
 */
public class EquivMutClassSizeTask extends MutClassSizeTask<EquivQuiverMatrix> {

	/** Set of all seen matrices. */
	private final Set<EquivQuiverMatrix> mList;

	/**
	 * Create a new task to find the mutation class size up to reordering rows and columns of the
	 * provided matrix.
	 * 
	 * @param matrix Initial matrix to find the mutation class of
	 */
	public EquivMutClassSizeTask(final EquivQuiverMatrix matrix) {
		super(matrix);
		mList = new HashSet<EquivQuiverMatrix>();
		setIterationsBetweenStats(100);
	}

	/**
	 * Create a new task to find the mutation class size up to reordering rows and columns of the
	 * provided matrix.
	 * 
	 * @param matrix Initial matrix to find the mutation class of
	 */
	public EquivMutClassSizeTask(final QuiverMatrix matrix) {
		this(new EquivQuiverMatrix(matrix));
	}

	@Override
	protected void setUp(final EquivQuiverMatrix m) {
		super.setUp(m);
		mList.add(m);
	}

	@Override
	public void reset() {
		super.reset();
		mList.clear();
	}

	@Override
	protected MatrixInfo handleResult(final MatrixInfo info, final int result) {
		info.setEquivMutationClassSize(result);
		return info;
	}

	@Override
	protected boolean matrixSeenBefore(final EquivQuiverMatrix newMatrix,
			final Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet) {
		return mList.contains(newMatrix) || super.matrixSeenBefore(newMatrix, matrixSet);
	}

	@Override
	protected void handleSeenMatrix(
			final Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet,
			final EquivQuiverMatrix mat, final EquivQuiverMatrix newMatrix, final int i) {
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
	protected void removeHandledQuiver(final EquivQuiverMatrix remove,
			final Pool<EquivQuiverMatrix> quiverPool,
			final Pool<LinkHolder<EquivQuiverMatrix>> holderPool,
			final Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet) {

		LinkHolder<EquivQuiverMatrix> holder = matrixSet.remove(remove);
		holderPool.returnObj(holder);
	}

	@Override
	protected void removeComplete(final EquivQuiverMatrix remove,
			final Pool<EquivQuiverMatrix> quiverPool,
			final Pool<LinkHolder<EquivQuiverMatrix>> holderPool,
			final Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet) {

		LinkHolder<EquivQuiverMatrix> holder = matrixSet.remove(remove);
		EquivQuiverMatrix key = holder.getQuiverMatrix();
		holderPool.returnObj(holder);
		// TODO Check if necessary to check this.
		// I think this always runs
		if (key != remove) {
			quiverPool.returnObj(remove);
		}
	}

	@Override
	protected void handleUnseenMatrix(
			final Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet,
			final Queue<EquivQuiverMatrix> incompleteQuivers,
			final Pool<LinkHolder<EquivQuiverMatrix>> holderPool, final EquivQuiverMatrix mat,
			final EquivQuiverMatrix newMatrix, final int i) {

		mList.add(newMatrix);
		super.handleUnseenMatrix(matrixSet, incompleteQuivers, holderPool, mat, newMatrix, i);
	}

	@Override
	protected void teardown(final Pool<EquivQuiverMatrix> quiverPool,
			final Pool<LinkHolder<EquivQuiverMatrix>> holderPool,
			final Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet) {

		for (EquivQuiverMatrix m : mList) {
			quiverPool.returnObj(m);
		}
		mList.clear();
		super.teardown(quiverPool, holderPool, matrixSet);
	}
}
