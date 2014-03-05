/**
 * Copyright 2014 John Lawson
 * 
 * MatrixAdaptorTest.java is part of JCluster.
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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.junit.Test;

/**
 * @author John Lawson
 * 
 */
public class MatrixAdaptorTest {

	@Test
	public void testEquals() {
		Matrix m1 = new MatrixAdaptor(4, 4, true, 0, 0, 3, 0, 1, 0, 0,
				0, 0, 0, 0, 4, 0, 2, 0, 0);
		Matrix m2 = new MatrixAdaptor(4, 4, true, 0, 0, 3, 0, 1, 0, 0,
				0, 0, 0, 0, 4, 0, 2, 0, 0);

		assertEquals(m1, m1);
		assertEquals(m1, m2);
	}

	@Test
	public void testMultEquals() {
		DenseMatrix64F p = new DenseMatrix64F(4, 4, true, 0, 0, 1, 0, 1, 0, 0,
				0, 0, 0, 0, 1, 0, 1, 0, 0);
		DenseMatrix64F a = new DenseMatrix64F(4, 4, true, 1, 0, 0, 0, 0, 2, 0,
				0, 0, 0, 3, 0, 0, 0, 0, 4);
		DenseMatrix64F b = new DenseMatrix64F(4, 4, true, 3, 0, 0, 0, 0, 1, 0,
				0, 0, 0, 4, 0, 0, 0, 0, 2);

		MatrixAdaptor pa = new MatrixAdaptor(4, 4);
		MatrixAdaptor bp = new MatrixAdaptor(4, 4);

		CommonOps.mult(p, a, pa);
		CommonOps.mult(b, p, bp);

		assertEquals(pa, bp);
		assertTrue(pa.equals(bp));
		assertTrue(bp.equals(pa));
	}
}
