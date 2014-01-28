package uk.co.jwlawson.jcluster;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class QuiverMatrixTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConstruct() {
		QuiverMatrix mat = new QuiverMatrix(3, 3, 0, 1, 0, -1, 0, 1, 0, -1, 0);
		System.out.println(mat);
	}

	@Test
	public void testMutate() {
		QuiverMatrix mat = new QuiverMatrix(3, 3, 0, 1, 0, -1, 0, 1, 0, -1, 0);
		QuiverMatrix exp1 = new QuiverMatrix(3, 3, 0, -1, 0, 1, 0, 1, 0, -1, 0);

		assertEquals("Mutation at 1 in A2", exp1, mat.mutate(1));
	}

}
