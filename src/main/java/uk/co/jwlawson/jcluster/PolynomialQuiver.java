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
 * @author John Lawson
 * 
 */
public class PolynomialQuiver extends Quiver {

	private final QuiverMatrix mMatrix;
	private final Ring mRing;
	private final RingElt[] mPolynomials;

	public PolynomialQuiver(int rows, int cols, double... data) {
		mMatrix = new QuiverMatrix(rows, cols, data);

		int min = Math.min(rows, cols);
		int max = Math.max(rows, cols);

		String[] variables = new String[max];
		for (int i = 0; i < min; i++) {
			variables[i] = "x" + i;
		}
		for (int i = min; i < max; i++) {
			variables[i] = "y" + i;
		}
		mRing = new QuotientField(new PolynomialRing(Ring.Q, variables));

		mPolynomials = new RingElt[max];
		for (int i = 0; i < min; i++) {
			mPolynomials[i] = mRing.map("x" + i);
		}
		for (int i = min; i < max; i++) {
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
				pos = mRing.mult(pos, mPolynomials[i]);
			} else if (mMatrix.get(k, i) < 0) {
				neg = mRing.mult(neg, mPolynomials[i]);
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
}
