package uk.co.jwlawson.jcluster;

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
							try {
//								log.debug("Matrix seen before {}, index {}, holder:{}", newMatrix,
//										i, matrixSet.get(newMatrix));
								handleSeenMatrix(matrixSet, mat, newMatrix, i);
							} catch (RuntimeException e) {
								log.error("seen matrix", e);
							}
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

	protected abstract void handleUnseenMatrix(Map<T, LinkHolder<T>> matrixSet,
			Queue<T> incompleteQuivers, ObjectPool<LinkHolder<T>> holderPool, T mat, T newMatrix,
			int i) throws PoolException;

	protected abstract void handleSeenMatrix(Map<T, LinkHolder<T>> matrixSet, T mat, T newMatrix,
			int i);

	protected ObjectPool<T> getQuiverPool() {
		return Pools.getQuiverMatrixPool(getRows(), getCols(), getMatrixClass());
	}

	protected ObjectPool<LinkHolder<T>> getHolderPool(int size) {
		return Pools.getHolderPool(size, getMatrixClass());
	}

	private int getSize(QuiverMatrix matrix) {
		return Math.min(matrix.getNumRows(), matrix.getNumCols());
	}

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

	protected abstract Map<T, LinkHolder<T>> getMatrixMap(int size);

	protected abstract boolean matrixSeenBefore(T newMatrix, Map<T, LinkHolder<T>> matrixSet);

	protected abstract void checkRemoveQuiver(T remove, ObjectPool<T> quiverPool,
			ObjectPool<LinkHolder<T>> holderPool, Map<T, LinkHolder<T>> matrixSet);

	protected abstract void teardown(ObjectPool<T> quiverPool,
			ObjectPool<LinkHolder<T>> holderPool, Map<T, LinkHolder<T>> matrixSet);

	private boolean shouldMutateAt(T matrix, int i, Map<T, LinkHolder<T>> matrixSet) {
		LinkHolder<T> holder = matrixSet.get(matrix);
		return holder != null && !holder.hasLink(i);
	}

}
