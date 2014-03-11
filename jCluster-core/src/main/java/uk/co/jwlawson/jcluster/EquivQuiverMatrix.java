/**
 * Copyright 2014 John Lawson
 * 
 * EquivQuiverMatrix.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class EquivQuiverMatrix extends QuiverMatrix {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final EquivalenceChecker mChecker;
	private int mHashcode;

	public EquivQuiverMatrix(int rows, int cols) {
		this(rows);
	}

	EquivQuiverMatrix(int size) {
		super(size, size);
		mChecker = EquivalenceChecker.getInstance(size);
	}

	public EquivQuiverMatrix(int size, int... values) {
		super(size, size, values);
		mChecker = EquivalenceChecker.getInstance(size);
	}

	public EquivQuiverMatrix(QuiverMatrix matrix) {
		super(matrix);
		mChecker = EquivalenceChecker.getInstance(matrix.getNumRows());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;

		EquivQuiverMatrix rhs = (EquivQuiverMatrix) obj;
		return mChecker.areEquivalent(this, rhs);
	}

	/*
	 * Because the hashcode must be the same for each matrix with permuted rows, this method will
	 * likely create lots of hash collisions.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see uk.co.jwlawson.jcluster.QuiverMatrix#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = mHashcode;
		if (hash == 0) {
			hash = 137;
			for (int i = 0; i < getNumRows(); i++) {
				for (int j = 0; j < getNumCols(); j++) {
					hash += Math.abs(unsafeGet(i, j));
				}
			}
		}
		return mHashcode;
	}
}
