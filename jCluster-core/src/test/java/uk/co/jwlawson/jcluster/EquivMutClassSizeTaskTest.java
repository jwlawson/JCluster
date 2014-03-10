/**
 * Copyright 2014 John Lawson
 * 
 * EquivMutClassSizeTaskTest.java is part of JCluster.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author John Lawson
 * 
 */
public class EquivMutClassSizeTaskTest {

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
}
