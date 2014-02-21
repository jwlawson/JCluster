/**
 * Copyright 2014 John Lawson
 * 
 * MutClassSizeTask.java is part of JCluster.
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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author John Lawson
 * 
 */
public class MutClassSizeTask extends AbstractMutClassSizeTask implements
		Callable<Integer> {

	public MutClassSizeTask(QuiverMatrix matrix) {
		super(matrix);
	}

	@Override
	protected Map<QuiverMatrix, LinkHolder> getMatrixMap(int size) {
		return new ConcurrentHashMap<QuiverMatrix, LinkHolder>((int) Math.pow(
				2, 3 * size - 3), 0.7f);
	}

	@Override
	protected boolean matrixSeenBefore(QuiverMatrix newMatrix,
			Map<QuiverMatrix, LinkHolder> matrixSet) {
		return matrixSet.containsKey(newMatrix);
	}

}
