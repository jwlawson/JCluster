/**
 * Copyright 2014 John Lawson
 * 
 * MinMutInfCheck.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * Check whether a matrix is minimally mutation infinite.
 * 
 * <p>
 * A matrix is minimally mutation infinite if it is infinite, but each sub-quiver is mutation
 * finite.
 * 
 * @author John Lawson
 * @param <T> Type of matrix to check
 */
public class MinMutInfCheck<T extends QuiverMatrix> implements MatrixTask<T> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Matrix to check if min mut inf. */
	private T mMatrix;
	/**
	 * Task to check whether each submatrix is mutation finite. Lazily loaded as not always required.
	 */
	private AllSubFiniteCheck<T> mSubCheck;
	/** Task to check whether the matrix is infinite. */
	private final FiniteCheck<T> mFiniteCheck;

	public MinMutInfCheck() {
		mFiniteCheck = new FiniteCheck<T>();
	}

	@Override
	public void setMatrix(T matrix) {
		mMatrix = matrix;
		mFiniteCheck.setMatrix(mMatrix);
		if (mSubCheck != null) {
			mSubCheck.setMatrix(mMatrix);
		}
	}

	@Override
	public void reset() {
		mSubCheck.reset();
		mFiniteCheck.reset();
	}

	@Override
	public void requestStop() {
		mFiniteCheck.requestStop();
		if (mSubCheck != null) {
			mSubCheck.requestStop();
		}
	}

	@Override
	public MatrixInfo call() throws Exception {
		MatrixInfo result = new MatrixInfo(mMatrix);

		log.debug("Checking whether matrix is infinite. {}", mMatrix);
		MatrixInfo checkInf = mFiniteCheck.call();
		result.combine(checkInf);
		if (checkInf.isFinite()) {
			log.debug("Matrix found to be finite");
			result.setMinMutInf(false);
			return result;
		}
		if (mSubCheck == null) {
			mSubCheck = AllSubFiniteCheck.getInstance(mMatrix);
		}
		log.debug("Checking whether all submatrices are finite. {}", mMatrix);
		MatrixInfo checkSubs = mSubCheck.call();
		result.combine(checkSubs);
		if (!result.isFinite() && result.getAllSubmatricesFinite()) {
			result.setMinMutInf(true);
		} else {
			result.setMinMutInf(false);
		}

		return result;
	}
}
