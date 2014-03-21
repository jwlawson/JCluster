/**
 * Copyright 2014 John Lawson
 * 
 * MinMutInfCheckTest.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import uk.co.jwlawson.jcluster.data.DynkinDiagram;
import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * @author John Lawson
 * 
 */
public class MinMutInfCheckTest {

	@Test
	public void testMinMutInf() {
		QuiverMatrix mat =
				new QuiverMatrix(4, 4, 0, 1, 0, 0, -1, 0, 1, 1, 0, -1, 0, 1, 0, -1, -1, 0);
		MinMutInfCheck<QuiverMatrix> task = new MinMutInfCheck<QuiverMatrix>();
		task.setMatrix(mat);

		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> future = exec.submit(task);

		MatrixInfo result;
		try {
			result = future.get();
			assertTrue(result.isMinMutInf());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}

	@Test
	public void testNotMinMutInf() {
		QuiverMatrix mat =
				new QuiverMatrix(5, 5, 0, 1, 0, 0, 1, -1, 0, 1, 1, 1, 0, -1, 0, 1, 0, 0, -1, -1, 0,
						0, -1, -1, 0, 0, 0);
		MinMutInfCheck<QuiverMatrix> task = new MinMutInfCheck<QuiverMatrix>();
		task.setMatrix(mat);

		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> future = exec.submit(task);

		MatrixInfo result;
		try {
			result = future.get();
			assertFalse(result.isMinMutInf());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}

	@Test
	public void testNotInfinite() {
		QuiverMatrix mat = DynkinDiagram.A6.getMatrix();
		MinMutInfCheck<QuiverMatrix> task = new MinMutInfCheck<QuiverMatrix>();
		task.setMatrix(mat);

		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> future = exec.submit(task);

		MatrixInfo result;
		try {
			result = future.get();
			assertFalse(result.isMinMutInf());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}


}
