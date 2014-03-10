/**
 * Copyright 2014 John Lawson
 * 
 * EquivQuiverMatrix.java is part of JCluster.
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author John Lawson
 * 
 */
public class EquivQuiverMatrix extends QuiverMatrix {

	private EquivalenceChecker mChecker;
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
		if (obj == null) return false;
		if (this == obj) return true;
		if (getClass() != obj.getClass()) return false;

		EquivQuiverMatrix rhs = (EquivQuiverMatrix) obj;
		return mChecker.areEquivalent(this, rhs);
	}

	/*
	 * Because the hashcode must be the same for each matrix with permuted rows,
	 * this method will likely create lots of hash collisions.
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

	public static class EquivalenceChecker {

		private static Map<Integer, EquivalenceChecker> mInstanceMap = new HashMap<Integer, EquivalenceChecker>();

		private IntMatrix[] mPermMatrices;
		private IntMatrix mMatrixPA;
		private IntMatrix mMatrixBP;

		public static synchronized EquivalenceChecker getInstance(int size) {
			EquivalenceChecker result = mInstanceMap.get(size);
			if (result == null) {
				result = new EquivalenceChecker(size);
				mInstanceMap.put(size, result);
			}
			return result;
		}

		/**
		 * Class to test whether matrices are equivalent up to permutation of
		 * their rows and columns. Constructor creates all permutation matrices
		 * of the required size, so should not be instantiated many times but
		 * rather cached.
		 * 
		 * @param size The size of the matrices which will be checked for
		 *            equivalence
		 */
		private EquivalenceChecker(int size) {
			setPermutations(size);

			mMatrixPA = new IntMatrix(size, size);
			mMatrixBP = new IntMatrix(size, size);
		}

		private void setPermutations(int size) {
			int fac = factorial(size);
			mPermMatrices = new IntMatrix[fac];
			for (int i = 0; i < fac; i++) {
				int[] vals = getPermutationValues(size, i);
				mPermMatrices[i] = new IntMatrix(size, size);
				for (int j = 0; j < size; j++) {
					mPermMatrices[i].set(j, vals[j], 1);
				}
			}
		}

		private int[] getPermutationValues(int size, int i) {
			int[] vals = new int[size];
			int id = i;
			for (int j = 0; j < size; j++) {
				vals[j] = id % (size - j);
				// Want to prevent having 1 twice in a column, so shift
				// the value across dependent on the previous values.
				for (int k = 0; k < j; k++) {
					if (vals[j] >= vals[k]) {
						vals[j]++;
					}
				}
				// Ensure there are no clashes
				boolean shifted = false;
				do {
					shifted = false;
					for (int k = 0; k < j; k++) {
						if (vals[j] == vals[k]) {
							vals[j]++;
							shifted = true;
						}
					}
				} while (shifted);
				id = id / (size - j);
			}
			return vals;
		}

		private int factorial(int num) {
			if (num == 1) {
				return 1;
			}
			return num * factorial(num - 1);
		}

		/**
		 * Check whether two matrices are equivalent up to permutations of the
		 * rows and columns.
		 * 
		 * @param a The first matrix
		 * @param b The second matrix
		 * @return Whether the two are equivalent
		 */
		public boolean areEquivalent(IntMatrix a, IntMatrix b) {
			for (IntMatrix p : mPermMatrices) {
				// Check if PA == BP
				// or PAP^(-1) == B
				synchronized (this) {
					a.multLeft(p, mMatrixPA);
					b.multRight(p, mMatrixBP);
					if (mMatrixBP.equals(mMatrixPA)) {
						return true;
				}
				}
			}
			return false;
		}

	}

}
