/**
 * Copyright 2014 John Lawson
 * 
 * MatrixTask.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import java.util.concurrent.Callable;

/**
 * Task to compute a property of a matrix. The result will be returned in a MatrixInfo object.
 * 
 * @param <T> Matrix class to use in the calculation
 * @author John Lawson
 * 
 */
public interface MatrixTask<T extends QuiverMatrix> extends Callable<MatrixInfo> {

	/**
	 * Set the matrix to be used in the calculation.
	 * 
	 * @param matrix Matrix to use
	 */
	void setMatrix(T matrix);

	/**
	 * Reset the task so that it can be used again for a different matrix.
	 */
	void reset();

	/**
	 * Perform the calculation on the matrix.
	 * 
	 * @return MatrixInfo object containing the result of the calculation
	 * @throws Exception if an error is encountered during the calculation
	 */
	MatrixInfo call() throws Exception;

}
