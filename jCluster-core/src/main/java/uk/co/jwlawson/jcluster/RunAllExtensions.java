/**
 * Copyright 2014 John Lawson
 * 
 * RunAllExtensions.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import java.util.concurrent.CompletionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.pool.Pool;

/**
 * @author John Lawson
 * 
 */
public class RunAllExtensions<T extends QuiverMatrix> extends RunMultipleTask<T> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private T mMatrix;
	private T mEnlargedMatrix;
	private MatrixTaskFactory<T> mFactory;
	private Pool<T> mPool;

	@Override
	public void setMatrix(T matrix) {
		mMatrix = matrix;
		mEnlargedMatrix = mMatrix.enlargeMatrix(1, 1, mPool.getObj());
	}

	@Override
	public void reset() {}

	@Override
	public void requestStop() {}

	@Override
	protected void submitTasks(CompletionService<MatrixInfo> exec) {
		int size = Math.min(mMatrix.getNumRows(), mMatrix.getNumCols());
		for (int num = 0; num < Math.pow(5, size); num++) {
			MatrixTask<T> task = mFactory.getTask(getExtension(num, size));
			exec.submit(task);
		}
	}

	private T getExtension(int num, int size) {
		T matrix = mPool.getObj();
		matrix.set(mEnlargedMatrix);
		for (int i = 0; i < size; i++) {
			int val = (((int) (num / Math.pow(5, i))) % 5) - 2;
			matrix.unsafeSet(size, i, val);
			matrix.unsafeSet(i, size, -val);
		}
		log.debug("Added vertex id: {} to get matrix: {}", num, matrix);
		return matrix;
	}

}
