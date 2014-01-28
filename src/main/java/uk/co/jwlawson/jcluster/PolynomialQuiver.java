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

import com.perisic.ring.PolynomialRing;
import com.perisic.ring.Ring;
import com.perisic.ring.RingElt;

/**
 * @author John Lawson
 * 
 */
public class PolynomialQuiver extends Quiver {

	private QuiverMatrix mMatrix;
	private PolynomialRing mRing;
	private RingElt[] mPolynomials;

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
		mRing = new PolynomialRing(Ring.Q, variables);

		mPolynomials = new RingElt[max];
		for (int i = 0; i < min; i++) {
			mPolynomials[i] = mRing.map("x" + i);
		}
		for (int i = min; i < max; i++) {
			mPolynomials[i] = mRing.map("y" + i);
		}
	}

	@Override
	public Quiver mutate(int k) {
		QuiverMatrix newMatrix = mMatrix.mutate(k);

		return null;
	}

}
