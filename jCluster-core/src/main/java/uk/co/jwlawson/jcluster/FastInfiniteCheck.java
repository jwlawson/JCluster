/**
 * Copyright 2014 John Lawson
 * 
 * FastInfiniteCheck.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
import java.util.concurrent.ThreadLocalRandom;

import uk.co.jwlawson.jcluster.pool.Pool;

/**
 * Checks whether the initial matrix is infinite. This can prove that the matrix is infinite, but
 * cannot prove it is finite, rather that it is probably finite.
 * 
 * @author John Lawson
 * 
 */
public class FastInfiniteCheck implements MatrixTask<QuiverMatrix> {

	private static final int MAX_NUMBER_MUTATIONS = 3000;

	private QuiverMatrix mMatrix;
	private final QuiverMatrix[] mutated;
	private final List<CheckInfListener> mListeners;

	public FastInfiniteCheck() {
		mutated = new QuiverMatrix[2];
		mListeners = new ArrayList<FastInfiniteCheck.CheckInfListener>(1);
	}

	public FastInfiniteCheck(QuiverMatrix matrix) {
		this();
		setMatrix(matrix);
	}

	public void setMatrix(QuiverMatrix matrix) {
		mMatrix = matrix;
	}

	public void reset() {}

	public void addListener(CheckInfListener listener) {
		mListeners.add(listener);
	}

	/**
	 * Check whether the quiver matrix is mutation infinite.
	 */
	public MatrixInfo call() throws Exception {
		MatrixInfo result = new MatrixInfo(mMatrix);
		if (isInfinte()) {
			result.setFinite(false);
		}
		return result;
	}

	/**
	 * Check whether the matrix is mutation infinite.
	 * 
	 * <p>
	 * This algorithm can only prove that the matrix is infinite. If {@code false} is returned then
	 * that means the matrix is probably mutation finite, but that is not known for sure.
	 * 
	 * @return true if the matrix is mutation infinite
	 */
	private boolean isInfinte() {
		Pool<QuiverMatrix> matrixPool =
				Pools.getQuiverMatrixPool(mMatrix.getNumRows(), mMatrix.getNumCols(), QuiverMatrix.class);

		try {
			// All 2x2 matrices are mutation finite
			if (mMatrix.getNumRows() == 2 && mMatrix.getNumCols() == 2) {
				return false;
			}

			for (int i = 0; i < 2; i++) {
				QuiverMatrix matrix = matrixPool.getObj();
				matrix.set(mMatrix);
				mutated[i] = matrix;
			}

			int lastMutation = -1;
			int counter = 0;

			while (counter < MAX_NUMBER_MUTATIONS) {
				int rand;
				do {
					rand = ThreadLocalRandom.current().nextInt(0, mMatrix.getNumRows());
				} while (rand == lastMutation);

				/* Alternate between mutating the two matrices in the array. */
				mutated[counter % 2].mutate(rand, mutated[++counter % 2]);
				if (mutated[counter % 2].isInfinite()) {
					return true;
				}
				lastMutation = rand;
			}
		} finally {
			for (int i = 0; i < 2; i++) {
				if (null != mutated[i]) {
					matrixPool.returnObj(mutated[i]);
				}
			}
			for (CheckInfListener l : mListeners) {
				l.matrixChecked(mMatrix);
			}
		}
		return false;
	}


	public interface CheckInfListener {
		void matrixChecked(QuiverMatrix matrix);
	}
}
