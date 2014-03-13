/**
 * Copyright 2014 John Lawson
 * 
 * EquivMutClassSizeTaskTest.java is part of JCluster. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
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

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author John Lawson
 * 
 */
public class EquivMutClassSizeTaskTest {

	@BeforeClass
	public static void setUp() {
		// Run so that the caches in EquivalenceChecker are created before any tests are run. This
		// means that the times given for each test better represent the time actually taken for
		// that test.
		EquivalenceChecker.getInstance(2);
	}

	@Test
	public void testA3() {
		QuiverMatrix matrix = DynkinDiagram.A3.getMatrix();
		EquivMutClassSizeTask task = new EquivMutClassSizeTask(matrix);
		int result;
		try {
			result = task.call();
			assertEquals(4, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testA4() {
		QuiverMatrix matrix = DynkinDiagram.A4.getMatrix();
		EquivMutClassSizeTask task = new EquivMutClassSizeTask(matrix);
		int result;
		try {
			result = task.call();
			assertEquals(6, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testE6() {
		QuiverMatrix matrix = DynkinDiagram.E6.getMatrix();
		EquivMutClassSizeTask task = new EquivMutClassSizeTask(matrix);
		int result;
		try {
			result = task.call();
			assertEquals(67, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Ignore("Takes over 10 seconds")
	@Test
	public void testD7() {
		QuiverMatrix matrix = DynkinDiagram.D7.getMatrix();
		EquivMutClassSizeTask task = new EquivMutClassSizeTask(matrix);
		int result;
		try {
			result = task.call();
			assertEquals(246, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testD5() {
		QuiverMatrix matrix = DynkinDiagram.D5.getMatrix();
		EquivMutClassSizeTask task = new EquivMutClassSizeTask(matrix);
		int result;
		try {
			result = task.call();
			assertEquals(26, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Ignore("Takes over 12 seconds")
	@Test
	public void testA7() {
		QuiverMatrix matrix = DynkinDiagram.A7.getMatrix();
		EquivMutClassSizeTask task = new EquivMutClassSizeTask(matrix);
		int result;
		try {
			result = task.call();
			assertEquals(150, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Ignore("Takes over 30 seconds")
	@Test
	public void testE7() {
		QuiverMatrix matrix = DynkinDiagram.E7.getMatrix();
		EquivMutClassSizeTask task = new EquivMutClassSizeTask(matrix);
		int result;
		try {
			result = task.call();
			assertEquals(416, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
