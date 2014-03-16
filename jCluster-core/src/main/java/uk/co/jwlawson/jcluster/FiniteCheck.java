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
 * @author John Lawson
 * 
 */
public class FiniteCheck<T extends QuiverMatrix> implements MatrixTask<T> {

	private T mMatrix;

	public void setMatrix(T matrix) {
		mMatrix = matrix;
	}

	public void reset() {}

	public MatrixInfo call() throws Exception {
		MatrixInfo result = tryCheckInfTask();
		if (!result.hasFiniteSet()) {
			MatrixInfo size = tryMutClassTask();
			result.combine(size);
		}
		return result;
	}

	private MatrixInfo tryCheckInfTask() throws Exception {
		FastInfiniteCheck task = new FastInfiniteCheck(mMatrix);
		return task.call();
	}

	private MatrixInfo tryMutClassTask() throws Exception {
		EquivMutClassSizeTask task = new EquivMutClassSizeTask(mMatrix);
		return task.call();
	}

}
