/**
 * Copyright 2014 John Lawson
 * 
 * MatrixTaskFactory.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import uk.co.jwlawson.jcluster.data.QuiverMatrix;


/**
 * Create tasks to compute information about matrices.
 * 
 * @author John Lawson
 * 
 * @param <T> Matrix type to be used in the tasks
 */
public interface MatrixTaskFactory<T extends QuiverMatrix> {

	/**
	 * Get a new task for the specified matrix.
	 * 
	 * <p>
	 * Some caching of tasks can be implemented provided that the {@link MatrixTask#reset()} and
	 * {@link MatrixTask#setMatrix(QuiverMatrix)} methods are called.
	 * 
	 * @param matrix Matrix to get a new task for
	 * @return New task
	 */
	MatrixTask<T> getTask(T matrix);

}
