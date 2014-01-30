/**
 * Copyright 2014 John Lawson
 * 
 * FindInfExtensionTaskTest.java is part of JCluster.
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

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class FindInfExtensionTaskTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void test() {
		ExecutorService threadPool = Executors.newFixedThreadPool(4);
		FindInfExtensionTask task = new FindInfExtensionTask(DynkinDiagram.A8.getMatrix(),
				threadPool);

		log.info("Starting test on matrix");
		Future<Set<QuiverMatrix>> future = threadPool.submit(task);
		try {
			Set<QuiverMatrix> set = future.get();
			log.debug("Infinite extensions found:");
			for (QuiverMatrix m : set) {
				log.debug("{}", m);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			threadPool.shutdown();
		}

	}

}
