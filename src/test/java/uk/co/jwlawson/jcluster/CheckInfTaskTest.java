/**
 * Copyright 2014 John Lawson
 * 
 * CheckInfTaskTest.java is part of JCluster.
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

import static org.junit.Assert.*;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class CheckInfTaskTest {

	Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testCall() {
		CheckInfTask task = new CheckInfTask(DynkinDiagram.A6.getMatrix(), QuiverPool.getInstance(
				6, 6));

		try {
			assertNull(task.call());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testThreads() {
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		CompletionService<QuiverMatrix> pool = new ExecutorCompletionService<QuiverMatrix>(
				threadPool);

		for (DynkinDiagram m : DynkinDiagram.MUT_FINITE) {
			QuiverMatrix matrix = m.getMatrix();
			pool.submit(new CheckInfTask(matrix, QuiverPool.getInstance(matrix.getNumRows(),
					matrix.getNumCols())));
		}

		for (int i = 0; i < DynkinDiagram.MUT_FINITE.size(); i++) {
			try {
				assertNull(pool.take().get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		threadPool.shutdown();
	}
}
