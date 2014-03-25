/**
 * Copyright 2014 John Lawson
 * 
 * RunMutationClassTest.java is part of JCluster. Licensed under the Apache License, Version 2.0
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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import uk.co.jwlawson.jcluster.data.DynkinDiagram;
import uk.co.jwlawson.jcluster.data.EquivQuiverMatrix;
import uk.co.jwlawson.jcluster.data.MatrixInfo;

/**
 * @author John Lawson
 * 
 */
public class RunMutationClassTest {

	@Test
	public void testA4() {
		CountTaskFactory<EquivQuiverMatrix> factory = new CountTaskFactory<EquivQuiverMatrix>();
		EquivQuiverMatrix mat = new EquivQuiverMatrix(DynkinDiagram.A4.getMatrix());
		RunMutationClass task = RunMutationClass.getInstance(mat);
		task.setResultHandler(new ResultHandler());
		task.addTaskFactory(factory);

		ExecutorService thread = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> future = thread.submit(task);

		try {
			future.get();
			int val = factory.getCount();
			// Expected value is one less than the size of the class, as the first matrix does not
			// get counted
			assertEquals(5, val);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			thread.shutdown();
		}
	}

	@Test
	public void testD5() {
		CountTaskFactory<EquivQuiverMatrix> factory = new CountTaskFactory<EquivQuiverMatrix>();
		EquivQuiverMatrix mat = new EquivQuiverMatrix(DynkinDiagram.D5.getMatrix());
		RunMutationClass task = RunMutationClass.getInstance(mat);
		task.setResultHandler(new ResultHandler());
		task.addTaskFactory(factory);

		ExecutorService thread = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> future = thread.submit(task);

		try {
			future.get();
			int val = factory.getCount();
			// Expected value is one less than the size of the class, as the first matrix does not
			// get counted
			assertEquals(25, val);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			thread.shutdown();
		}
	}

	/**
	 * Dummy result handler which does nothing when a new result comes in and just returns null at the
	 * end.
	 */
	private class ResultHandler extends TECSResultHandler {

		public ResultHandler() {
			super(null);
		}

		@Override
		protected void handleResult(MatrixInfo matrix) {}

		@Override
		protected MatrixInfo getFinal() {
			return null;
		}

	}

}
