/**
 * Copyright 2014 John Lawson
 * 
 * FiniteCheck.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

/**
 * Task to check whether a matrix is mutation finite or not.
 * 
 * @author John Lawson
 * 
 * @param <T> Type of matrix which is being checked
 */
public class FiniteCheck implements MatrixTask<QuiverMatrix> {

	/** Matrix to check. */
	private QuiverMatrix mMatrix;
	/** Fast check task. */
	private final FastInfiniteCheck mFastCheck;
	/** Slower mutation class check. Loaded lazily as not always needed. */
	private EquivMutClassSizeTask mSlowCheck;

	/**
	 * Create a new task to check whether a matrix is finite.
	 */
	public FiniteCheck() {
		mFastCheck = new FastInfiniteCheck();
	}

	@Override
	public void setMatrix(final QuiverMatrix matrix) {
		mMatrix = matrix;
	}

	@Override
	public void reset() {}

	@Override
	public MatrixInfo call() throws Exception {
		MatrixInfo result = tryFastCheck();
		if (!result.hasFiniteSet()) {
			MatrixInfo sizeInfo = tryMutClassTask();
			int size = sizeInfo.getEquivMutationClassSize();
			if (size == -1) {
				result.setFinite(false);
			} else {
				result.setEquivMutationClassSize(size);
				result.setFinite(true);
			}
		}
		return result;
	}

	/**
	 * Try the fast infinite check first.
	 * 
	 * @return MatrixInfo which contains the results
	 * @throws Exception if something goes wrong
	 */
	private MatrixInfo tryFastCheck() throws Exception {
		mFastCheck.reset();
		mFastCheck.setMatrix(mMatrix);
		return mFastCheck.call();
	}

	/**
	 * If the fast check wasn't conclusive fall back onto the longer check.
	 * 
	 * @return MatrixInfo object with the results
	 * @throws Exception if something goes wrong
	 */
	private MatrixInfo tryMutClassTask() throws Exception {
		if (mSlowCheck == null) {
			mSlowCheck = new EquivMutClassSizeTask(mMatrix);
		} else {
			mSlowCheck.reset();
			mSlowCheck.setMatrix(new EquivQuiverMatrix(mMatrix));
		}
		return mSlowCheck.call();
	}

	@Override
	public void requestStop() {}

}
