package uk.co.jwlawson.jcluster;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class NumericQuiverTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test4x4() {
		NumericQuiver quiv = new NumericQuiver(4, 4, 0, -1, 0, 1, 1, 0, -1, 0, 0, 1, 0, -1, -1, 0,
				1, 0);

		NumericQuiver mut = (NumericQuiver) quiv.mutate(2).mutate(1).mutate(0).mutate(1).mutate(3)
				.mutate(0).mutate(2);
		assertEquals(1, mut.getValue(0), 10e-10);
		assertEquals(2, mut.getValue(1), 10e-10);
		assertEquals(3, mut.getValue(2), 10e-10);
		assertEquals(4, mut.getValue(3), 10e-10);
	}

}
