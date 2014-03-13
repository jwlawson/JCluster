/**
 * Copyright 2014 John Lawson
 * 
 * IntMatrix.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
 * Matrix of integers.
 * 
 * @author John Lawson
 * 
 */
public class IntMatrix {

	private int[] mData;
	private int mRows;
	private int mCols;
	private int mHashCode = 0;

	/**
	 * Create a new IntMatrix of the specified size which is filled with 0.
	 * 
	 * @param rows Number of rows in the matrix
	 * @param cols NUmber of columns in the matrix
	 */
	public IntMatrix(int rows, int cols) {
		mData = new int[rows * cols];
		mRows = rows;
		mCols = cols;
	}

	/**
	 * Create a new IntMatrix containing the data provided. The data should collected into rows, so
	 * the first row is given then the second and so on.
	 * 
	 * @param rows Number of rows in the matrix
	 * @param cols Number of columns in the matrix
	 * @param data Data contained in the matrix
	 */
	public IntMatrix(int rows, int cols, int... data) {
		if (data.length != rows * cols) {
			throw new IllegalArgumentException(
					"Number of entries must match the size of the matrix");
		}
		mData = new int[rows * cols];
		mRows = rows;
		mCols = cols;
		for (int i = 0; i < data.length; i++) {
			mData[i] = data[i];
		}
	}

	/**
	 * Create a new IntMatrix which is a copy of the provided matrix.
	 * 
	 * @param m Matrix to copy
	 */
	protected IntMatrix(IntMatrix m) {
		this(m.mRows, m.mCols, m.mData);
	}

	public int getNumCols() {
		return mCols;
	}

	public int getNumRows() {
		return mRows;
	}

	/**
	 * Get the value stored in the matrix at specified position.
	 * 
	 * @param row Row position
	 * @param col Column position
	 * @return Value stored in matrix
	 * @throws IllegalArgumentException if the row or column index is less than 0, or greater than
	 *         the number of rows or columns.
	 */
	public int get(int row, int col) {
		if (row < 0 || row > mRows) {
			throw new IllegalArgumentException(
					"row must be non-negative and within the bounds. Expected <" + mRows
							+ " but got " + row);
		}
		if (col < 0 || col > mCols) {
			throw new IllegalArgumentException(
					"col must be non-negative and within the bounds. Expected <" + mCols
							+ " but got " + col);
		}
		return unsafeGet(row, col);
	}

	/**
	 * Get the value stored in the matrix at requested position. No bounds checks are done, so this
	 * method should only be used if the position is guaranteed to be valid.
	 * 
	 * @param row Row position
	 * @param col Column position
	 * @return Value stored in matrix
	 */
	public int unsafeGet(int row, int col) {
		return mData[getIndex(row, col)];
	}

	/**
	 * Get the value stored in the underlying array at the specified index. Index can be computed as
	 * {@code row * numCols + col}.
	 * 
	 * @param index Index to get
	 * @return The value stored at index
	 * @throws ArrayIndexOutOfBoundsException if index is out of bounds
	 */
	public int unsafeGet(int index) {
		return mData[index];
	}

	/**
	 * Convert the row and column indices to the index in the array storing the data.
	 * 
	 * @param row Row index
	 * @param col Column index
	 * @return Index in array
	 */
	private int getIndex(int row, int col) {
		return row * mCols + col;
	}

	/**
	 * Get a row of the matrix as an array.
	 * 
	 * @param row The index of the row
	 * @return An array containing values of the row
	 */
	public int[] getRow(int row) {
		int[] result = new int[mCols];
		int count = row * mCols;
		for (int j = 0; j < mCols; j++) {
			result[j] = mData[count++];
		}
		return result;
	}

	/**
	 * Get a column of the matrix as an array.
	 * 
	 * @param col The index of the column
	 * @return An array containing values of the column
	 */
	public int[] getCol(int col) {
		int[] result = new int[mRows];
		int count = col;
		for (int j = 0; j < mRows; j++) {
			result[j] = mData[count];
			count += mCols;
		}
		return result;
	}

