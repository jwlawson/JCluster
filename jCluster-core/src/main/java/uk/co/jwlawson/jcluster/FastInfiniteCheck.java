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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;
import uk.co.jwlawson.jcluster.pool.Pools;

/**
 * Checks whether the initial matrix is infinite. This can prove that the matrix is infinite, but
 * cannot prove it is finite, rather that it is probably finite.
 * 
 * @author John Lawson
 * 
 */
public class FastInfiniteCheck implements MatrixTask<QuiverMatrix> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Number of random mutations to try before giving up. */
	private static final int MAX_NUMBER_MUTATIONS = 3000;

	/** Initial matrix to check. */
	private QuiverMatrix mMatrix;
	/** Array holding two matrices to put the mutations into. */
	private final QuiverMatrix[] mutated;
	/** Listeners called when the task has finished. */
	private final List<CheckInfListener> mListeners;

	/** Create a new instance. */
	public FastInfiniteCheck() {
		mutated = new QuiverMatrix[2];
		mListeners = new ArrayList<FastInfiniteCheck.CheckInfListener>(1);
	}

	/**
	 * Create a new instance with provided initial matrix to check.
	 * 
	 * @param matrix Initial matrix to check if mutation infinite
	 */
	public FastInfiniteCheck(final QuiverMatrix matrix) {
		this();
		setMatrix(matrix);
	}

	@Override
	public void setMatrix(final QuiverMatrix matrix) {
		mMatrix = matrix;
	}

	@Override
	public void reset() {}

	/**
	 * Add a listener which will be called once the task completes.
	 * 
	 * @param listener Listener to add
	 */
	public void addListener(final CheckInfListener listener) {
		mListeners.add(listener);
	}

	/**
	 * Check whether the quiver matrix is mutation infinite.
	 * 
	 * @return MatrixInfo object containing the result of the calculation
	 * @throws Exception if something goes wrong
	 */
	@Override
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
					log.debug("Infinite matrix found {} for initial {}", mutated[counter % 2], mMatrix);
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

	/** Listener called once the calculation is complete. */
	public interface CheckInfListener {
		/**
		 * Called once the matrix has been checked.
		 * 
		 * @param matrix Matrix checked
		 */
		void matrixChecked(QuiverMatrix matrix);
	}

	@Override
	public void requestStop() {}

}
