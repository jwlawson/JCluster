/**
 * Copyright 2014 John Lawson
 * 
 * CheckInfTask.java is part of JCluster.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.jwlawson.jcluster;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class CheckInfTask implements Callable<QuiverMatrix> {

	private static final int MAX_NUMBER_MUTATIONS = 3000;
	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private QuiverMatrix mMatrix;
	private QuiverMatrix[] mMutated;
	private int mLastMutation;
	private int mCounter;
	private int mRand;
	private ObjectPool<QuiverMatrix> mMatrixPool;

	public CheckInfTask(QuiverMatrix matrix, ObjectPool<QuiverMatrix> pool) {
		mMatrix = matrix;
		mCounter = 0;
		mRand = -1;
		mLastMutation = -1;
		mMatrixPool = pool;
		mMutated = new QuiverMatrix[2];
	}

	/**
	 * Check whether the quiver matrix is mutation infinite. Return the matrix if it is.
	 */
	public QuiverMatrix call() throws Exception {
//		log.debug("Starting to check if infinite");
		try {
			for (int i = 0; i < 2; i++) {
				QuiverMatrix matrix = mMatrixPool.borrowObject();
				matrix.set(mMatrix);
				mMutated[i] = matrix;
			}
			while (mCounter < MAX_NUMBER_MUTATIONS) {
				do {
					mRand = ThreadLocalRandom.current().nextInt(0, mMatrix.getNumRows());
				} while (mRand == mLastMutation);

				/* Alternate between mutating the two matrices in the array. */
				mMutated[mCounter % 2].mutate(mRand, mMutated[++mCounter % 2]);
				if (isInfinite(mMutated[mCounter % 2])) {
					return mMatrix;
				}
				mLastMutation = mRand;
			}
		} finally {
			for (int i = 0; i < 2; i++) {
				if (null != mMutated[i]) {
					mMatrixPool.returnObject(mMutated[i]);
				}
			}
			mMatrixPool.returnObject(mMatrix);
		}
		return null;
	}

	private boolean isInfinite(QuiverMatrix matrix) {

		for (int i = 0; i < matrix.getNumRows(); i++) {
			for (int j = 0; j < matrix.getNumCols(); j++) {
				if (matrix.unsafeGet(i, j) >= 3) {
					return true;
				}
			}
		}
		return false;
	}

}
