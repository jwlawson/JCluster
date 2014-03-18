/**
 * Copyright 2014 John Lawson
 * 
 * IntMatrixTest.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author John Lawson
 * 
 */
public class IntMatrixTest {

	@Test
	public void testEquals() {
		IntMatrix m1 = new IntMatrix(4, 4, 0, 0, 3, 0, 1, 0, 0, 0, 0, 0, 0, 4, 0, 2, 0, 0);
		IntMatrix m2 = new IntMatrix(4, 4, 0, 0, 3, 0, 1, 0, 0, 0, 0, 0, 0, 4, 0, 2, 0, 0);

		assertEquals(m1, m1);
		assertEquals(m1, m2);
	}

	@Test
	public void testMultEquals() {
		IntMatrix p = new IntMatrix(4, 4, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0);
		IntMatrix a = new IntMatrix(4, 4, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 4);
		IntMatrix b = new IntMatrix(4, 4, 3, 0, 0, 0, 0, 1, 0, 0, 0, 0, 4, 0, 0, 0, 0, 2);

		IntMatrix pa = new IntMatrix(4, 4);
		IntMatrix bp = new IntMatrix(4, 4);

		a.multLeft(p, pa);
		b.multRight(p, bp);

		assertEquals(pa, bp);
		assertTrue(pa.equals(bp));
		assertTrue(bp.equals(pa));
	}

	@Test
	public void test3Equals() {
		IntMatrix p = new IntMatrix(3, 3, 0, 1, 0, 0, 0, 1, 1, 0, 0);
		IntMatrix m1 = new IntMatrix(3, 3, 0, 1, 0, -1, 0, 1, 0, -1, 0);
		IntMatrix m2 = new IntMatrix(3, 3, 0, 1, -1, -1, 0, 0, 1, 0, 0);

		IntMatrix pa = p.multRight(m1);
		IntMatrix bp = p.multLeft(m2);

		assertTrue(pa.equals(bp));
		assertTrue(bp.equals(pa));
	}

	@Test
	public void testMultiplication() {
		IntMatrix a = new IntMatrix(2, 2, 1, 2, 3, 4);
		IntMatrix b = new IntMatrix(2, 2, 5, 6, 7, 8);
		IntMatrix exp = new IntMatrix(2, 2, 19, 22, 43, 50);

		assertEquals(exp, a.multRight(b));
	}

	@Test
	public void testMultiplication3x3() {
		IntMatrix a = new IntMatrix(3, 3, 1, 2, 3, 1, 3, 2, 5, 4, 3);
		IntMatrix b = new IntMatrix(3, 3, 5, 5, 5, 2, 3, 4, 7, 6, 4);
		IntMatrix exp = new IntMatrix(3, 3, 30, 29, 25, 25, 26, 25, 54, 55, 53);

		assertEquals(exp, a.multRight(b));
	}

	@Test
	public void testIdentity() {
		IntMatrix a = new IntMatrix(3, 3, 0, 0, -1, 0, 0, 1, 1, -1, 0);
		IntMatrix id = new IntMatrix(3, 3, 1, 0, 0, 0, 1, 0, 0, 0, 1);

		assertEquals(a, a.multLeft(id));
		assertEquals(a, id.multLeft(a));
	}

	@Test
	public void testSubmatrix3x3a() {
		IntMatrix m = new IntMatrix(3, 3, 0, 1, 2, 3, 4, 5, 6, 7, 8);
		IntMatrix exp = new IntMatrix(2, 2, 4, 5, 7, 8);

		assertEquals(exp, m.submatrix(0, 0));
	}

	@Test
	public void testSubmatrix3x3b() {
		IntMatrix m = new IntMatrix(3, 3, 0, 1, 2, 3, 4, 5, 6, 7, 8);
		IntMatrix exp = new IntMatrix(2, 2, 1, 2, 4, 5);

		assertEquals(exp, m.submatrix(2, 0));
	}

	@Test
	public void testSubmatrix3x3c() {
		IntMatrix m = new IntMatrix(3, 3, 0, 1, 2, 3, 4, 5, 6, 7, 8);
		IntMatrix exp = new IntMatrix(2, 2, 0, 1, 6, 7);

		assertEquals(exp, m.submatrix(1, 2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSubmatrixRows() {
		IntMatrix m = new IntMatrix(3, 3, 0, 1, 2, 3, 4, 5, 6, 7, 8);
		m.submatrix(3, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSubmatrixCols() {
		IntMatrix m = new IntMatrix(3, 3, 0, 1, 2, 3, 4, 5, 6, 7, 8);
		m.submatrix(0, 3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSubmatrixResultRows() {
		IntMatrix m = new IntMatrix(3, 3, 0, 1, 2, 3, 4, 5, 6, 7, 8);
		IntMatrix m1 = new IntMatrix(3, 2);
		m.submatrix(0, 1, m1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSubmatrixResultCols() {
		IntMatrix m = new IntMatrix(3, 3, 0, 1, 2, 3, 4, 5, 6, 7, 8);
		IntMatrix m1 = new IntMatrix(2, 3);
		m.submatrix(1, 1, m1);
	}

	@Test
	public void testZeroRow() {
		IntMatrix m = new IntMatrix(3, 3, 1, 2, 3, 0, 0, 0, 1, 2, 3);
		assertEquals(1, m.getZeroRow());
	}

	@Test
	public void testFirstZeroRow() {
		IntMatrix m = new IntMatrix(4, 4, 0, 0, 0, 0, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4);
		assertEquals(0, m.getZeroRow());
	}

	@Test
	public void testLastZeroRow() {
		IntMatrix m = new IntMatrix(4, 4, 0, 1, 1, 0, -1, 0, 1, 0, -1, -1, 0, 0, 0, 0, 0, 0);
		assertEquals(3, m.getZeroRow());
	}

	@Test
	public void testNonZeroRow() {
		IntMatrix m = new IntMatrix(4, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4);
		assertEquals(-1, m.getZeroRow());
	}

	@Test
	public void testEnlarge3To6() {
		IntMatrix m = new IntMatrix(3, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		IntMatrix exp =
				new IntMatrix(6, 6, 1, 2, 3, 0, 0, 0, 4, 5, 6, 0, 0, 0, 7, 8, 9, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

		assertEquals(exp, m.enlargeMatrix(3, 3, new IntMatrix(6, 6)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEnlargeInvalidRows() {
		IntMatrix m = new IntMatrix(2, 2);
		m.enlargeMatrix(-1, 1, new IntMatrix(1, 3));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEnlargeInvalidCols() {
		IntMatrix m = new IntMatrix(3, 4);
		m.enlargeMatrix(2, -2, new IntMatrix(5, 2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEnlargeInvalidContainerRows() {
		IntMatrix m = new IntMatrix(2, 2);
		m.enlargeMatrix(1, 1, new IntMatrix(2, 3));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEnlargeInvalidContainerCols() {
		IntMatrix m = new IntMatrix(2, 2);
		m.enlargeMatrix(1, 3, new IntMatrix(3, 3));
	}
}
