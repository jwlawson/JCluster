/**
 * Copyright 2014 John Lawson
 * 
 * NumericQuiver.java is part of JCluster.
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Quiver which has numeric values at each vertex which get mutated along with the matrix.
 * @author John Lawson
 * 
 */
public class NumericQuiver extends Quiver {

	private final QuiverMatrix mMatrix;
	private final double[] mValues;

	public NumericQuiver(int rows, int cols, double... data) {
		mMatrix = new QuiverMatrix(rows, cols, data);

		int max = Math.max(rows, cols);
		mValues = new double[max];
		for (int i = 0; i < max; i++) {
			mValues[i] = 1;
		}
	}

	public NumericQuiver(QuiverMatrix matrix, double[] values) {
		int max = Math.max(matrix.getNumRows(), matrix.getNumCols());
		if (max != values.length) {
			throw new IllegalArgumentException();
		}
		this.mMatrix = matrix;
		this.mValues = values;
	}

	public double getValue(int k) {
		return mValues[k];
	}

	/**
	 * Mutate the quiver at the specified vertex. Remember that the indices start at 0.
	 * 
	 * @return A new quiver which is the mutation of this one.
	 */
	@Override
	public Quiver mutate(int k) {
		QuiverMatrix newMatrix = mMatrix.mutate(k);
		double[] newValues = Arrays.copyOf(mValues, mValues.length);
		double pos = 1;
		double neg = 1;
		for (int i = 0; i < mValues.length; i++) {
			if (mMatrix.get(k, i) > 0) {
				pos = pos * mValues[i];
			} else if (mMatrix.get(k, i) < 0) {
				neg = neg * mValues[i];
			}
		}
		newValues[k] = (pos + neg) / mValues[k];

		return new NumericQuiver(newMatrix, newValues);
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
		NumericQuiver rhs = (NumericQuiver) obj;
		return new EqualsBuilder().append(mMatrix, rhs.mMatrix).append(mValues, rhs.mValues)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(23, 43).append(mMatrix).append(mValues).toHashCode();
	}
}
