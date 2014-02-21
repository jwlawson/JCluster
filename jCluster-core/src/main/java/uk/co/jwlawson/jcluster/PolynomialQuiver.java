/**
 * Copyright 2014 John Lawson
 * 
 * PolynomialQuiver.java is part of JCluster.
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

import com.perisic.ring.PolynomialRing;
import com.perisic.ring.QuotientField;
import com.perisic.ring.Ring;
import com.perisic.ring.RingElt;

/**
 * A quiver which has polynomials at each vertex.
 * @author John Lawson
 * 
 */
public class PolynomialQuiver extends Quiver {

	private final QuiverMatrix mMatrix;
	private Ring mRing;
	private RingElt[] mPolynomials;

	public PolynomialQuiver(int rows, int cols, double... data) {
		mMatrix = new QuiverMatrix(rows, cols, data);

		int min = Math.min(rows, cols);
		int max = Math.max(rows, cols);

		initPolynomials(min, max);
	}
	
	public PolynomialQuiver(QuiverMatrix matrix){
		mMatrix = matrix;
		int rows = mMatrix.getNumRows();
		int cols = mMatrix.getNumCols();
		
		int unFrozenVars = Math.min(rows, cols);
		int totalVars = Math.max(rows, cols);
		
		initPolynomials(unFrozenVars, totalVars);
	}

	private void initPolynomials(int unFrozenVars, int totalVars) {
		String[] variables = new String[totalVars];
		for (int i = 0; i < unFrozenVars; i++) {
			variables[i] = "x" + i;
		}
		for (int i = unFrozenVars; i < totalVars; i++) {
			variables[i] = "y" + i;
		}
		mRing = new QuotientField(new PolynomialRing(Ring.Q, variables));

		mPolynomials = new RingElt[totalVars];
		for (int i = 0; i < unFrozenVars; i++) {
			mPolynomials[i] = mRing.map("x" + i);
		}
		for (int i = unFrozenVars; i < totalVars; i++) {
			mPolynomials[i] = mRing.map("y" + i);
		}
	}

	public PolynomialQuiver(QuiverMatrix matrix, RingElt[] polynomials, Ring ring) {
		int max = Math.max(matrix.getNumRows(), matrix.getNumCols());
		if (max != polynomials.length) {
			throw new IllegalArgumentException();
		}
		this.mMatrix = matrix;
		this.mPolynomials = polynomials;
		this.mRing = ring;
	}

	/**
	 * Provides ring for testing, so that the expected polynomials can be created.
	 */
	Ring getRing() {
		return mRing;
	}

	/**
	 * Get the polynomial at the specified vertex.
	 * @param k The vertex at which the required polynomial is
	 * @return The polynomial at vertex k
	 */
	public RingElt getPolynomial(int k) {
		return mPolynomials[k];
	}

	/**
	 * Mutate the quiver at the specified vertex. Remember that the indices start at 0.
	 * 
	 * @return A new quiver which is the mutation of this one.
	 */
	@Override
	public Quiver mutate(int k) {
		QuiverMatrix newMatrix = mMatrix.mutate(k);
		RingElt[] newPolynomials = Arrays.copyOf(mPolynomials, mPolynomials.length);
		RingElt pos = mRing.one();
		RingElt neg = mRing.one();
		for (int i = 0; i < mPolynomials.length; i++) {
			if (mMatrix.get(k, i) > 0) {
				RingElt mul = mRing.pow(mPolynomials[i], 
						(int) (mMatrix.get(k,i)));
				pos = mRing.mult(pos,mul);
			} else if (mMatrix.get(k, i) < 0) {
				RingElt mul = mRing.pow(mPolynomials[i], 
						(int) -(1*mMatrix.get(k,i)));
				neg = mRing.mult(neg, mul);
			}
		}
		newPolynomials[k] = mRing.div(mRing.add(pos, neg), mPolynomials[k]);

		return new PolynomialQuiver(newMatrix, newPolynomials, mRing);
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
		PolynomialQuiver rhs = (PolynomialQuiver) obj;
		return new EqualsBuilder().append(mMatrix, rhs.mMatrix)
				.append(mPolynomials, rhs.mPolynomials).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(23, 43).append(mMatrix).append(mPolynomials).toHashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mMatrix);
		sb.append(System.lineSeparator());
		sb.append(" { ");
		for(int i = 0; i < mPolynomials.length; i++){
			sb.append(mPolynomials[i]).append(", ");
		}
		sb.deleteCharAt(sb.length()-2);
		sb.append("} ");
		return sb.toString();
	}
}
