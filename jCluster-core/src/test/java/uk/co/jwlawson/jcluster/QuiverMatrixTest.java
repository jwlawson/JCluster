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

		assertEquals("Get 0,0", 0, mat.get(0, 0), 10e-10);
		assertEquals("Get 0,1", 1, mat.get(0, 1), 10e-10);
	}

	@Test
	public void testMutate() {
		QuiverMatrix mat = new QuiverMatrix(3, 3, 0, 1, 0, -1, 0, 1, 0, -1, 0);
		QuiverMatrix exp1 = new QuiverMatrix(3, 3, 0, -1, 0, 1, 0, 1, 0, -1, 0);

		assertEquals("Mutation at 0 in A2", exp1, mat.mutate(0));
	}

	@Test
	public void testMutate2() {
		QuiverMatrix mat = new QuiverMatrix(3, 3, 0, 1, 0, -1, 0, 1, 0, -1, 0);
		QuiverMatrix exp1 = new QuiverMatrix(3, 3, 0, -1, 1, 1, 0, -1, -1, 1, 0);

		assertEquals("Mutation at 1 in A2", exp1, mat.mutate(1));
	}

	@Test
	public void testMarkov() {
		QuiverMatrix mat = new QuiverMatrix(3, 3, 0, 2, -2, -2, 0, 2, 2, -2, 0);
		QuiverMatrix exp = new QuiverMatrix(3, 3, 0, -2, 2, 2, 0, -2, -2, 2, 0);

		assertEquals("Mutation in Markov quiver", exp, mat.mutate(0));
		assertEquals("Mutation in Markov quiver", exp, mat.mutate(1));
		assertEquals("Mutation in Markov quiver", exp, mat.mutate(2));
	}

	@Test
	public void testEnlarge() {
		QuiverMatrix m = new QuiverMatrix(3, 3, 0, 2, -2, -2, 0, 2, 2, -2, 0);
		QuiverMatrix exp = new QuiverMatrix(4, 4, 0, 2, -2, 0, -2, 0, 2, 0, 2, -2, 0, 0, 0, 0, 0, 0);

		assertEquals("Enlargin 3x3 to 4x4", exp, m.enlargeMatrix(1, 1));
	}

	@Test
	public void testEnlarge2() {
		QuiverMatrix m = new QuiverMatrix(3, 3, 0, 2, -2, -2, 0, 2, 2, -2, 0);
		QuiverMatrix exp = new QuiverMatrix(5, 5, 0, 2, -2, 0, 0, -2, 0, 2, 0, 0, 2, -2, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

		assertEquals("Enlargin 3x3 to 4x4", exp, m.enlargeMatrix(2, 2));
	}
}
