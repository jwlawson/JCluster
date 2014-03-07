/**
 * Copyright 2014 John Lawson
 * 
 * EquivMutClassSizeTask.java is part of JCluster.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nf.fr.eraasoft.pool.ObjectPool;

/**
 * @author John Lawson
 * 
 */
public class EquivMutClassSizeTask extends MutClassSizeTask<EquivQuiverMatrix> {
	
	private List<EquivQuiverMatrix> mList;

	public EquivMutClassSizeTask(EquivQuiverMatrix matrix) {
		super(matrix);
		mList = new ArrayList<EquivQuiverMatrix>();
	}

	public EquivMutClassSizeTask(QuiverMatrix matrix) {
		this(new EquivQuiverMatrix(matrix));
	}
	
	@Override
	protected boolean matrixSeenBefore(EquivQuiverMatrix newMatrix,
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet) {
		boolean result = super.matrixSeenBefore(newMatrix, matrixSet);
		result = result || mList.contains(newMatrix);
		return result;
	}
	
	@Override
	protected void handleSeenMatrix(
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet,
			EquivQuiverMatrix mat, EquivQuiverMatrix newMatrix, int i) {
		LinkHolder<EquivQuiverMatrix> newHolder = matrixSet.get(newMatrix);
		if(newHolder == null){
			// Matrix seen before, but no longer in map as all links mutated
			return;
		}
		if(QuiverMatrix.areEqual(newMatrix, newHolder.getQuiverMatrix())){
			// Matrices are equal, not just equivalent so update the stored matrix link
			LinkHolder<EquivQuiverMatrix> oldHolder = matrixSet.get(mat);
			oldHolder.setLinkAt(i);
		}
		newHolder.setLinkAt(i);
	}
	
	@Override
	protected void checkRemoveQuiver(EquivQuiverMatrix remove,
			ObjectPool<EquivQuiverMatrix> quiverPool,
			ObjectPool<LinkHolder<EquivQuiverMatrix>> holderPool,
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> mMatrixSet) {
		boolean removeQ = true;
		LinkHolder<EquivQuiverMatrix> holder = mMatrixSet.get(remove);
		if (holder != null && holder.isComplete()) {
			EquivQuiverMatrix key = holder.getQuiverMatrix();
			holder = mMatrixSet.remove(remove);
			if (key == remove) {
				removeQ = false;
			}
			mList.add(key);
			holderPool.returnObj(holder);
		}
		if(removeQ){
		quiverPool.returnObj(remove);
		}
	}
	
	@Override
	protected void removeFromMap(EquivQuiverMatrix remove,
			ObjectPool<EquivQuiverMatrix> quiverPool,
			ObjectPool<LinkHolder<EquivQuiverMatrix>> holderPool,
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> mMatrixSet,
			LinkHolder<EquivQuiverMatrix> holder) {
		EquivQuiverMatrix key = holder.getQuiverMatrix();
		holder = mMatrixSet.remove(remove);
		if (key != remove) {
			// So remove.equals(key), but they are not the same object
			mList.add(key);
		} else {
			throw new RuntimeException("It did a oops");
		}
		holderPool.returnObj(holder);
	}
	
	@Override
	protected void teardown(ObjectPool<EquivQuiverMatrix> quiverPool,
			ObjectPool<LinkHolder<EquivQuiverMatrix>> holderPool,
			Map<EquivQuiverMatrix, LinkHolder<EquivQuiverMatrix>> matrixSet) {
		for(EquivQuiverMatrix m : mList){
			quiverPool.returnObj(m);
		}
		mList.clear();
	}
}
