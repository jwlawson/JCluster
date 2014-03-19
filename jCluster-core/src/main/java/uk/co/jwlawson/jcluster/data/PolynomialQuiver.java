/**
 * Copyright 2014 John Lawson
 * 
 * PolynomialQuiver.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
package uk.co.jwlawson.jcluster.data;

import java.util.Arrays;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.perisic.ring.PolynomialRing;
import com.perisic.ring.QuotientField;
import com.perisic.ring.Ring;
import com.perisic.ring.RingElt;

/**
 * A quiver which has polynomials at each vertex.
 * 
 * @author John Lawson
 * 
 */
public class PolynomialQuiver extends Quiver {

	/** Constant to use as base for the hashcode. */
	private static final int HASH_BASE = 23;
	/** Constant to use as the hash multiplier. */
	private static final int HASH_MULT = 43;

	/** Matrix containing the quiver information. */
	private final QuiverMatrix mMatrix;

	/** Ring that the polynomials are from. */
	private Ring mRing;

	/** Array of polynomials which are at the vertices of the quiver. */
	private RingElt[] mPolynomials;

	/**
	 * Create a new quiver with default polynomials from the quiver provided in the array.
	 * 
	 * @param rows Number of rows in matrix
	 * @param cols Number of columns in matrix
	 * @param data Values in the matrix
	 */
	public PolynomialQuiver(final int rows, final int cols, final int... data) {
		this(new QuiverMatrix(rows, cols, data));
	}

	/**
	 * Create a new quiver with default polynomials on each vertex.
	 * 
	 * <p>
	 * The default vertices are {@code x1 ... xk} where k is the number of unfrozen vertices and then
	 * {@code y1 ... yn } for the unfrozen vertices.
	 * 
	 * @param matrix Matrix containing the quiver
	 */
	public PolynomialQuiver(final QuiverMatrix matrix) {
		mMatrix = matrix;
		int rows = mMatrix.getNumRows();
		int cols = mMatrix.getNumCols();

		int unFrozenVars = Math.min(rows, cols);
		int totalVars = Math.max(rows, cols);

		initPolynomials(unFrozenVars, totalVars);
	}

	/**
	 * Initialise the polynomials at the vertices. Unfrozen variables are called xi for an integer i,
	 * whereas the frozen ones are yi.
	 * 
	 * @param unFrozenVars Number of unfrozen vertices
	 * @param totalVars Total number of vertices
	 */
	private void initPolynomials(final int unFrozenVars, final int totalVars) {
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

	/**
	 * Create a new quiver with the supplied polynomials with coefficients fromt he supplied ring.
	 * 
	 * @param matrix Matrix to base the quiver on
	 * @param polynomials Polynomials at the vertices
	 * @param ring Ring the polynomials are from
	 */
	public PolynomialQuiver(final QuiverMatrix matrix, final RingElt[] polynomials, final Ring ring) {
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
	 * 
	 * @return The ring which the polynomials are from
	 */
	final Ring getRing() {
		return mRing;
	}

	/**
	 * Get the polynomial at the specified vertex.
	 * 
	 * @param k The vertex at which the required polynomial is
	 * @return The polynomial at vertex k
	 */
	public final RingElt getPolynomial(final int k) {
		return mPolynomials[k];
	}

	/**
	 * Mutate the quiver at the specified vertex. Remember that the indices start at 0.
	 * 
	 * @param k The vertex to mutate at
	 * @return A new quiver which is the mutation of this one.
	 */
	@Override
	public final Quiver mutate(final int k) {
		QuiverMatrix newMatrix = mMatrix.mutate(k);
		RingElt[] newPolynomials = Arrays.copyOf(mPolynomials, mPolynomials.length);
		RingElt pos = mRing.one();
		RingElt neg = mRing.one();
		for (int i = 0; i < mPolynomials.length; i++) {
			if (mMatrix.get(k, i) > 0) {
				RingElt mul = mRing.pow(mPolynomials[i], (mMatrix.get(k, i)));
				pos = mRing.mult(pos, mul);
			} else if (mMatrix.get(k, i) < 0) {
				RingElt mul = mRing.pow(mPolynomials[i], -(1 * mMatrix.get(k, i)));
				neg = mRing.mult(neg, mul);
			}
		}
		newPolynomials[k] = mRing.div(mRing.add(pos, neg), mPolynomials[k]);

		return new PolynomialQuiver(newMatrix, newPolynomials, mRing);
	}

	@Override
	public final boolean equals(final Object obj) {
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
		return new EqualsBuilder().append(mMatrix, rhs.mMatrix).append(mPolynomials, rhs.mPolynomials)
				.isEquals();
	}

	@Override
	public final int hashCode() {
		return new HashCodeBuilder(HASH_BASE, HASH_MULT).append(mMatrix).append(mPolynomials)
				.toHashCode();
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mMatrix);
		sb.append(System.lineSeparator());
		sb.append(" { ");
		for (int i = 0; i < mPolynomials.length; i++) {
			sb.append(mPolynomials[i]).append(", ");
		}
		sb.deleteCharAt(sb.length() - 2);
		sb.append("} ");
		return sb.toString();
	}
}
