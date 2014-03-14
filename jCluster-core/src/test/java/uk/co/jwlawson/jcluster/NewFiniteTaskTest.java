/**
 * Copyright 2014 John Lawson
 * 
 * NewFiniteTaskTest.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class NewFiniteTaskTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void test() {
		log.debug("Starting test");
		MutClassSizeTask<QuiverMatrix> task =
				new MutClassSizeTask<QuiverMatrix>(DynkinDiagram.A4.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<Integer> future = exec.submit(task);
		try {
			int value = future.get();
			assertEquals(144, value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}


	@Test
	public void test1() {
		log.debug("Starting test");
		MutClassSizeTask<QuiverMatrix> task =
				new MutClassSizeTask<QuiverMatrix>(DynkinDiagram.A3.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<Integer> future = exec.submit(task);
		try {
			int value = future.get();
			assertEquals(14, value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Ignore("Don't know the real value")
	@Test
	public void testA5() {
		log.debug("Starting test");
		MutClassSizeTask<QuiverMatrix> task =
				new MutClassSizeTask<QuiverMatrix>(DynkinDiagram.A5.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<Integer> future = exec.submit(task);
		try {
			int value = future.get();
			assertEquals(14, value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Ignore("Don't know the real value")
	@Test
	public void testD5() {
		log.debug("Starting test");
		MutClassSizeTask<QuiverMatrix> task =
				new MutClassSizeTask<QuiverMatrix>(DynkinDiagram.D5.getMatrix());
		ExecutorService exec = Executors.newSingleThreadExecutor();

		Future<Integer> future = exec.submit(task);
		try {
			int value = future.get();
			assertEquals(14, value);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Ignore("Takes ages and not really a test")
	@Test
	public void testAllSmallMatrices() {
		Executor exec = Executors.newSingleThreadExecutor();
		CompletionService<Integer> pool = new ExecutorCompletionService<Integer>(exec);
		for (DynkinDiagram d : DynkinDiagram.TEST_SET) {
			MutClassSizeTask<QuiverMatrix> task = new MutClassSizeTask<QuiverMatrix>(d.getMatrix());
			pool.submit(task);
		}
		for (DynkinDiagram d : DynkinDiagram.TEST_SET) {
			try {
				int value = pool.take().get();
				log.info("Found {} quivers in class of {}", value, d.toString());
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