	/**
	 * Copy the values from the supplied matrix into this one.
	 * 
	 * @param matrix Matrix to copy
	 */
	public void set(IntMatrix matrix) {
		set(matrix.mRows, matrix.mCols, matrix.mData);
	}

	/**
	 * Set the values of this matrix to be those provided. The array of data should be arranged with
	 * rows together, so the first row is given, then the second and so on.
	 * 
	 * @param rows Number of rows in the matrix
	 * @param cols Number of columns in the matrix
	 * @param data Array of values to store in the matrix.
	 * @throws IllegalArgumentException if the array is the wrong size compared to the number of
	 *         rows and columns
	 */
	public void set(int rows, int cols, int... data) {
		if (data.length != rows * cols) {
			throw new IllegalArgumentException(
					"Number of entries must match the size of the matrix");
		}
		reset();
		mData = new int[rows * cols];
		mRows = rows;
		mCols = cols;
		for (int i = 0; i < data.length; i++) {
			mData[i] = data[i];
		}
	}

	/**
	 * Set the value in the matrix at the specified position.
	 * 
	 * @param row Row index
	 * @param col Column index
	 * @param a New value to store
	 * @throws IllegalArgumentException if the indices are not valid for this matrix
	 */
	public void set(int row, int col, int a) {
		if (row < 0 || row > mRows) {
			throw new IllegalArgumentException(
					"row must be non-negative and within the bounds. Expected <" + mRows
							+ " but got " + row);
		}
		if (col < 0 || col > mCols) {
			throw new IllegalArgumentException(
					"col must be non-negative and within the bounds. Expected <" + mCols
							+ " but got " + col);
		}
		unsafeSet(row, col, a);
	}

	/**
	 * Sets the value in the matrix at the specified position with out carrying out any bounds
	 * checking. This should only be used if the indices are guaranteed to be valid.
	 * 
	 * @param row Row index
	 * @param col Columns index
	 * @param a new value to store
	 */
	public void unsafeSet(int row, int col, int a) {
		mData[getIndex(row, col)] = a;
	}

	/**
	 * Create a new matrix which contains a copy of the data stored in this one.
	 * 
	 * @return A new copy of this matrix
	 */
	public IntMatrix copyMatrix() {
		IntMatrix result = new IntMatrix(mRows, mCols);
		for (int i = 0; i < mData.length; i++) {
			result.mData[i] = mData[i];
		}
		return result;
	}

