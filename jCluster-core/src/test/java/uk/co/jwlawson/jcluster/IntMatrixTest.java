/**
 * Copyright 2014 John Lawson
 * 
 * IntMatrixTest.java is part of JCluster.
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
public class IntMatrixTest {

	@Test
	public void testEquals() {
		IntMatrix m1 = new IntMatrix(4, 4, 0, 0, 3, 0, 1, 0, 0, 0, 0, 0, 0, 4,
				0, 2, 0, 0);
		IntMatrix m2 = new IntMatrix(4, 4, 0, 0, 3, 0, 1, 0, 0, 0, 0, 0, 0, 4,
				0, 2, 0, 0);

		assertEquals(m1, m1);
		assertEquals(m1, m2);
	}

	@Test
	public void testMultEquals() {
		IntMatrix p = new IntMatrix(4, 4, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1,
				0, 1, 0, 0);
		IntMatrix a = new IntMatrix(4, 4, 1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0,
				0, 0, 0, 4);
		IntMatrix b = new IntMatrix(4, 4, 3, 0, 0, 0, 0, 1, 0, 0, 0, 0, 4, 0,
				0, 0, 0, 2);

		IntMatrix pa = new IntMatrix(4, 4);
		IntMatrix bp = new IntMatrix(4, 4);

		a.multLeft(p, pa);
		b.multRight(p, bp);

		assertEquals(pa, bp);
		assertTrue(pa.equals(bp));
		assertTrue(bp.equals(pa));
	}
}
