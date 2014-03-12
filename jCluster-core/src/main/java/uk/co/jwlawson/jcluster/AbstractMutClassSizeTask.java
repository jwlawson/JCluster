/**
 * Copyright 2014 John Lawson
 * 
 * AbstractMutClassSizeTask.java is part of JCluster. Licensed under the Apache License, Version 2.0
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

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMutClassSizeTask<T extends QuiverMatrix> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final T mInitialMatrix;

	public AbstractMutClassSizeTask(T matrix) {
		@SuppressWarnings("unchecked")
		ObjectPool<T> pool =
				Pools.getQuiverMatrixPool(matrix.getNumRows(), matrix.getNumCols(),
						(Class<T>) matrix.getClass());
		T first;
		try {
			first = pool.getObj();
			first.set(matrix);
			mInitialMatrix = first;
		} catch (PoolException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Find the number of matrices in the mutation class of the initial matrix. If the matrix is
	 * mutation-infinite then -1 is returned.
	 * 
	 * @return The size of the mutation class, or -1 if infinite
	 * @throws Exception
	 */
	public Integer call() throws Exception {
		log.debug("MutClassSizeTask started for {}", mInitialMatrix);
		int size = getSize(mInitialMatrix);
		int numMatrices = 0;

		Map<T, LinkHolder<T>> matrixSet = getMatrixMap(size);

		LinkHolder<T> initial = new LinkHolder<T>(getSize(mInitialMatrix));
		initial.setMatrix(mInitialMatrix);
		matrixSet.put(mInitialMatrix, initial);

		Queue<T> incompleteQuivers = new ArrayDeque<T>((int) Math.pow(2, 3 * size - 3));
		incompleteQuivers.add(mInitialMatrix);

		ObjectPool<T> quiverPool = getQuiverPool();
		ObjectPool<LinkHolder<T>> holderPool = getHolderPool(size);
		try {
			T mat;
			T newMatrix;
			int i;
			do {
				mat = incompleteQuivers.poll();
				for (i = 0; i < size; i++) {
					if (shouldMutateAt(mat, i, matrixSet)) {
						newMatrix = quiverPool.getObj();
						newMatrix = mat.mutate(i, newMatrix);
						if (matrixSeenBefore(newMatrix, matrixSet)) {
							handleSeenMatrix(matrixSet, mat, newMatrix, i);
							checkRemoveQuiver(newMatrix, quiverPool, holderPool, matrixSet);
						} else {
							if (newMatrix.isInfinite()) {
								return -1;
							}
							log.debug("Adding new matrix: {}, index {}", newMatrix, i);
							handleUnseenMatrix(matrixSet, incompleteQuivers, holderPool, mat,
									newMatrix, i);
						}
					}
				}
				checkRemoveQuiver(mat, quiverPool, holderPool, matrixSet);
				if (numMatrices % 50000 == 0) {
					log.debug("Handled {} matrices, now at {}, with {} in map.", numMatrices,
							incompleteQuivers.size(), matrixSet.size());
				}
				numMatrices++;
			} while (!incompleteQuivers.isEmpty());
			log.info("Graph completed. Vertices: {}", numMatrices);
			return numMatrices;
		} finally {
			teardown(quiverPool, holderPool, matrixSet);
		}
	}

	/**
	 * Handle matrix not yet seen before in the mutation process.
	 * 
	 * @param matrixSet {@link Map} containing the matrices and which mutations from them have been
	 *        considered
	 * @param incompleteQuivers {@link Queue} containing quivers not yet handled
	 * @param holderPool {@link ObjectPool} providing {@link LinkHolder} objects
	 * @param mat Matrix mutated to get the unseen matrix
	 * @param newMatrix Unseen matrix
	 * @param i Index mutated at to get unseen matrix
	 * @throws PoolException Thrown if there is some problem taking objects from the
	 *         {@link LinkHolder} pool
	 */
	protected abstract void handleUnseenMatrix(Map<T, LinkHolder<T>> matrixSet,
			Queue<T> incompleteQuivers, ObjectPool<LinkHolder<T>> holderPool, T mat, T newMatrix,
			int i) throws PoolException;

	/**
	 * Handle a matrix which has been seen before in the mutation process.
	 * 
	 * @param matrixSet {@link Map} containing the matrices and which mutations from them have been
	 *        considered
	 * @param mat Matrix mutated to get the seen before matrix
	 * @param newMatrix Matrix which has been seen before
	 * @param i Index mutated at to get to newMatrix
	 */
	protected abstract void handleSeenMatrix(Map<T, LinkHolder<T>> matrixSet, T mat, T newMatrix,
			int i);

	/**
	 * Get the {@link ObjectPool} which provides {@link QuiverMatrix} objects of type T.
	 * 
	 * @return Pool of quiver objects
	 */
	protected ObjectPool<T> getQuiverPool() {
		return Pools.getQuiverMatrixPool(getRows(), getCols(), getMatrixClass());
	}

	/**
	 * Get the {@link ObjectPool} which provides {@link LinkHolder} objects which expect matrices of
	 * Type T.
	 * 
	 * @param size Number of links in each {@link LinkHolder}
	 * @return Pool of {@link LinkHolder} objects
	 */
	protected ObjectPool<LinkHolder<T>> getHolderPool(int size) {
		return Pools.getHolderPool(size, getMatrixClass());
	}

	/**
	 * Convenience method to get the number of unfrozen vertices in the quiver.
	 * 
	 * @param matrix Matrix to get the size of
	 * @return Number of unfrozen vertices in the quiver
	 */
	private int getSize(QuiverMatrix matrix) {
		return Math.min(matrix.getNumRows(), matrix.getNumCols());
	}

	/**
	 * Get the {@link Type} of {@link QuiverMatrix} which is being used.
	 * 
	 * @return the {@link Type} of {@link QuiverMatrix}
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> getMatrixClass() {
		return (Class<T>) mInitialMatrix.getClass();
	}

	protected int getRows() {
		return mInitialMatrix.getNumRows();
	}

	protected int getCols() {
		return mInitialMatrix.getNumCols();
	}

	/**
	 * Get the {@link Map} which holds the {@link QuiverMatrix} objects which have been seen before
	 * and the {@link LinkHolder} associated to them.
	 * 
	 * @param size Size of the matrices which will be stored in the map
	 * @return Map between matrices and link holders
	 */
	protected abstract Map<T, LinkHolder<T>> getMatrixMap(int size);

	/**
	 * Check whether the matrix has been seen before in the mutation process.
	 * 
	 * @param newMatrix Matrix to check
	 * @param matrixSet Map containing the seen matrices which are currently incomplete
	 * @return true if the matrix has been seen before
	 */
	protected abstract boolean matrixSeenBefore(T newMatrix, Map<T, LinkHolder<T>> matrixSet);

	/**
	 * Check whether the matrix should be removed from the map. If so it is removed and returned to
	 * the pools.
	 * 
	 * @param remove Matrix to check
	 * @param quiverPool Pool to return the quiver to
	 * @param holderPool Pool to return the link holder to
	 * @param matrixSet Map to remove the quiver from
	 */
	protected abstract void checkRemoveQuiver(T remove, ObjectPool<T> quiverPool,
			ObjectPool<LinkHolder<T>> holderPool, Map<T, LinkHolder<T>> matrixSet);

	/**
	 * Called after the calculation is complete. Can be used to clean up and return objects to their
	 * pools.
	 * 
	 * @param quiverPool Pool of quiver objects
	 * @param holderPool Pool of link holder objects
	 * @param matrixSet Map containing quivers and holders
	 */
	protected abstract void teardown(ObjectPool<T> quiverPool,
			ObjectPool<LinkHolder<T>> holderPool, Map<T, LinkHolder<T>> matrixSet);

	/**
	 * Check whether we need to mutate the vertex at the specified index, or whether that mutation
	 * has already been considered.
	 * 
	 * @param matrix Matrix to check
	 * @param i Index to mutate at
	 * @param matrixSet Map containing {@link LinkHolder} objects
	 * @return true if should mutate at the index
	 */
	private boolean shouldMutateAt(T matrix, int i, Map<T, LinkHolder<T>> matrixSet) {
		LinkHolder<T> holder = matrixSet.get(matrix);
		return holder != null && !holder.hasLink(i);
	}

}