	/**
	 * IntMatrices are meant to be fairly immutable, so the hashcode is cached. Reset should be
	 * called each time that the matrix is changed.
	 */
	public void reset() {
		mHashCode = 0;
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
		if (hashCode() != obj.hashCode()) {
			return false;
		}
		IntMatrix rhs = (IntMatrix) obj;
		if (mData.length != rhs.mData.length) {
			return false;
		}
		for (int i = 0; i < mData.length; i++) {
			if (mData[i] != rhs.mData[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashcode = mHashCode;
		if (hashcode == 0) {
			hashcode = Arrays.hashCode(mData);
			mHashCode = hashcode;
		}
		return mHashCode;
	}

	/**
	 * Multiply this matrix on the left by {@code mat} and return a new matrix containing the
	 * result.
	 * 
	 * <p>
	 * i.e. {@code result = mat * this}
	 * 
	 * @param mat Matrix to multiply with
	 * @return A new matrix with the result in
	 */
	public IntMatrix multLeft(IntMatrix mat) {
		return multLeft(mat, new IntMatrix(mat.getNumRows(), mCols));
	}

	/**
	 * Multiply this matrix on the right by {@code mat} and return a new matrix containing the
	 * result.
	 * 
	 * <p>
	 * i.e. {@code result = this * mat}
	 * 
	 * @param mat Matrix to multiply with
	 * @return A new matrix with the result in
	 */
	public IntMatrix multRight(IntMatrix mat) {
		return multRight(mat, new IntMatrix(mRows, mat.getNumCols()));
	}

	/**
	 * Multiply this matrix on the left by {@code mat} and store the result in {@code container}
	 * 
	 * <p>
	 * i.e. {@code container = mat * this}
	 * 
	 * @param mat Matrix to multiply with
	 * @param container Matrix to store the result in
	 * @return The container with the result in
	 */
	public IntMatrix multLeft(IntMatrix mat, IntMatrix container) {
		if (mat == null || container == null) {
			throw new IllegalArgumentException("Cannot multiply null matrices");
		}
		if (mat.getNumCols() != this.getNumRows()) {
			String error =
					String.format(
							"Matrix to multiply is wrong size. Expected %d columns but have %d",
							getNumRows(), mat.getNumCols());
			throw new IllegalArgumentException(error);
		}
		if (container.getNumRows() != mat.getNumRows() || container.getNumCols() != getNumCols()) {
			String error =
					String.format("Container is wrong size. Expected %d x %d but have %d x %d",
							mat.getNumRows(), getNumCols(), container.getNumRows(),
							container.getNumCols());
			throw new IllegalArgumentException(error);
		}
		return unsafeMult(mat, this, container);
	}

	/**
	 * Multiply this matrix on the right by {@code mat} and store the result in {@code container}
	 * 
	 * <p>
	 * i.e. {@code container = this * mat}
	 * 
	 * @param mat Matrix to multiply with
	 * @param container Matrix to store the result in
	 * @return The container with the result in
	 */
	public IntMatrix multRight(IntMatrix mat, IntMatrix container) {
		if (mat == null || container == null) {
			throw new IllegalArgumentException("Cannot multiply null matrices");
		}
		if (getNumCols() != mat.getNumRows()) {
			String error =
					String.format("Matrix to multiply is wrong size. Expected %d rows but have %d",
							getNumCols(), mat.getNumRows());
			throw new IllegalArgumentException(error);
		}
		if (container.getNumRows() != getNumRows() || container.getNumCols() != mat.getNumCols()) {
			String error =
					String.format("Container is wrong size. Expected %d x %d but have %d x %d",
							getNumRows(), mat.getNumCols(), container.getNumRows(),
							container.getNumCols());
			throw new IllegalArgumentException(error);
		}
		return unsafeMult(this, mat, container);
	}

	/**
	 * Multiply together two matrices and store the result into container. i.e.
	 * 
	 * {@code container = left * right}
	 * 
	 * <p>
	 * No bounds checking is done, so this method should only be used if the matrices are guaranteed
	 * to be the right size.
	 * 
	 * @param left Matrix to multiply on the left
	 * @param right Matrix to multiply on the right
	 * @param container Matrix to store the result in
	 * @return container with the result
	 */
	private IntMatrix unsafeMult(IntMatrix left, IntMatrix right, IntMatrix container) {
		container.reset();
		int colIncrement = right.getNumRows();
		int leftInd;
		int leftIndStart = 0;
		int rightInd;
		int calcInd = 0;
		for (int i = 0; i < left.getNumRows(); i++) {
			for (int j = 0; j < right.getNumCols(); j++) {
				leftInd = leftIndStart;
				rightInd = j;
				container.mData[calcInd] = 0;
				while (leftInd < leftIndStart + left.getNumCols()) {
					container.mData[calcInd] += left.mData[leftInd] * right.mData[rightInd];
					leftInd++;
					rightInd += colIncrement;
				}
				calcInd++;
			}
			leftIndStart += left.getNumCols();
		}
		return container;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mRows; i++) {
			for (int j = 0; j < mCols; j++) {
				sb.append(mData[getIndex(i, j)]);
				sb.append(" ");
			}
			if (i != mRows - 1) {
				sb.append(System.lineSeparator());
			}
		}
		return sb.toString();
	}

	public static boolean areEqual(IntMatrix lhs, IntMatrix rhs) {
		if (lhs == null && rhs == null) {
			return true;
		}
		if (lhs == null || rhs == null) {
			return false;
		}
		if (lhs == rhs) {
			return true;
		}
		if (lhs.mData.length != rhs.mData.length) {
			return false;
		}
		for (int i = 0; i < lhs.mData.length; i++) {
			if (lhs.mData[i] != rhs.mData[i]) {
				return false;
			}
		}
		return true;
	}

	public static int hashCode(IntMatrix a) {
		return Arrays.hashCode(a.mData);
	}
}
