/**
 * Copyright 2014 John Lawson
 * 
 * MutationBenchmark.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
package uk.co.jwlawson.jcluster.demos;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.DynkinDiagram;
import uk.co.jwlawson.jcluster.EquivMutClassSizeTask;
import uk.co.jwlawson.jcluster.MatrixInfo;
import uk.co.jwlawson.jcluster.MutClassSizeTask;
import uk.co.jwlawson.jcluster.QuiverMatrix;

/**
 * @author John Lawson
 * 
 */
public class MutationBenchmark {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private <V> V runTimedTask(Callable<V> task, Timer timer) {
		timer.start();
		V result;
		try {
			result = task.call();
		} catch (Exception e) {
			log.error("Error when executing task", e);
			timer.stop();
			return null;
		}
		timer.stop();
		return result;
	}

	private Timer runTimedMutTask(QuiverMatrix matrix) {
		Timer timer = new Timer();
		Callable<MatrixInfo> task = new MutClassSizeTask<QuiverMatrix>(matrix);
		runTimedTask(task, timer);
		return timer;
	}

	private Timer runTimedEquivTask(QuiverMatrix matrix) {
		Timer timer = new Timer();
		Callable<MatrixInfo> task = new EquivMutClassSizeTask(matrix);
		runTimedTask(task, timer);
		return timer;
	}

	public Timer[] runBenchmark(QuiverMatrix matrix) {
		Timer mut = runTimedMutTask(matrix);
		Timer equ = runTimedEquivTask(matrix);
		return new Timer[] {mut, equ};
	}

	public Timer[] runBenchmark(DynkinDiagram d) {
		return runBenchmark(d.getMatrix());
	}

	public void logBenchmark(DynkinDiagram d) {
		Timer[] times = runBenchmark(d);
		log.info("{}: MutClassSize{}.    EquivMutClassSize{}.", d.toString(), times[0], times[1]);
	}

	private static class Timer {

		private long startTime;
		private long stopTime;

		private void start() {
			startTime = System.nanoTime();
		}

		private void stop() {
			stopTime = System.nanoTime();
		}

		private long getTimeTaken() {
			return stopTime - startTime;
		}

		@Override
		public String toString() {
			return String.format("Task took %,16d ns", getTimeTaken());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MutationBenchmark bench = new MutationBenchmark();

		for (DynkinDiagram d : DynkinDiagram.TEST_SET) {
			bench.logBenchmark(d);
		}
	}

}
