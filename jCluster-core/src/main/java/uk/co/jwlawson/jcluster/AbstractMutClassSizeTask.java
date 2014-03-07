package uk.co.jwlawson.jcluster;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import nf.fr.eraasoft.pool.ObjectPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMutClassSizeTask<T extends QuiverMatrix> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final T mInitialMatrix;

	public AbstractMutClassSizeTask(T matrix) {
		mInitialMatrix = matrix;
	}

	public Integer call() throws Exception {
		log.debug("MutClassSizeTask started for {}", mInitialMatrix);
		int size = getSize(mInitialMatrix);
		int numMatrices = 0;

		Map<T, LinkHolder<T>> matrixSet = getMatrixMap(size);

		LinkHolder<T> initial = new LinkHolder<T>(getSize(mInitialMatrix));
		matrixSet.put(mInitialMatrix, initial);

		Queue<T> incompleteQuivers = new ArrayDeque<T>((int) Math.pow(2,
				3 * size - 3));
		incompleteQuivers.add(mInitialMatrix);

		ObjectPool<T> quiverPool = getQuiverPool();
		ObjectPool<LinkHolder<T>> holderPool = getHolderPool(size);
		try {
			LinkHolder<T> newHolder;
			LinkHolder<T> oldHolder;
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
							newHolder = matrixSet.get(newMatrix);
							removeQuiver(newMatrix, quiverPool, holderPool,
									matrixSet);
						} else {
							incompleteQuivers.add(newMatrix);
							newHolder = holderPool.getObj();
							newHolder.setMatrix(newMatrix);
							matrixSet.put(newMatrix, newHolder);
						}
						newHolder.setLinkAt(i);
						oldHolder = matrixSet.get(mat);
						oldHolder.setLinkAt(i);
					}
				}
				removeQuiver(mat, quiverPool, holderPool, matrixSet);
				if (numMatrices % 50000 == 0) {
					log.debug(
							"Handled {} matrices, now at {}, with {} in map.",
							numMatrices, incompleteQuivers.size(),
							matrixSet.size());
				}
				numMatrices++;
			} while (!incompleteQuivers.isEmpty());
			log.info("Graph completed. Vertices: {}", numMatrices);
			return numMatrices;
		} finally {
			teardown(quiverPool, holderPool, matrixSet);
		}
	}

	protected abstract ObjectPool<LinkHolder<T>> getHolderPool(int size);

	protected abstract ObjectPool<T> getQuiverPool();

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

	protected abstract boolean matrixSeenBefore(T newMatrix,
			Map<T, LinkHolder<T>> matrixSet);

	protected abstract void removeQuiver(T remove, ObjectPool<T> quiverPool,
			ObjectPool<LinkHolder<T>> holderPool,
			Map<T, LinkHolder<T>> matrixSet);

	protected abstract void teardown(ObjectPool<T> quiverPool,
			ObjectPool<LinkHolder<T>> holderPool,
			Map<T, LinkHolder<T>> matrixSet);

	private boolean shouldMutateAt(T matrix, int i,
			Map<T, LinkHolder<T>> matrixSet) {
		LinkHolder<T> holder = matrixSet.get(matrix);
		return holder != null && !holder.hasLink(i);
	}

}
