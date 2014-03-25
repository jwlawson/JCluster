/**
 * Copyright 2014 John Lawson
 * 
 * FastInfiniteCheckTest.java is part of JCluster. Licensed under the Apache License, Version 2.0
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.DynkinDiagram;
import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * @author John Lawson
 * 
 */
public class FastInfiniteCheckTest {

	Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testCall() {
		QuiverMatrix matrix = DynkinDiagram.A5.getMatrix();
		FastInfiniteCheck task = new FastInfiniteCheck(matrix);

		try {
			MatrixInfo info = task.call();
			assertFalse(info.hasFiniteSet());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testThreads() {
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		CompletionService<MatrixInfo> pool = new ExecutorCompletionService<MatrixInfo>(threadPool);

		for (DynkinDiagram m : DynkinDiagram.TEST_SET) {
			QuiverMatrix matrix = m.getMatrix();
			pool.submit(new FastInfiniteCheck(matrix));
		}

		for (int i = 0; i < DynkinDiagram.TEST_SET.size(); i++) {
			try {
				MatrixInfo info = pool.take().get();
				assertFalse(info.hasFiniteSet());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		threadPool.shutdown();
	}

	@Test
	public void testInf() {
		QuiverMatrix mat = new QuiverMatrix(4, 4, 0, 1, 0, 0, -1, 0, 1, 1, 0, -1, 0, 1, 0, -1, -1, 0);
		FastInfiniteCheck task = new FastInfiniteCheck(mat);

		try {
			MatrixInfo info = task.call();
			assertTrue(info.hasFiniteSet());
			assertFalse(info.isFinite());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		QuiverMatrix mat =
				new QuiverMatrix(5, 5, 0, -1, 1, 0, 2, 1, 0, -1, 0, -2, -1, 1, 0, 1, 2, 0, 0, -1, 0, 0, -2,
						2, -2, 0, 0);
		FastInfiniteCheck task = new FastInfiniteCheck(mat);

		try {
			MatrixInfo info = task.call();
			assertTrue(info.hasFiniteSet());
			assertFalse(info.isFinite());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
