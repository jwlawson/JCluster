/**
 * Copyright 2014 John Lawson
 * 
 * QuiverMatrix.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
 * The basic quiver with methods to mutate the quiver at its vertices.
 * 
 * @author John Lawson
 * 
 */
public class QuiverMatrix extends IntMatrix {

	private QuiverMatrix(IntMatrix m) {
		super(m);
	}

	/**
	 * Create a new QuiverMatrix with set number of rows and columns but no data. The matrix will by
	 * default be filled with zeros.
	 * 
	 * @param rows Number of rows
	 * @param cols Number of columns
	 */
	public QuiverMatrix(int rows, int cols) {
		this(new IntMatrix(rows, cols));
	}

	/**
	 * Create a new QuiverMatrix with set number of rows and columns. The provided array must have the
	 * correct number of entries and be in row-major form.
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
		this(copy.copyMatrix());
	}

	/**
	 * Mutates the matrix at the k-th entry and returns the new mutated matrix. This does not change
	 * the initial matrix.
	 * 
	 * Remember that the indexing starts at 0.
	 * 
	 * @param k Index to mutate on.
	 * @return New mutated matrix.
	 */
	public QuiverMatrix mutate(int k) {
		return mutate(k, new QuiverMatrix(this.getNumRows(), this.getNumCols()));
	}

	/**
	 * Mutates the matrix at the k-th entry and returns the new mutated matrix. This does not change
	 * the initial matrix.
	 * 
	 * Remember that the indexing starts at 0.
	 * 
	 * @param k Index to mutate on.
	 * @param result The matrix to insert the new matrix. Ensure it is the right size.
	 * @return New mutated matrix.
	 */
	public <T extends QuiverMatrix> T mutate(int k, T result) {
		checkParam(result == null, "Do not call this method with null - use the one parameter method.");
		int rows = getNumRows();
		int cols = getNumCols();
		checkParam(
				k < 0 || k > Math.min(rows, cols),
				"Index needs to be within the unfrozen vaules of the matrix. Expected: %d to %d Actual: %d",
				0, Math.min(rows, cols), k);
		checkParam(rows != result.getNumRows() || cols != result.getNumCols(),
				"Incorrectly sized matrix passed. Expected: %d x %d. Actual: %d x %d", rows, cols,
				result.getNumRows(), result.getNumCols());
		unsafeMutate(k, result, rows, cols);
		return result;
	}

	/**
	 * Mutates this matrix at the k-th entry and put the result into the provided matrix. No bound
	 * checks or size checks are performed.
	 * 
	 * Remember that the indexing starts at 0.
	 * 
	 * @param k Index to mutate on.
	 * @param result The matrix to insert the new matrix. Ensure it is the right size as no checks are
	 *        done.
	 * @return New mutated matrix.
	 */
	private <S extends QuiverMatrix> void unsafeMutate(int k, S result, int rows, int cols) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				int a;
				if (i == k || j == k) {
					a = -1 * unsafeGet(i, j);
				} else {
					a =
							unsafeGet(i, j)
									+ (Math.abs(unsafeGet(i, k)) * unsafeGet(k, j) + unsafeGet(i, k)
											* Math.abs(unsafeGet(k, j))) / 2;
				}
				result.unsafeSet(i, j, a);
			}
		}
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
		int rows = this.getNumRows();
		int cols = this.getNumCols();
		int newRows = rows + extraRows;
		int newCols = cols + extraCols;
		int[] values = new int[newRows * newCols];
		Arrays.fill(values, 0);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				values[i * newCols + j] = this.unsafeGet(i, j);
			}
		}
		QuiverMatrix result = new QuiverMatrix(newRows, newCols, values);
		return result;
	}

	/**
	 * Creates a copy of this matrix.
	 * 
	 * @return A new matrix which contains the same entries as the first.
	 */
	public QuiverMatrix copy() {
		return new QuiverMatrix(copyMatrix());
	}

	/**
	 * Check if the QuiverMatrix is mutation-infinite. This only checks the current matrix, not any
	 * others in the mutation class.
	 * 
	 * @return true if this is mutation-infinite in its current form
	 */
	public boolean isInfinite() {
		for (int i = 0; i < getNumRows(); i++) {
			for (int j = 0; j < getNumCols(); j++) {
				int val = unsafeGet(i, j);
				if (val >= 3 || val <= -3) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public QuiverMatrix submatrix(int i, int j) {
		return submatrix(i, j, new QuiverMatrix(getNumRows() - 1, getNumCols() - 1));
	}

	private void checkParam(boolean expression, String formatString, Object... formatParams) {
		if (expression) {
			String error = String.format(formatString, formatParams);
			throw new IllegalArgumentException(error);
		}
	}
}
