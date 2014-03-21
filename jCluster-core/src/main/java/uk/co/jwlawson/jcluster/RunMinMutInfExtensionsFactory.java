/**
 * Copyright 2014 John Lawson
 * 
 * RunMinMutInfExtensionsFactory.java is part of JCluster. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster;

import java.util.ArrayList;
import java.util.List;

import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * @author John Lawson
 * 
 */
public class RunMinMutInfExtensionsFactory<T extends QuiverMatrix> implements MatrixTaskFactory<T> {

	private final List<MatrixTaskFactory<QuiverMatrix>> mFactories =
			new ArrayList<MatrixTaskFactory<QuiverMatrix>>();

	private MatrixInfoResultHandler mHandler;

	/**
	 * Add a factory to provide tasks to run on each minimally mutation infinite matrix found.
	 * 
	 * @param factory Factory to add
	 */
	public void addTaskFactory(MatrixTaskFactory<QuiverMatrix> factory) {
		mFactories.add(factory);
	}

	public void setResultHandler(MatrixInfoResultHandler handler) {
		mHandler = handler;
	}

	@Override
	public MatrixTask<T> getTask(T matrix) {
		RunMinMutInfExtensions<T> result =
				RunMinMutInfExtensions.Builder.builder().withInitial(matrix)
						.withResultHandler(mHandler).build();
		for (MatrixTaskFactory<QuiverMatrix> fac : mFactories) {
			result.addTaskFactory(fac);
		}
		return null;
	}

}
