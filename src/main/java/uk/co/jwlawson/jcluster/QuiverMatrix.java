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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author John Lawson
 * 
 */
public class QuiverMatrix {

	private MatrixAdaptor mMatrix;

	private QuiverMatrix(int rows, int cols) {
		mMatrix = new MatrixAdaptor(rows, cols);
	}

	public QuiverMatrix(int rows, int cols, double... values) {
		mMatrix = new MatrixAdaptor(rows, cols, true, values);
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
		int rows = mMatrix.getNumRows();
		int cols = mMatrix.getNumCols();
		if (k < 0 || k > Math.min(rows, cols)) {
			throw new ArrayIndexOutOfBoundsException(
					"Index needs to be within the unfrozen vaules of the matrix. Expected: " + 0
							+ " to " + Math.min(rows, cols) + " Actual: " + k);
		}
		QuiverMatrix result = new QuiverMatrix(rows, cols);

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				double a;
				if (i == k || j == k) {
					a = -1 * unsafe_get(i, j);
				} else {
					a = unsafe_get(i, j)
							+ (Math.abs(unsafe_get(i, k)) * unsafe_get(k, j) + unsafe_get(i, k)
									* Math.abs(unsafe_get(k, j))) / 2;
				}
				result.mMatrix.unsafe_set(i, j, a);
			}
		}

		return result;
	}

	public double get(int row, int col) {
		return mMatrix.get(row, col);
	}

	private double unsafe_get(int i, int j) {
		return mMatrix.unsafe_get(i, j);
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
		return new EqualsBuilder().append(mMatrix, rhs.mMatrix).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(19, 41).append(mMatrix).toHashCode();
	}

}
