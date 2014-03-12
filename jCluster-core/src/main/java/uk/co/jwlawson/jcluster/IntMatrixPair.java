/**
 * Copyright 2014 John Lawson
 * 
 * IntMatrixPair.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
 * Class to store a pair of {@link IntMatrixPair} objects. The order of the matrices is important in
 * the equals and hashcode methods.
 * 
 * @author John Lawson
 * 
 */
public class IntMatrixPair {
	IntMatrix a;
	IntMatrix b;

	public IntMatrixPair() {}

	/**
	 * Clear the contents of the pair.
	 */
	public void reset() {
		a = null;
		b = null;
	}

	/**
	 * Set the matrices stored in the pair.
	 * 
	 * @param a First matrix
	 * @param b Second matrix
	 */
	public void set(IntMatrix a, IntMatrix b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * The order of the matrices in the pair is considered when checking equals. A pair
	 * {@code (a,b)} is not equal to the pair {@code (b,a)}.
	 * 
	 * @param obj Object to check equality with
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IntMatrixPair rhs = (IntMatrixPair) obj;
		boolean result = IntMatrix.areEqual(a, rhs.a) && IntMatrix.areEqual(b, rhs.b);
		return result;
	}

	@Override
	public int hashCode() {
		return IntMatrix.hashCode(a) + 137 * IntMatrix.hashCode(b);
	}

}
