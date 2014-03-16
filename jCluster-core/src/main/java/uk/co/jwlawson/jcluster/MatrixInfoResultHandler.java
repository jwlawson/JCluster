/**
 * Copyright 2014 John Lawson
 * 
 * MatrixInfoResultHandler.java is part of JCluster. Licensed under the Apache License, Version 2.0
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

import java.util.concurrent.Callable;

/**
 * @author John Lawson
 * 
 */
public abstract class MatrixInfoResultHandler implements Callable<MatrixInfo> {

	private CompletionResultQueue<MatrixInfo> queue;
	private boolean running = true;

	/**
	 * Handle the new result.
	 * 
	 * @param matrix New result
	 */
	protected abstract void handleResult(MatrixInfo matrix);

	/**
	 * Get the final MatrixInfo object after processing all the results.
	 * 
	 * @return Final MatrixInfo object
	 */
	protected abstract MatrixInfo getFinal();

	/**
	 * Inform the handler that all results have been queued, so once the queue is empty there will be
	 * no more.
	 */
	public void allResultsQueued() {
		running = false;
	}

	@Override
	public MatrixInfo call() throws Exception {
		while (queue.hasResult() || running) {
			MatrixInfo info = queue.popResult();
			handleResult(info);
		}
		return getFinal();
	}

}
