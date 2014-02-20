/**
 * 
 */
package uk.co.jwlawson.jcluster;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import nf.fr.eraasoft.pool.ObjectPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John
 * 
 */
public class NewFiniteTask implements Callable<Integer> {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final QuiverMatrix mInitialMatrix;

	public NewFiniteTask(QuiverMatrix matrix) {
		mInitialMatrix = matrix;
	}

	private int getSize(QuiverMatrix matrix) {
		return Math.min(matrix.getNumRows(), matrix.getNumCols());
	}

	public Integer call() throws Exception {
		log.debug("NewFiniteTask started for {}", mInitialMatrix);
		int size = getSize(mInitialMatrix);
		int numMatrices = 0;

		Map<QuiverMatrix, LinkHolder> matrixSet = new ConcurrentHashMap<QuiverMatrix, LinkHolder>(
				(int) Math.pow(2, 3 * size - 3), 0.7f);

		LinkHolder initial = new LinkHolder(getSize(mInitialMatrix));
		matrixSet.put(mInitialMatrix, initial);

		Queue<QuiverMatrix> incompleteQuivers = new ArrayDeque<QuiverMatrix>(
				(int) Math.pow(2, 3 * size - 3));
		incompleteQuivers.add(mInitialMatrix);

		ObjectPool<QuiverMatrix> quiverPool = Pools.getQuiverMatrixPool(
				mInitialMatrix.getNumRows(), mInitialMatrix.getNumCols());
		ObjectPool<LinkHolder> holderPool = Pools.getHolderPool(size);

		LinkHolder newHolder;
		LinkHolder oldHolder;
		QuiverMatrix mat;
		QuiverMatrix newMatrix;
		int i;
		do {
			mat = incompleteQuivers.poll();
			for (i = 0; i < size; i++) {
				if (shouldMutateAt(matrixSet, mat, i)) {
					newMatrix = quiverPool.getObj();
					newMatrix = mat.mutate(i, newMatrix);
					if (matrixSet.containsKey(newMatrix)) {
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
				log.debug("Handled {} matrices, now at {}, with {} in map.",
						numMatrices, incompleteQuivers.size(), matrixSet.size());
			}
			numMatrices++;
		} while (!incompleteQuivers.isEmpty());
		log.info("Graph completed. Vertices: {}", numMatrices);
		return numMatrices;
	}

	private void removeQuiver(QuiverMatrix remove,
			ObjectPool<QuiverMatrix> quiverPool,
			ObjectPool<LinkHolder> holderPool,
			Map<QuiverMatrix, LinkHolder> mMatrixSet) {
		LinkHolder holder = mMatrixSet.get(remove);
		if (holder != null && holder.isComplete()) {
			QuiverMatrix key = holder.getQuiverMatrix();
			holder = mMatrixSet.remove(remove);
			if (key != remove) {
				// So remove.equals(key), but they are not the same object
				quiverPool.returnObj(key);
			}
			holderPool.returnObj(holder);
		}
		quiverPool.returnObj(remove);
	}

	private boolean shouldMutateAt(Map<QuiverMatrix, LinkHolder> matrixSet,
			QuiverMatrix matrix, int i) {
		LinkHolder holder = matrixSet.get(matrix);
		return holder != null && !holder.hasLink(i);
	}

}
