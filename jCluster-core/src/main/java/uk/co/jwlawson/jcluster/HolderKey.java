package uk.co.jwlawson.jcluster;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Key object used to lookup {@link LinkHolder} pools in the cache
 * 
 * @author John Lawson
 * 
 * @param <V> Type of {@link QuiverMatrix} expected by the holders in the pool
 */
public class HolderKey<V> {
	private final int size;
	private final Class<V> clazz;

	public HolderKey(int size, Class<V> clazz) {
		this.size = size;
		this.clazz = clazz;
	}

	public int getSize() {
		return size;
	}

	public Class<V> getClassObject() {
		return clazz;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (obj.getClass() != getClass())
			return false;
		HolderKey<?> rhs = (HolderKey<?>) obj;
		return (size == rhs.size) && (clazz == rhs.clazz);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(57, 293).append(size).append(clazz).toHashCode();
	}
}
