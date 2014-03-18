/**
 * Copyright 2014 John Lawson
 * 
 * AllSubFiniteCheckTest.java is part of JCluster. Licensed under the Apache License, Version 2.0
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
package uk.co.jwlawson.jcluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author John Lawson
 * 
 */
public class AllSubFiniteCheckTest {

	@Test
	public void testA4() {
		QuiverMatrix mat = DynkinDiagram.A4.getMatrix();
		AllSubFiniteCheck<QuiverMatrix> task = new AllSubFiniteCheck<QuiverMatrix>(mat);

		try {
			MatrixInfo result = task.call();
			assertTrue(result.getAllSubmatricesFinite());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testD5() {
		QuiverMatrix mat = DynkinDiagram.D5.getMatrix();
		AllSubFiniteCheck<QuiverMatrix> task = new AllSubFiniteCheck<QuiverMatrix>(mat);

		try {
			MatrixInfo result = task.call();
			assertTrue(result.getAllSubmatricesFinite());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testInf() {
		QuiverMatrix mat =
				new QuiverMatrix(5, 5, 0, 1, 0, 0, 1, -1, 0, 1, 1, 0, 0, -1, 0, 1, 0, 0, -1, -1, 0,
						0, -1, 0, 0, 0, 0);
		AllSubFiniteCheck<QuiverMatrix> task = new AllSubFiniteCheck<QuiverMatrix>(mat);

		try {
			MatrixInfo result = task.call();
			assertFalse(result.getAllSubmatricesFinite());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
