/**
 * Copyright 2014 John Lawson
 * 
 * IntMatrix.java is part of JCluster.
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

import java.util.Arrays;

/**
 * @author John Lawson
 * 
 */
public class IntMatrix {

	private int[] mData;
	private int mRows;
	private int mCols;
	private int mHashCode = 0;

	@SuppressWarnings("unchecked")
	public IntMatrix(int rows, int cols) {
		mData = new int[rows * cols];
		mRows = rows;
		mCols = cols;
	}

	public int getNumCols() {
		return mCols;
	}

	public int getNumRows() {
		return mRows;
	}

	public int get(int row, int col) {
		if (row < 0 || row > mRows) {
			throw new IllegalArgumentException(
					"row must be non-negative and within the bounds. Expected <"
							+ mRows + " but got " + row);
		}
		if (col < 0 || col > mCols) {
			throw new IllegalArgumentException(
					"col must be non-negative and within the bounds. Expected <"
							+ mCols + " but got " + col);
		}
		return unsafe_get(row, col);
	}

	public int unsafe_get(int row, int col) {
		return mData[getIndex(row, col)];
	}

	private int getIndex(int row, int col) {
		return row * mCols + col;
	}

	@SuppressWarnings("unchecked")
	public void set(int rows, int cols, int... data) {
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

	public void set(int row, int col, int a) {
		if (row < 0 || row > mRows) {
			throw new IllegalArgumentException(
					"row must be non-negative and within the bounds. Expected <"
							+ mRows + " but got " + row);
		}
		if (col < 0 || col > mCols) {
			throw new IllegalArgumentException(
					"col must be non-negative and within the bounds. Expected <"
							+ mCols + " but got " + col);
		}
		unsafe_set(row, col, a);
	}

	public void unsafe_set(int row, int col, int a) {
		mData[getIndex(row, col)] = a;
	}

	public IntMatrix copyMatrix() {
		IntMatrix result = new IntMatrix(mRows, mCols);
		for (int i = 0; i < mData.length; i++) {
			result.mData[i] = mData[i];
		}
		return result;
	}

	public void removeNegZero() {
	}

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
		IntMatrix rhs = (IntMatrix) obj;
		if (mData.length != rhs.mData.length) {
			return false;
		}
		if (hashCode() != rhs.hashCode()) {
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

	public IntMatrix multLeft(IntMatrix mat) {
		return multLeft(mat, new IntMatrix(mat.getNumRows(), mCols));
	}

	public IntMatrix multRight(IntMatrix mat) {
		return multRight(mat, new IntMatrix(mRows, mat.getNumCols()));
	}

	public IntMatrix multLeft(IntMatrix mat, IntMatrix container) {
		// TODO Bounds check
		return unsafeMult(mat, this, container);
	}

	public IntMatrix multRight(IntMatrix mat, IntMatrix container) {
		// TODO Bounds check
		return unsafeMult(this, mat, container);
	}

	private IntMatrix unsafeMult(IntMatrix left, IntMatrix right,
			IntMatrix container) {
		int tmp;
		for (int i = 0; i < left.getNumRows(); i++) {
			for (int j = 0; j < right.getNumCols(); j++) {
				tmp = 0;
				for (int k = 0; k < left.getNumCols(); k++) {
					tmp += left.unsafe_get(i, k) * right.unsafe_get(k, j);
				}
				container.unsafe_set(i, j, tmp);
			}
		}

		return container;
	}

}
