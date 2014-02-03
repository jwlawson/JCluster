/**
 * Copyright 2014 John Lawson
 * 
 * AddVertexTask.java is part of JCluster.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.jwlawson.jcluster;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author John Lawson
 * 
 */
public class AddVertexTask implements Callable<Future<QuiverMatrix>> {

	private QuiverMatrix mMatrix;
	private int size;
	private int num;
	private ExecutorService pool;

	public AddVertexTask(QuiverMatrix matrix, int value, ExecutorService executor) {
		mMatrix = matrix;
		size = matrix.getNumCols() - 1;
		num = value;
		pool = executor;
	}

	public Future<QuiverMatrix> call() throws Exception {
		QuiverMatrix matrix = mMatrix.copy();
		for (int i = 0; i < size; i++) {
			int val = (((int) (num / Math.pow(5, i))) % 5) - 2;
			matrix.unsafeSet(size, i, val);
			matrix.unsafeSet(i, size, -val);
		}
		Future<QuiverMatrix> future = pool.submit(new CheckInfTask(matrix, QuiverPool.getInstance(
				matrix.getNumRows(), matrix.getNumCols())));

		return future;
	}

}
