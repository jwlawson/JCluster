/**
 * Copyright 2014 John Lawson
 * 
 * QuiverMatrix.java is part of JCluster.
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

import gnu.trove.list.TLinkableAdapter;

import java.util.Arrays;

import org.ejml.data.DenseMatrix64F;

/**
 * The basic quiver with methods to mutate the quiver at its vertices.
 * 
 * @author John Lawson
 * 
 */
public class QuiverMatrix extends TLinkableAdapter<QuiverMatrix> {

	private final MatrixAdaptor mMatrix;
	private int mHashCode = 0;

	private QuiverMatrix(MatrixAdaptor m) {
		if(m == null){
			throw new IllegalArgumentException("Cannot create matrix from null");
		}
		mMatrix = m;
	}

	/**
	 * Create a new QuiverMatrix with set number of rows and columns but no
	 * data. The matrix will by default be filled with zeros.
	 * 
	 * @param rows Number of rows
	 * @param cols Number of columns
	 */
	QuiverMatrix(int rows, int cols) {
		this(new MatrixAdaptor(rows, cols));
	}

	/**
	 * Create a new QuiverMatrix with set number of rows and columns. The
	 * provided array must have the correct number of entries and be in
	 * row-major form.
	 * 
	 * That is {@code row 1},{row 2}, ... } }.
	 * 
	 * @param rows Number of rows
	 * @param cols Number of columns
	 * @param values Array of entries in the matrix
	 */
	public QuiverMatrix(int rows, int cols, double... values) {
		this(new MatrixAdaptor(rows, cols, true, values));
	}

	/**
	 * Mutates the matrix at the k-th entry and returns the new mutated matrix.
	 * This does not change the initial matrix.
	 * 
	 * Remember that the indexing starts at 0.
	 * 
	 * @param k Index to mutate on.
	 * @return New mutated matrix.
	 */
	public QuiverMatrix mutate(int k) {
		return mutate(k, new QuiverMatrix(mMatrix.numRows, mMatrix.numCols));
	}

	/**
	 * Mutates the matrix at the k-th entry and returns the new mutated matrix.
	 * This does not change the initial matrix.
	 * 
	 * Remember that the indexing starts at 0.
	 * 
	 * @param k Index to mutate on.
	 * @param result The matrix to insert the new matrix. Ensure it is the right
	 *            size.
	 * @return New mutated matrix.
	 */
	public QuiverMatrix mutate(int k, QuiverMatrix result) {
		if (result == null) {
			throw new RuntimeException(
					"Do not call this method with null - use the one parameter method.");
		}
		int rows = getNumRows();
		int cols = getNumCols();
		if (k < 0 || k > Math.min(rows, cols)) {
			throw new IllegalArgumentException(
					"Index needs to be within the unfrozen vaules of the matrix. Expected: "
							+ 0 + " to " + Math.min(rows, cols) + " Actual: "
							+ k);
		}
		if (rows != result.getNumRows() || cols != result.getNumCols()) {
			throw new IllegalArgumentException(
					"Incorrectly sized matrix passed. Expected: " + rows
							+ " x " + cols + ". Actual: " + result.getNumRows()
							+ " x " + result.getNumCols());
		}
		unsafeMutate(k, result, rows, cols);
		return result;
	}

	/**
	 * Mutates this matrix at the k-th entry and put the result into the
	 * provided matrix. No bound checks or size checks are performed.
	 * 
	 * Remember that the indexing starts at 0.
	 * 
	 * @param k Index to mutate on.
	 * @param result The matrix to insert the new matrix. Ensure it is the right
	 *            size as no checks are done.
	 * @return New mutated matrix.
	 */
	private void unsafeMutate(int k, QuiverMatrix result, int rows, int cols) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				double a;
				if (i == k || j == k) {
					a = -1 * unsafeGet(i, j);
				} else {
					a = unsafeGet(i, j)
							+ (Math.abs(unsafeGet(i, k)) * unsafeGet(k, j) + unsafeGet(
									i, k) * Math.abs(unsafeGet(k, j))) / 2;
				}
				result.mMatrix.unsafe_set(i, j, a);
			}
		}
		result.mMatrix.removeNegZero();
	}

	public double get(int row, int col) {
		return mMatrix.get(row, col);
	}

	double unsafeGet(int i, int j) {
		return mMatrix.unsafe_get(i, j);
	}

	void unsafeSet(int i, int j, double val) {
		mMatrix.unsafe_set(i, j, val);
	}

	/**
	 * Provides a new matrix which has added rows and columns filled with zeros.
	 * 
	 * This can be used to add vertices to the quiver.
	 * 
	 * @param extraRows Number of extra rows to add
	 * @param extraCols Number of extra columns to add
	 * @return The enlarged matrix
	 */
	public QuiverMatrix enlargeMatrix(int extraRows, int extraCols) {
		int rows = mMatrix.numRows;
		int cols = mMatrix.numCols;
		int newRows = rows + extraRows;
		int newCols = cols + extraCols;
		double[] values = new double[newRows * newCols];
		Arrays.fill(values, 0d);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				values[i * newCols + j] = mMatrix.unsafe_get(i, j);
			}
		}
		QuiverMatrix result = new QuiverMatrix(newRows, newCols, values);
		return result;
	}

	public int getNumRows() {
		return mMatrix.numRows;
	}

	public int getNumCols() {
		return mMatrix.numCols;
	}

	public void reset() {
		mMatrix.reset();
		mHashCode = 0;
	}

	/**
	 * Creates a copy of this matrix.
	 * 
	 * @return A new matrix which contains the same entries as the first.
	 */
	public QuiverMatrix copy() {
		return new QuiverMatrix(mMatrix.copyMatrix());
	}

	/**
	 * Set this matrix to match the values in the provided matrix.
	 * 
	 * @param matrix The matrix to copy the values from.
	 */
	public void set(QuiverMatrix matrix) {
		reset();
		mMatrix.set(matrix.getNumRows(), matrix.getNumCols(), true,
				matrix.mMatrix.data);
	}

	@Override
	public String toString() {
		return mMatrix.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		QuiverMatrix rhs = (QuiverMatrix) obj;
		if (hashCode() != rhs.hashCode()) {
			return false;
		}
		return mMatrix.equals(rhs.mMatrix);
	}

	@Override
	public int hashCode() {
		int hash = mHashCode; // Extra variable makes this thread safe
		if (hash == 0) {
			hash = mMatrix.hashCode();
			mHashCode = hash;
		}
		return mHashCode;
	}

	public class EquivalenceChecker {

		private DenseMatrix64F[] mPermMatrices;

		// TODO Fancy checking of size and create specific checker for different
		// sizes.

		public EquivalenceChecker(int size) {
			int fac = factorial(size);
			mPermMatrices = new DenseMatrix64F[fac];
			for (int i = 0; i < fac; i++) {
				// Generate permutation matrices
			}
		}

		private int factorial(int num) {
			if (num == 1) {
				return 1;
			}
			return num * factorial(num - 1);
		}

		public boolean areEquivalent(DenseMatrix64F a, DenseMatrix64F b) {
			for (DenseMatrix64F p : mPermMatrices) {
				// Check if PA == BP
				// or PAP^(-1) == B
			}
			return false;
		}
	}
}
