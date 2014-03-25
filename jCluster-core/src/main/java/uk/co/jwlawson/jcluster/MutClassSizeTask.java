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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.LinkHolder;
import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;

/**
 * Base class which finds the number of matrices in the mutation class of the initial matrix. This
 * is fast for small matrices, but larger matrices with larger mutation classes require a huge
 * amount of memory, as each matrix in the class is stored.
 * 
 * @param <T> Type of matrix which is being used in the calculation
 * @author John Lawson
 * 
 */
public class MutClassSizeTask<T extends QuiverMatrix> extends AbstractMutClassSizeTask<T> {

	private final List<NewMatrixSeenListener<T>> mListeners;

	/**
	 * Create a new instance with the specified initial matrix.
	 * 
	 * @param matrix Matrix to calculate the mutation class size of
	 */
	public MutClassSizeTask(final T matrix) {
		super(matrix);
		setIterationsBetweenStats(50000);
		mListeners = new ArrayList<MutClassSizeTask.NewMatrixSeenListener<T>>(2);
		mListeners.add(new NewMatrixLogger());
	}

	public void addNewMatrixListener(NewMatrixSeenListener<T> listener) {
		mListeners.add(listener);
	}

	@Override
	protected MatrixInfo handleResult(final MatrixInfo info, final int result) {
		info.setMutationClassSize(result);
		return info;
	}

	@Override
	protected void removeHandledQuiver(final T remove, final Pool<T> quiverPool,
			final Pool<LinkHolder<T>> holderPool, final Map<T, LinkHolder<T>> matrixSet) {

		LinkHolder<T> holder = matrixSet.remove(remove);
		holderPool.returnObj(holder);
		returnMatrix(remove, quiverPool);
	}

	@Override
	protected void removeComplete(final T remove, final Pool<T> quiverPool,
			final Pool<LinkHolder<T>> holderPool, final Map<T, LinkHolder<T>> matrixSet) {

		LinkHolder<T> holder = matrixSet.remove(remove);
		holderPool.returnObj(holder);
		returnMatrix(remove, quiverPool);
	}

	@Override
	protected void handleUnseenMatrix(final Map<T, LinkHolder<T>> matrixSet,
			final Queue<T> incompleteQuivers, final Pool<LinkHolder<T>> holderPool, final T mat,
			final T newMatrix, final int i) {
		for (NewMatrixSeenListener<T> lis : mListeners) {
			lis.newMatrixSeen(newMatrix);
		}
		incompleteQuivers.add(newMatrix);
		LinkHolder<T> newHolder = holderPool.getObj();
		newHolder.setMatrix(newMatrix);
		newHolder.setLinkAt(i);
		LinkHolder<T> oldHolder = matrixSet.get(mat);
		oldHolder.setLinkAt(i);
		matrixSet.put(newMatrix, newHolder);
	}

	@Override
	protected void handleSeenMatrix(final Map<T, LinkHolder<T>> matrixSet, final T mat,
			final T newMatrix, final int i) {
		LinkHolder<T> newHolder = matrixSet.get(newMatrix);
		newHolder.setLinkAt(i);
		LinkHolder<T> oldHolder = matrixSet.get(mat);
		oldHolder.setLinkAt(i);
	}

	@Override
	protected Map<T, LinkHolder<T>> getMatrixMap(final int size) {
		return new ConcurrentHashMap<T, LinkHolder<T>>((int) Math.pow(2, 3 * size - 3), 0.7f);
	}

	@Override
	protected boolean matrixSeenBefore(final T newMatrix, final Map<T, LinkHolder<T>> matrixSet) {
		return matrixSet.containsKey(newMatrix);
	}

	@Override
	protected void teardown(final Pool<T> quiverPool, final Pool<LinkHolder<T>> holderPool,
			final Map<T, LinkHolder<T>> matrixSet) {

		for (NewMatrixSeenListener<T> lis : mListeners) {
			lis.allMatricesSeen();
		}
		for (T matrix : matrixSet.keySet()) {
			LinkHolder<T> holder = matrixSet.remove(matrix);
			holderPool.returnObj(holder);
			quiverPool.returnObj(matrix);
		}
	}

	/**
	 * Get a callback each time a new matrix is seen in the computation of the mutation class.
	 * 
	 * @author John Lawson
	 * 
	 */
	public interface NewMatrixSeenListener<T extends QuiverMatrix> {

		/**
		 * Called when a new matrix is computed.
		 * 
		 * @param matrix
		 */
		void newMatrixSeen(final T matrix);

		/**
		 * Called once the mutation class is complete and all matrices have been seen.
		 */
		void allMatricesSeen();
	}

	private class NewMatrixLogger implements NewMatrixSeenListener<T> {

		private final Logger log = LoggerFactory.getLogger(MutClassSizeTask.class);

		@Override
		public void newMatrixSeen(T matrix) {
			log.trace("New matrix seen {}", matrix);
		}

		@Override
		public void allMatricesSeen() {
			log.trace("All matrices seen. Mutation class complete.");
		}

	}

	@Override
	public boolean isSubmitting() {
		return false;
	}

	@Override
	public boolean submitsSubmitting() {
		return false;
	}
}
