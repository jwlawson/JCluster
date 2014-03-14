/**
 * Copyright 2014 John Lawson
 * 
 * NumericQuiverTest.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import org.junit.Before;
import org.junit.Test;

public class NumericQuiverTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void test4x4() {
		NumericQuiver quiv =
				new NumericQuiver(4, 4, 0, -1, 0, 1, 1, 0, -1, 0, 0, 1, 0, -1, -1, 0, 1, 0);

		NumericQuiver mut =
				(NumericQuiver) quiv.mutate(2).mutate(1).mutate(0).mutate(1).mutate(3).mutate(0).mutate(2);
		assertEquals(1, mut.getValue(0), 10e-10);
		assertEquals(2, mut.getValue(1), 10e-10);
		assertEquals(3, mut.getValue(2), 10e-10);
		assertEquals(4, mut.getValue(3), 10e-10);
	}

}
