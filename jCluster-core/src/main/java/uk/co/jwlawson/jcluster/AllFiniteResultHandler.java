/**
 * Copyright 2014 John Lawson
 * 
 * AllFiniteResultHandler.java is part of JCluster. Licensed under the Apache License, Version 2.0
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

import javax.annotation.Nullable;

/**
 * @author John Lawson
 * 
 */
public class AllFiniteResultHandler extends MatrixInfoResultHandler {

	/** Whether all results so far were finite. */
	private boolean allFinite = true;

	/** Task creating the results. */
	@Nullable
	private MatrixTask<?> task;

	public AllFiniteResultHandler(MatrixInfo initial) {
		super(initial);
	}

	public void setTask(MatrixTask<?> task) {
		this.task = task;
	}

	@Override
	protected void handleResult(MatrixInfo matrix) {
		if (!matrix.hasFiniteSet()) {
			throw new IllegalStateException("Cannot handle result which is unknown");
		}
		if (!matrix.isFinite()) {
			allFinite = false;
			if (task != null) {
				task.requestStop();
			}
		}
	}

	@Override
	protected MatrixInfo getFinal() {
		MatrixInfo info = getInitial();
		info.setAllSubmatricesFinite(allFinite);
		return info;
	}

}
