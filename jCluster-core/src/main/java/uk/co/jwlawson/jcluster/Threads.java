/**
 * Copyright 2014 John Lawson
 * 
 * Threads.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster;

import java.util.concurrent.ExecutorService;

/**
 * @author John Lawson
 * 
 */
public class Threads {

	public static synchronized ExecutorService getSubmittingThreadPool() {
		if (submittingPool == null) {
			submittingPool = factory.createSubmittingThreadPool();
		}
		return submittingPool;
	}

	public static synchronized ExecutorService getCalculationThreadPool() {
		if (calculatingPool == null) {
			calculatingPool = factory.createCalculationThreadPool();
		}
		return calculatingPool;
	}

	public static ExecutorService getThreadPoolForTask(MatrixTask<?> task) {
		if (task.submitsSubmitting()) {
			return getSubmittingThreadPool();
		} else {
			return getCalculationThreadPool();
		}
	}

	public static void shutdownAll() {
		submittingPool.shutdown();
		calculatingPool.shutdown();
	}

	private static ExecutorService submittingPool;
	private static ExecutorService calculatingPool;

	private static ThreadPoolFactory factory = new BlockingThreadPoolFactory();

}
