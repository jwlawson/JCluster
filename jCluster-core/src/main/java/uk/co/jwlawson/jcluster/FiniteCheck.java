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
public class FiniteCheck<T extends QuiverMatrix> implements MatrixTask<T> {

	/** Matrix to check. */
	private T mMatrix;

	/** {@inheritDoc} */
	public void setMatrix(final T matrix) {
		mMatrix = matrix;
	}

	/** {@inheritDoc} */
	public void reset() {}

	/** {@inheritDoc} */
	public MatrixInfo call() throws Exception {
		MatrixInfo result = tryFastCheck();
		if (!result.hasFiniteSet()) {
			MatrixInfo size = tryMutClassTask();
			result.combine(size);
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
		FastInfiniteCheck task = new FastInfiniteCheck(mMatrix);
		return task.call();
	}

	/**
	 * If the fast check wasn't conclusive fall back onto the longer check.
	 * 
	 * @return MatrixInfo object with the results
	 * @throws Exception if something goes wrong
	 */
	private MatrixInfo tryMutClassTask() throws Exception {
		EquivMutClassSizeTask task = new EquivMutClassSizeTask(mMatrix);
		return task.call();
	}

}
