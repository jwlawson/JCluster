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

import java.util.Arrays;

import nf.fr.eraasoft.pool.ObjectPool;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic quiver with methods to mutate the quiver at its vertices.
 * @author John Lawson
 * 
 */
public class QuiverMatrix {
	
	private static final ObjectPool<EqualsBuilder> sBuilderPool = Pools.getEqualsBuilerPool();

	private final MatrixAdaptor mMatrix;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private int mHashCode = Integer.MAX_VALUE;

	private QuiverMatrix(MatrixAdaptor m) {
		mMatrix = m;
	}

	QuiverMatrix(int rows, int cols) {
		mMatrix = new MatrixAdaptor(rows, cols);
	}

	public QuiverMatrix(int rows, int cols, double... values) {
		mMatrix = new MatrixAdaptor(rows, cols, true, values);
	}

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
	 * @param result The matrix to insert the new matrix. Ensure it is the right size.
	 * @return New mutated matrix.
	 */
	public QuiverMatrix mutate(int k, QuiverMatrix result) {
		if(result == null){
			throw new RuntimeException("Do not call this method with null - use the one parameter method.");
		}
		int rows = getNumRows();
		int cols = getNumCols();
		if (k < 0 || k > Math.min(rows, cols)) {
			throw new IllegalArgumentException(
					"Index needs to be within the unfrozen vaules of the matrix. Expected: " + 0
							+ " to " + Math.min(rows, cols) + " Actual: " + k);
		}
		if (rows != result.getNumRows() || cols != result.getNumCols()) {
			throw new IllegalArgumentException("Incorrectly sized matrix passed. Expected: " + rows
					+ " x " + cols + ". Actual: " + result.getNumRows() + " x "
					+ result.getNumCols());
		}
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				double a;
				if (i == k || j == k) {
					a = -1 * unsafeGet(i, j);
				} else {
					a = unsafeGet(i, j)
							+ (Math.abs(unsafeGet(i, k)) * unsafeGet(k, j) + unsafeGet(i, k)
									* Math.abs(unsafeGet(k, j))) / 2;
				}
				result.mMatrix.unsafe_set(i, j, a);
			}
		}

		return result;
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
		mHashCode = Integer.MAX_VALUE;
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
		mMatrix.set(matrix.getNumRows(), matrix.getNumCols(), true, matrix.mMatrix.data);
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
//		EqualsBuilder builder = null;
//		try{
//			builder = sBuilderPool.getObj();
//			return builder.append(mMatrix, rhs.mMatrix).isEquals();
//		} catch (PoolException e) {
//			log.error("Error getting equals builder from pool" + e.getMessage(), e);
			return new EqualsBuilder().append(mMatrix, rhs.mMatrix).isEquals();
//		} finally {
//			if(builder != null){
//				sBuilderPool.returnObj(builder);
//			}
//		}
	}

	@Override
	public int hashCode() {
		if(mHashCode == Integer.MAX_VALUE){
			mHashCode =  new HashCodeBuilder(19, 41).append(mMatrix).toHashCode();
		}
		return mHashCode;
	}

	public void zero() {
		mMatrix.zero();
	}

}
