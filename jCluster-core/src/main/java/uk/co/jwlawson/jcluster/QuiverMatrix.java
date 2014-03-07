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

/**
 * The basic quiver with methods to mutate the quiver at its vertices.
 * 
 * @author John Lawson
 * 
 */
public class QuiverMatrix extends TLinkableAdapter<QuiverMatrix> {

	protected final IntMatrix mMatrix;
	private int mHashCode = 0;

	private QuiverMatrix(IntMatrix m) {
		if (m == null) {
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
	public QuiverMatrix(int rows, int cols) {
		this(new IntMatrix(rows, cols));
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
	public QuiverMatrix(int rows, int cols, int... values) {
		this(new IntMatrix(rows, cols, values));
	}

	protected QuiverMatrix(QuiverMatrix copy) {
		this(copy.mMatrix.copyMatrix());
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
		return mutate(k,
				new QuiverMatrix(mMatrix.getNumRows(), mMatrix.getNumCols()));
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
	public <T extends QuiverMatrix> T mutate(int k, T result) {
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
	private <S extends QuiverMatrix> void unsafeMutate(int k, S result, int rows, int cols) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				int a;
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

	public int get(int row, int col) {
		return mMatrix.get(row, col);
	}

	int unsafeGet(int i, int j) {
		return mMatrix.unsafe_get(i, j);
	}

	void unsafeSet(int i, int j, int val) {
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
		int rows = mMatrix.getNumRows();
		int cols = mMatrix.getNumCols();
		int newRows = rows + extraRows;
		int newCols = cols + extraCols;
		int[] values = new int[newRows * newCols];
		Arrays.fill(values, 0);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				values[i * newCols + j] = mMatrix.unsafe_get(i, j);
			}
		}
		QuiverMatrix result = new QuiverMatrix(newRows, newCols, values);
		return result;
	}

	public int getNumRows() {
		return mMatrix.getNumRows();
	}

	public int getNumCols() {
		return mMatrix.getNumCols();
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
		mMatrix.set(matrix.mMatrix);
	}

	@Override
	public String toString() {
		return getClass() + mMatrix.toString();
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

	/**
	 * Explicitly check whether two matrices which extend {@link QuiverMatrix} are equal as QuiverMatrices
	 * @param lhs
	 * @param rhs
	 * @return Whether the two matrices are equal as QuiverMatrices
	 */
	public static boolean areEqual(QuiverMatrix lhs, QuiverMatrix rhs){
		if (lhs.hashCode() != rhs.hashCode()) {
			return false;
		}
		return lhs.mMatrix.equals(rhs.mMatrix);
	}
}
