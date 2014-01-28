package uk.co.jwlawson.jcluster;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.perisic.ring.Ring;
import com.perisic.ring.RingElt;

public class PolynomialQuiverTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConstructor() {
		PolynomialQuiver quiv = new PolynomialQuiver(3, 3, 0, 1, 0, -1, 0, 1, 0, -1, 0);

		Ring ring = quiv.getRing();
		RingElt x0 = ring.map("x0");
		RingElt x1 = ring.map("x1");
		RingElt x2 = ring.map("x2");

		assertEquals(x0, quiv.getPolynomial(0));
		assertEquals(x1, quiv.getPolynomial(1));
		assertEquals(x2, quiv.getPolynomial(2));
	}

	@Test
	public void testMutate() {
		PolynomialQuiver quiv = new PolynomialQuiver(3, 3, 0, 1, 0, -1, 0, 1, 0, -1, 0);

		Ring ring = quiv.getRing();
		RingElt x0 = ring.map("(1+x1)/x0");
		RingElt x1 = ring.map("x1");
		RingElt x2 = ring.map("x2");

		PolynomialQuiver mut = (PolynomialQuiver) quiv.mutate(0);

		assertEquals(x0, mut.getPolynomial(0));
		assertEquals(x1, mut.getPolynomial(1));
		assertEquals(x2, mut.getPolynomial(2));
	}
}
