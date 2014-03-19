/**
 * Copyright 2014 John Lawson
 * 
 * PolynomialQuiverTest.java is part of JCluster. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.co.jwlawson.jcluster.data;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.perisic.ring.Ring;
import com.perisic.ring.RingElt;

public class PolynomialQuiverTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testConstructor() {
		PolynomialQuiver quiv = new PolynomialQuiver(3, 3, 0, 1, 0, -1, 0, 1, 0, -1, 0);

		Ring ring = quiv.getRing();
		RingElt x0 = ring.map("x0");
		RingElt x1 = ring.map("x1");
		RingElt x2 = ring.map("x2");

		assertEquals(x0, quiv.getPolynomial(0));
		assertEquals(x1, quiv.getPolynomial(1));
		assertEquals(x2, quiv.getPolynomial(2));
	}

	@Test
	public void testMutate() {
		PolynomialQuiver quiv = new PolynomialQuiver(3, 3, 0, 1, 0, -1, 0, 1, 0, -1, 0);

		Ring ring = quiv.getRing();
		RingElt x0 = ring.map("(1+x1)/x0");
		RingElt x1 = ring.map("x1");
		RingElt x2 = ring.map("x2");

		PolynomialQuiver mut = (PolynomialQuiver) quiv.mutate(0);

		assertEquals(x0, mut.getPolynomial(0));
		assertEquals(x1, mut.getPolynomial(1));
		assertEquals(x2, mut.getPolynomial(2));
	}

	@Test
	public void test4x4() {
		PolynomialQuiver quiv =
				new PolynomialQuiver(4, 4, 0, -1, 0, 1, 1, 0, -1, 0, 0, 1, 0, -1, -1, 0, 1, 0);

		/* Expected values calculated using Kellers app */
		Ring ring = quiv.getRing();
		RingElt x0 = ring.map("(x0*x1 + x1*x2 + (x0 + x2)*x3)/(x0*x1*x2)");
		RingElt x1 = ring.map("(x0 + x2)/x1");
		RingElt x2 = ring.map("(x0*x1 + x1*x2 + (x0 + x2)*x3)/(x0*x1*x3)");
		RingElt x3 = ring.map("(x0*x1 + x1*x2 + (x0 + x2)*x3)/(x1*x2*x3)");

		PolynomialQuiver mut =
				(PolynomialQuiver) quiv.mutate(1).mutate(2).mutate(3).mutate(0).mutate(2);
		assertEquals(x0, mut.getPolynomial(0));
		assertEquals(x1, mut.getPolynomial(1));
		assertEquals(x2, mut.getPolynomial(2));
		assertEquals(x3, mut.getPolynomial(3));
	}

	@Test
	public void testG3() {
		PolynomialQuiver quiv = new PolynomialQuiver(2, 2, 0, 3, -1, 0);

		Ring ring = quiv.getRing();
		RingElt x0 = ring.map("(x1^3+1)/x0");
		RingElt x1 = ring.map("x1");

		PolynomialQuiver mut = (PolynomialQuiver) quiv.mutate(0);

		assertEquals(x0, mut.getPolynomial(0));
		assertEquals(x1, mut.getPolynomial(1));

		assertEquals("Mutate should be involution", quiv, mut.mutate(0));

	}
}
