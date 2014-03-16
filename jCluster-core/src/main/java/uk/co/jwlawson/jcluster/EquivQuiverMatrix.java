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

import java.util.Arrays;

/**
 * A QuiverMatrix where two matrices are considered equal if they are equivalent up to permutations
 * of the rows and columns.
 * 
 * <p>
 * This is much harder to compute than the normal matrix equals method, so the
 * {@link EquivQuiverMatrix#equals(Object)} method is fairly slow.
 * 
 * @author John Lawson
 * 
 */
public class EquivQuiverMatrix extends QuiverMatrix {

	/** Checker for whether two matrices are equivalent. */
	private final EquivalenceChecker mChecker;
	/** Cached hashcode. */
	private int mHashcode;

	/**
	 * Create a new matrix with {@code rows} number of rows and {@code cols} number of columns.
	 * 
	 * @see uk.co.jwlawson.jcluster.QuiverMatrix#QuiverMatrix(int, int)
	 * @param rows Number of rows in matrix
	 * @param cols Number of columns in matrix
	 */
	public EquivQuiverMatrix(final int rows, final int cols) {
		this(rows);
	}

	/**
	 * Create a new square matrix with {@code size} rows and columns.
	 * 
	 * @param size Number of rows and columns
	 * @see uk.co.jwlawson.jcluster.QuiverMatrix#QuiverMatrix(int, int)
	 */
	EquivQuiverMatrix(final int size) {
		super(size, size);
		mChecker = EquivalenceChecker.getInstance(size);
	}

	/**
	 * Create a new square matrix with {@code size} rows and columns filled with the data provided in
	 * {@code values}.
	 * 
	 * @param size Number fo rows and columns
	 * @param values Data to store in the matrix
	 * @see uk.co.jwlawson.jcluster.QuiverMatrix#QuiverMatrix(int, int, int...)
	 */
	public EquivQuiverMatrix(final int size, final int... values) {
		super(size, size, values);
		mChecker = EquivalenceChecker.getInstance(size);
	}

	/**
	 * Create a new matrix which copies the values in {@code matrix}.
	 * 
	 * @see uk.co.jwlawson.jcluster.QuiverMatrix#QuiverMatrix(QuiverMatrix)
	 * @param matrix Matrix to copy the values from
	 */
	public EquivQuiverMatrix(final QuiverMatrix matrix) {
		super(matrix);
		mChecker = EquivalenceChecker.getInstance(matrix.getNumRows());
	}

	@Override
	public void reset() {
		mHashcode = 0;
		super.reset();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		if (hashCode() != obj.hashCode()) {
			return false;
		}
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
			int[] rowSum = new int[getNumRows()];
			int[] colSum = new int[getNumCols()];
			int[] absRowSum = new int[getNumRows()];
			int[] absColSum = new int[getNumCols()];
			for (int i = 0; i < getNumRows(); i++) {
				for (int j = 0; j < getNumCols(); j++) {
					rowSum[i] += unsafeGet(i, j);
					colSum[j] += unsafeGet(i, j);
					absRowSum[i] += Math.abs(unsafeGet(i, j));
					absColSum[j] += Math.abs(unsafeGet(i, j));
				}
			}
			Arrays.sort(rowSum);
			Arrays.sort(colSum);
			Arrays.sort(absRowSum);
			Arrays.sort(absColSum);
			hash += 257 * Arrays.hashCode(rowSum);
			hash += 73 * Arrays.hashCode(colSum);
			hash += 67 * Arrays.hashCode(absRowSum);
			hash += 157 * Arrays.hashCode(absColSum);
			mHashcode = hash;
		}
		return mHashcode;
	}
}
