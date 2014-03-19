/**
 * Copyright 2014 John Lawson
 * 
 * MutClassSizeTaskTest.java is part of JCluster. Licensed under the Apache License, Version 2.0
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.DynkinDiagram;
import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * @author John Lawson
 * 
 */
public class MutClassSizeTaskTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testA4() {
		log.debug("Starting test");
		MutClassSizeTask<QuiverMatrix> task =
				new MutClassSizeTask<QuiverMatrix>(DynkinDiagram.A4.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<MatrixInfo> future = exec.submit(task);
		try {
			int value = future.get().getMutationClassSize();
			assertEquals(144, value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}


	@Test
	public void testA3() {
		log.debug("Starting test");
		MutClassSizeTask<QuiverMatrix> task =
				new MutClassSizeTask<QuiverMatrix>(DynkinDiagram.A3.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<MatrixInfo> future = exec.submit(task);
		try {
			int value = future.get().getMutationClassSize();
			assertEquals(14, value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}

	@Test
	public void testA5() {
		log.debug("Starting test");
		MutClassSizeTask<QuiverMatrix> task =
				new MutClassSizeTask<QuiverMatrix>(DynkinDiagram.A5.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<MatrixInfo> future = exec.submit(task);
		try {
			int value = future.get().getMutationClassSize();
			assertEquals(1980, value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}

	@Test
	public void testD5() {
		log.debug("Starting test");
		MutClassSizeTask<QuiverMatrix> task =
				new MutClassSizeTask<QuiverMatrix>(DynkinDiagram.D5.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<MatrixInfo> future = exec.submit(task);
		try {
			int value = future.get().getMutationClassSize();
			assertEquals(2184, value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}

	@Test
	public void testInf() {
		QuiverMatrix mat =
				new QuiverMatrix(4, 4, 0, 1, 0, 0, -1, 0, 1, 1, 0, -1, 0, 1, 0, -1, -1, 0);
		MutClassSizeTask<QuiverMatrix> task = new MutClassSizeTask<QuiverMatrix>(mat);
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<MatrixInfo> future = exec.submit(task);
		try {
			int value = future.get().getMutationClassSize();
			assertEquals(-1, value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			exec.shutdown();
		}
	}

}
