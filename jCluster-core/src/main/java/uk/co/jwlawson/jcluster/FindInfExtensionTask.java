/**
 * Copyright 2014 John Lawson
 * 
 * FindInfExtensionTask.java is part of JCluster.
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import nf.fr.eraasoft.pool.ObjectPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.CheckInfTask.CheckInfListener;

/**
 * @author John Lawson
 * 
 */
public class FindInfExtensionTask implements Callable<Set<QuiverMatrix>> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private QuiverMatrix mInitialMatrix;
	private QuiverMatrix mEnlargedMatrix;
	private ExecutorService mExecutor;

	public FindInfExtensionTask(QuiverMatrix initial, ExecutorService executor) {
		if (initial.getNumCols() != initial.getNumRows()) {
			throw new IllegalArgumentException(
					"Task assumes that the matrix is square");
		}
		mInitialMatrix = initial;
		mEnlargedMatrix = initial.enlargeMatrix(1, 1);
		mExecutor = executor;
	}

	public Set<QuiverMatrix> call() throws Exception {
		int size = mInitialMatrix.getNumRows();
		ExecutorCompletionService<QuiverMatrix> pool = new ExecutorCompletionService<QuiverMatrix>(
				mExecutor);
		final ObjectPool<QuiverMatrix> matrixPool = Pools.getQuiverMatrixPool(
				mEnlargedMatrix.getNumRows(), mEnlargedMatrix.getNumCols(),
				QuiverMatrix.class);

		for (int num = 0; num < Math.pow(5, size); num++) {
			CheckInfTask task = getEnlargedCheckInfTask(size, matrixPool, num);
			pool.submit(task);
			if (num % 100000 == 0) {
				log.debug("{} CheckInfTasksSubmitted out of {}", num,
						Math.pow(5, size));
			}
		}
		log.info("All extensions queued up");
		Set<QuiverMatrix> infiniteMatrices = new HashSet<QuiverMatrix>();
		for (int num = 0; num < Math.pow(5, size); num++) {
			if (num % 100000 == 0) {
				log.debug("{} Infinite matrices found out of {}", num,
						Math.pow(5, size));
			}
			QuiverMatrix res = pool.take().get();
			if (res != null) {
//				infiniteMatrices.add(res);
			}
		}
		log.info("All extensions checked");
		return infiniteMatrices;
	}

	private CheckInfTask getEnlargedCheckInfTask(int size,
			final ObjectPool<QuiverMatrix> matrixPool, int num)
			throws Exception {
		QuiverMatrix matrix = addVertexToMatrix(size, matrixPool, num);
		CheckInfTask task = new CheckInfTask(matrix, Pools.getQuiverMatrixPool(
				matrix.getNumRows(), matrix.getNumCols(), QuiverMatrix.class));
		task.addListener(new CheckInfListener() {
			public void matrixChecked(QuiverMatrix matrix) {
				try {
					matrixPool.returnObj(matrix);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return task;
	}

	private QuiverMatrix addVertexToMatrix(int size,
			final ObjectPool<QuiverMatrix> matrixPool, int num)
			throws Exception {
		QuiverMatrix matrix = matrixPool.getObj();
		matrix.set(mEnlargedMatrix);
		log.debug("Enlarged matrix: {} copied to {}", mEnlargedMatrix, matrix);
		for (int i = 0; i < size; i++) {
			int val = (((int) (num / Math.pow(5, i))) % 5) - 2;
			matrix.unsafeSet(size, i, val);
			matrix.unsafeSet(i, size, -val);
		}
		log.debug("Added vertex id: {} to get matrix: {}", num, matrix);
		return matrix;
	}

}
