/**
 * 
 */
package uk.co.jwlawson.jcluster;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	private final Map<QuiverMatrix, LinkHolder> mMatrixSet;
	private final QuiverMatrix mInitialMatrix;
	private final List<QuiverMatrix>[] mNewVerticesArr;
	private int mCount;
	private int mNumMatrices;
	
	public NewFiniteTask(QuiverMatrix matrix) {
		log.debug("Set up task to check if finite for {}", matrix);
		mInitialMatrix = matrix;
		
		mMatrixSet = new ConcurrentHashMap<QuiverMatrix, LinkHolder>();
		
		LinkHolder initial = new LinkHolder(getSize(matrix));
		mMatrixSet.put(mInitialMatrix, initial);
		
		mNewVerticesArr = new List[2];
		mCount = 0;
		mNumMatrices = 1;
	}

	private int getSize(QuiverMatrix matrix) {
		return Math.min(matrix.getNumRows(), matrix.getNumCols());
	}

	public Integer call() throws Exception {
		log.debug("NewFiniteTask started for {}", mInitialMatrix);
		for(int i = 0; i < 2; i++){
			mNewVerticesArr[i] = new LinkedList<QuiverMatrix>();
		}
		mNewVerticesArr[0].add(mInitialMatrix);
		int size = Math.min(mInitialMatrix.getNumRows(),mInitialMatrix.getNumCols());
		ObjectPool<QuiverMatrix> quiverPool = Pools.getQuiverMatrixPool(mInitialMatrix.getNumRows(),
				mInitialMatrix.getNumCols());
		ObjectPool<LinkHolder> holderPool = Pools.getHolderPool(size);
		int readIndex;
		int addIndex;
		int counter;
		List<QuiverMatrix> possibleRemove = new ArrayList<QuiverMatrix>();
		LinkHolder newHolder;
		LinkHolder oldHolder;
		QuiverMatrix mat;
		QuiverMatrix newMatrix;
		int i;
		int matrixIndex;
		do {
			readIndex = mCount % 2;
			addIndex = (++mCount) % 2;
			mNewVerticesArr[addIndex].clear();
			counter = 0;
			while(!mNewVerticesArr[readIndex].isEmpty()){
				matrixIndex = 0;
				mat = mNewVerticesArr[readIndex].remove(matrixIndex);
				for (i = 0; i < size; i++) {
					if (shouldMutateAt(mMatrixSet, mat, i)) {
						newMatrix = quiverPool.getObj();
						newMatrix = mat.mutate(i, newMatrix);
						if (mMatrixSet.containsKey(newMatrix)) {
							newHolder = mMatrixSet.get(newMatrix);
							possibleRemove.add(newMatrix);
						} else {
							mNewVerticesArr[addIndex].add(newMatrix);
							newHolder = holderPool.getObj();
							mMatrixSet.put(newMatrix, newHolder);
						}
						newHolder.setLinkAt(i, mat);
						oldHolder = mMatrixSet.get(mat);
						oldHolder.setLinkAt(i, newMatrix);
					}
				}
				removeUnneeded(quiverPool, holderPool, possibleRemove);
				removeQuiver(mat, quiverPool, holderPool);
				if(counter % 50000 == 0 && counter != 0){
					log.debug("Handled {} matrices, now at {}, with {} in map. quiverpool {}. holderpool {}.", counter, 
							mNumMatrices + mNewVerticesArr[addIndex].size(), 
							mMatrixSet.size(), quiverPool.toString(), holderPool.toString());
				}
				counter++;
			}
			mNumMatrices += mNewVerticesArr[addIndex].size();
			log.debug("Added {} vertices, now at {}, with {} in map", 
					mNewVerticesArr[addIndex].size(), mNumMatrices, mMatrixSet.size());
		} while (!mNewVerticesArr[addIndex].isEmpty());
		log.info("Graph completed. Vertices: {}", mNumMatrices );
		return mNumMatrices;
	}

	private void removeUnneeded(ObjectPool<QuiverMatrix> quiverPool,
			ObjectPool<LinkHolder> holderPool,
			List<QuiverMatrix> possibleRemove) {
		for(int i = 0; i < possibleRemove.size(); i++){
			QuiverMatrix remove = possibleRemove.remove(i);
			removeQuiver(remove, quiverPool, holderPool);
		}
	}

	private void removeQuiver(QuiverMatrix remove,
			ObjectPool<QuiverMatrix> quiverPool,
			ObjectPool<LinkHolder> holderPool) {
		LinkHolder holder = mMatrixSet.get(remove);
		if(holder != null && holder.isComplete()){
			mMatrixSet.remove(remove);
			quiverPool.returnObj(remove);
			holderPool.returnObj(holder);
		}
	}

	private boolean shouldMutateAt(
			Map<QuiverMatrix, LinkHolder> matrixSet,
			QuiverMatrix matrix, int i) {
		LinkHolder holder = matrixSet.get(matrix);
		return holder != null && !holder.hasLink(i);
	}

}
