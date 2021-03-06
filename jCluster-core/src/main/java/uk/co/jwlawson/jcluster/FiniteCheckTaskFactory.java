/**
 * Copyright 2014 John Lawson
 * 
 * FiniteCheckTaskFactory.java is part of JCluster. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
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
 * @author John Lawson
 * 
 * @param <T> Type of matrix being checked
 */
public class FiniteCheckTaskFactory<T extends QuiverMatrix> implements MatrixTaskFactory<T> {

	@Override
	public MatrixTask<T> getTask(T matrix) {
		MatrixTask<T> task = new FiniteCheck<T>();
		task.setMatrix(matrix);
		return task;
	}

}
