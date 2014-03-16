package uk.co.jwlawson.jcluster;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Key object used to lookup {@link LinkHolder} pools in the cache.
 * 
 * @author John Lawson
 * 
 * @param <V> Type of {@link QuiverMatrix} expected by the holders in the pool
 */
public class HolderKey<V> {
	private final int mSize;
	private final Class<V> mClazz;

	/**
	 * Create a new HolderKey.
	 * 
	 * @param size Number of links in the holder
	 * @param clazz Type of matrix expected
	 */
	public HolderKey(final int size, final Class<V> clazz) {
		this.mSize = size;
		this.mClazz = clazz;
	}

	/**
	 * Get the mSize.
	 * 
	 * @return Size
	 */
	public int getSize() {
		return mSize;
	}

	/**
	 * Get the class object.
	 * 
	 * @return class object
	 */
	public Class<V> getClassObject() {
		return mClazz;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		HolderKey<?> rhs = (HolderKey<?>) obj;
		return (mSize == rhs.mSize) && (mClazz == rhs.mClazz);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(57, 293).append(mSize).append(mClazz).toHashCode();
	}
}
