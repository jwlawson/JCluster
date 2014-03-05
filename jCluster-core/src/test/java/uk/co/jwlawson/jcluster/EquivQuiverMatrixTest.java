/**
 * Copyright 2014 John Lawson
 * 
 * EquivQuiverMatrixTest.java is part of JCluster.
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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author John Lawson
 * 
 */
public class EquivQuiverMatrixTest {

	@Test
	public void testSimpleA3() {
		EquivQuiverMatrix m1 = new EquivQuiverMatrix(3, 0, 1, 0, -1, 0, 1, 0,
				-1, 0);
		EquivQuiverMatrix m2 = new EquivQuiverMatrix(3, 0, 1, -1, -1, 0, 0, 1,
				0, 0);

		assertTrue(m1.equals(m2));
		assertTrue(m2.equals(m1));
	}

	@Test
	public void testSameAreEqual() {
		EquivQuiverMatrix m1 = new EquivQuiverMatrix(3, 1, 0, 0, 0, 1, 0, 0, 0,
				1);

		assertTrue("Same object equal", m1.equals(m1));

		EquivQuiverMatrix m2 = new EquivQuiverMatrix(3, 1, 0, 0, 0, 1, 0, 0, 0,
				1);

		assertTrue("Same matrix equal", m1.equals(m2));
	}

	@Test
	public void test4x4() {
		EquivQuiverMatrix m1 = new EquivQuiverMatrix(4, 1, 0, 0, 0, 0, 1, 0, 0,
				0, 0, 1, 0, 0, 0, 0, 1);
		EquivQuiverMatrix m2 = new EquivQuiverMatrix(4, 1, 0, 0, 0, 0, 1, 0, 0,
				0, 0, 1, 0, 0, 0, 0, 1);

		assertTrue("Simple 4x4 example", m1.equals(m2));
	}

	@Test
	public void test4() {
		EquivQuiverMatrix m1 = new EquivQuiverMatrix(4, 0, 1, 0, 1, 1, 0, 0, 0,
				0, 1, 1, 0, 0, 0, 0, 1);
		EquivQuiverMatrix m2 = new EquivQuiverMatrix(4, 0, 0, 1, 0, 0, 1, 0, 0,
				1, 1, 0, 0, 1, 0, 0, 1);

		assertEquals("Permuted 4x4 matrices", m1, m2);
	}

	@Test
	public void testNotEquivalent() {
		EquivQuiverMatrix m1 = new EquivQuiverMatrix(4, 1, 0, 0, 0, 0, 2, 0, 0,
				0, 0, 3, 0, 0, 0, 0, 4);
		EquivQuiverMatrix m2 = new EquivQuiverMatrix(4, 5, 0, 0, 1, 0, 2, 0, 0,
				0, 0, 3, 0, 0, 0, 0, 4);

		assertFalse("Matrices not equivalent", m1.equals(m2));
	}

}
