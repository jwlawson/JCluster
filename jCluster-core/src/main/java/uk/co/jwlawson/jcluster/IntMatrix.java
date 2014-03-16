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
		checkParam(data.length != rows * cols, "Number of entries must match the size of the matrix");
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
	 * @throws IllegalArgumentException if the row or column index is less than 0, or greater than the
	 *         number of rows or columns.
	 */
	public int get(int row, int col) {
		checkParam(row < 0 || row > mRows,
				"row must be non-negative and within the bounds. Expected < %d but got %d", mRows, row);
		checkParam(col < 0 || col > mCols,
				"col must be non-negative and within the bounds. Expected < %d but got %d", mCols, col);
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
	 * @throws IllegalArgumentException if the array is the wrong size compared to the number of rows
	 *         and columns
	 */
	public void set(int rows, int cols, int... data) {
		checkParam(data.length != rows * cols, "Number of entries must match the size of the matrix");
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
		checkParam(row < 0 || row > mRows,
				"row must be non-negative and within the bounds. Expected < %d but got %d", mRows, row);
		checkParam(col < 0 || col > mCols,
				"col must be non-negative and within the bounds. Expected < %d but got %d", mCols, col);
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
	 * IntMatrices are meant to be fairly immutable, so the hashcode is cached. Reset should be called
	 * each time that the matrix is changed.
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
			hashcode = 113;
			for (int i = 0; i < mData.length; i++) {
				hashcode *= 523;
				hashcode += mData[i];
			}
			mHashCode = hashcode;
		}
		return mHashCode;
	}

	/**
	 * Multiply this matrix on the left by {@code mat} and return a new matrix containing the result.
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
	 * Multiply this matrix on the right by {@code mat} and return a new matrix containing the result.
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
	public <T extends IntMatrix> T multLeft(IntMatrix mat, T container) {
		checkParam(mat == null || container == null, "Cannot multiply null matrices");
		checkParam(mat.getNumCols() != this.getNumRows(),
				"Matrix to multiply is wrong size. Expected %d columns but have %d", getNumRows(),
				mat.getNumCols());
		checkParam(
				container.getNumRows() != mat.getNumRows() || container.getNumCols() != getNumCols(),
				"Container is wrong size. Expected %d x %d but have %d x %d", mat.getNumRows(),
				getNumCols(), container.getNumRows(), container.getNumCols());
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
	public <T extends IntMatrix> T multRight(IntMatrix mat, T container) {
		checkParam(mat == null || container == null, "Cannot multiply null matrices");
		checkParam(getNumCols() != mat.getNumRows(),
				"Matrix to multiply is wrong size. Expected %d rows but have %d", getNumCols(),
				mat.getNumRows());
		checkParam(
				container.getNumRows() != getNumRows() || container.getNumCols() != mat.getNumCols(),
				"Container is wrong size. Expected %d x %d but have %d x %d", getNumRows(),
				mat.getNumCols(), container.getNumRows(), container.getNumCols());
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
	private <T extends IntMatrix> T unsafeMult(IntMatrix left, IntMatrix right, T container) {
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

	/**
	 * Create a new matrix which contains the values on this matrix, except those in the specified row
	 * and column.
	 * 
	 * @param row Row to remove
	 * @param col Column to remove
	 * @return New matrix which is the submatrix of this one
	 * @throws IllegalArgumentException if the row or column index are outside the bounds of the
	 *         matrix
	 */
	public IntMatrix submatrix(int row, int col) {
		return submatrix(row, col, new IntMatrix(getNumRows() - 1, getNumCols() - 1));
	}

	/**
	 * Get the submatrix of this matrix by removing the specified row and column. The submatrix is
	 * returned in the provided matrix.
	 * 
	 * @param row Row to remove
	 * @param col Column to remove
	 * @param result Submatrix returned
	 * @return Submatrix with removed row and column
	 * @throws IllegalArgumentException if the parameters do not match those expected.
	 */
	public <T extends IntMatrix> T submatrix(int row, int col, T result) {
		checkParam(result.mRows != (mRows - 1),
				"Provided container matrix of the wrong size. Expected %d rows but got %d.", mRows - 1,
				result.mRows);
		checkParam(result.mCols != (mCols - 1),
				"Provided container matrix of the wrong size. Expected %d columns but got %d.", mCols - 1,
				result.mCols);
		checkParam(row < 0 || row >= mRows,
				"Row index not contained in the matrix. Expected 0 < row < %d but got %d", getNumRows(),
				row);
		checkParam(col < 0 || col >= mCols,
				"Column index not contained in the matrix. Expected 0 < col < %d but got %d", getNumCols(),
				col);
		return unsafeSubmatrix(row, col, result);
	}

	/**
	 * Get the submatrix of this by removing the specified row and column. No bounds checking is done,
	 * so only use if you are certain that the parameters are valid.
	 * 
	 * @param row Row to remove
	 * @param col Column to remove
	 * @param result Submatrix to return
	 * @return Submatrix with row and column removed
	 */
	private <T extends IntMatrix> T unsafeSubmatrix(int row, int col, T result) {
		result.reset();
		int resInd = 0;
		int origInd = 0;
		while (resInd < result.mRows * result.mCols) {
			boolean changed;
			do {
				changed = false;
				if (origInd == row * mCols) {
					origInd += mCols;
					changed = true;
				}
				if (origInd % mCols == col) {
					origInd++;
					changed = true;
				}
			} while (changed);
			result.mData[resInd++] = mData[origInd++];
		}
		return result;
	}

	/**
	 * Check parameters by providing an expression which tests their validity. If the expression
	 * evaluates to true then a new IllegalArgumentException is thrown containing the provided
	 * formatted string.
	 * 
	 * @param expression Expression to check
	 * @param formatString String to format if the expression is true
	 * @param formatParams Objects to format the string with
	 * @throws IllegalArgumentException if {@code expression} is true
	 */
	private void checkParam(boolean expression, String formatString, Object... formatParams) {
		if (expression) {
			String error = String.format(formatString, formatParams);
			throw new IllegalArgumentException(error);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[ ");
		for (int i = 0; i < mRows; i++) {
			sb.append("{");
			for (int j = 0; j < mCols; j++) {
				sb.append(" ");
				sb.append(mData[getIndex(i, j)]);
			}
			sb.append(" } ");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Test whether two matrices are equal as IntMatrix objects.
	 * 
	 * <p>
	 * This can be useful to check whether two objects which extend IntMatrix are equal without the
	 * added structure that their class provides.
	 * 
	 * @param lhs First matrix to check
	 * @param rhs Second matrix to check
	 * @return true if the matrices are equal as IntMatrix objects
	 */
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

	/**
	 * Get the IntMatrix hashcode of the supplied matrix.
	 * 
	 * @param a Matrix to calculate the hashcode of
	 * @return Hashcode of the matrix as if it were an IntMatrix
	 */
	public static int hashCode(IntMatrix a) {
		int hashcode = 113;
		for (int i = 0; i < a.mData.length; i++) {
			hashcode *= 523;
			hashcode += a.mData[i];
		}
		return hashcode;
	}
}
