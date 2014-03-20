/**
 * Copyright 2014 John Lawson
 * 
 * RunAllExtensionsTest.java is part of JCluster. Licensed under the Apache License, Version 2.0
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
import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * @author John Lawson
 * 
 */
public class RunAllExtensionsTest {

	/**
	 * Checks that the number of matrices which are submitted to have tasks run on them matches the
	 * total number of possible extensions.
	 */
	@Test
	public void testA4() {
		CountTaskFactory<QuiverMatrix> factory = new CountTaskFactory<QuiverMatrix>();
		QuiverMatrix mat = DynkinDiagram.A4.getMatrix();
		RunAllExtensions<QuiverMatrix> task = RunAllExtensions.getInstance(mat);
		task.setResultHandler(new ResultHandler());
		task.addTaskFactory(factory);

		ExecutorService thread = Executors.newSingleThreadExecutor();
		Future<MatrixInfo> future = thread.submit(task);

		try {
			future.get();
			int val = factory.getCount();
			assertEquals((int) Math.pow(5, 4), val);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			thread.shutdown();
		}
	}

	/**
	 * Dummy result handler which does nothing with the results and just returns null at the end.
	 * 
	 * @author John Lawson
	 * 
	 */
	private class ResultHandler extends MatrixInfoResultHandler {

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
