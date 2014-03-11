package uk.co.jwlawson.jcluster;

public class IntMatrixPair {
	IntMatrix a;
	IntMatrix b;

	public IntMatrixPair() {}

	public void reset() {
		a = null;
		b = null;
	}

	public void set(IntMatrix a, IntMatrix b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IntMatrixPair rhs = (IntMatrixPair) obj;
		boolean result = IntMatrix.areEqual(a, rhs.a) && IntMatrix.areEqual(b, rhs.b);
		return result;
	}

	@Override
	public int hashCode() {
		return IntMatrix.hashCode(a) + 137 * IntMatrix.hashCode(b);
	}

}
