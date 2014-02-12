/**
 * 
 */
package uk.co.jwlawson.jcluster;

import java.util.ArrayList;
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
public class NewFiniteTask implements Callable<QuiverMatrix> {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Map<QuiverMatrix, LinkableQuiverMatrixHolder> mMatrixSet;
	private final QuiverMatrix mInitialMatrix;
	private final List<QuiverMatrix>[] mNewVerticesArr;
	private int mCount;
	private int mNumMatrices;
	
	public NewFiniteTask(QuiverMatrix matrix) {
		log.debug("Set up task to check if finite for {}", matrix);
		mInitialMatrix = matrix;
		
		mMatrixSet = new ConcurrentHashMap<QuiverMatrix, LinkableQuiverMatrixHolder>();
		
		LinkableQuiverMatrixHolder initial = new LinkableQuiverMatrixHolder(getSize(matrix));
		mMatrixSet.put(mInitialMatrix, initial);
		
		mNewVerticesArr = new List[2];
		mCount = 0;
		mNumMatrices = 1;
	}

	private int getSize(QuiverMatrix matrix) {
		return Math.min(matrix.getNumRows(), matrix.getNumCols());
	}

	public QuiverMatrix call() throws Exception {
		log.debug("NewFiniteTask started for {}", mInitialMatrix);
		for(int i = 0; i < 2; i++){
			mNewVerticesArr[i] = new ArrayList<QuiverMatrix>();
		}
		mNewVerticesArr[0].add(mInitialMatrix);
		int size = Math.min(mInitialMatrix.getNumRows(),mInitialMatrix.getNumCols());
		ObjectPool<QuiverMatrix> quiverPool = QuiverPool.getInstance(mInitialMatrix.getNumRows(),
				mInitialMatrix.getNumCols());
		int readIndex;
		int addIndex;
		List<QuiverMatrix> possibleRemove = new ArrayList<QuiverMatrix>();
		do {
			readIndex = mCount % 2;
			addIndex = (++mCount) % 2;
			mNewVerticesArr[addIndex].clear();
			
			for (QuiverMatrix mat : mNewVerticesArr[readIndex]) {
				for (int i = 0; i < size; i++) {
					if (shouldMutateAt(mMatrixSet, mat, i)) {
						QuiverMatrix newMatrix = mat.mutate(i, quiverPool.getObj());
						LinkableQuiverMatrixHolder newHolder;
						if (mMatrixSet.containsKey(newMatrix)) {
							newHolder = mMatrixSet.get(newMatrix);
							possibleRemove.add(newMatrix);
						} else {
							mNewVerticesArr[addIndex].add(newMatrix);
							newHolder = new LinkableQuiverMatrixHolder(getSize(newMatrix));
							mMatrixSet.put(newMatrix, newHolder);
						}
						newHolder.setLinkAt(i, mat);
						LinkableQuiverMatrixHolder oldHolder = mMatrixSet.get(mat);
						oldHolder.setLinkAt(i, newMatrix);
					}
				}
				for(QuiverMatrix remove : possibleRemove){
					LinkableQuiverMatrixHolder holder = mMatrixSet.get(remove);
					if(holder.isComplete()){
						mMatrixSet.remove(remove);
						quiverPool.returnObj(remove);
					}
				}
				possibleRemove.clear();
			}
			mNumMatrices += mNewVerticesArr[addIndex].size();
			log.debug("Added {} vertices, now at {}, with {} in map", mNewVerticesArr[addIndex].size(),mNumMatrices, mMatrixSet.size());
			for(QuiverMatrix remove : mNewVerticesArr[readIndex]){
				LinkableQuiverMatrixHolder holder = mMatrixSet.get(remove);
				if(holder != null && holder.isComplete()){
					mMatrixSet.remove(remove);
					quiverPool.returnObj(remove);
				}
			}
		} while (!mNewVerticesArr[addIndex].isEmpty());
		log.info("Graph completed. Vertices: {}", mNumMatrices );
		return null;
	}

	private boolean shouldMutateAt(
			Map<QuiverMatrix, LinkableQuiverMatrixHolder> matrixSet,
			QuiverMatrix matrix, int i) {
		LinkableQuiverMatrixHolder holder = matrixSet.get(matrix);
		return holder != null && !holder.hasLink(i);
	}

}
