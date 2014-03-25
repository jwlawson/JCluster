/**
 * Copyright 2014 John Lawson
 * 
 * CountTask.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import org.apache.commons.lang3.mutable.MutableInt;

import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * @author John Lawson
 * 
 */
public class CountTask<T extends QuiverMatrix> implements MatrixTask<T> {

	private static final int[] lock = new int[0];

	private final MutableInt val;
	private T matrix;

	public CountTask(MutableInt i) {
		this.val = i;
	}

	@Override
	public void setMatrix(T matrix) {
		this.matrix = matrix;
	}

	@Override
	public void reset() {}

	@Override
	public void requestStop() {}

	@Override
	public MatrixInfo call() throws Exception {
		synchronized (lock) {
			val.increment();
		}
		return new MatrixInfo(matrix);
	}

}
