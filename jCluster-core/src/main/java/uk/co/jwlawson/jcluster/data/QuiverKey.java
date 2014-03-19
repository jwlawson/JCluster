package uk.co.jwlawson.jcluster.data;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *uk.co.jwlawson.jcluster.data.QuiverMatrix QuiverMatrix} pools in the cache.
 * 
 * @author John Lawson
 * 
 * @param <V> Type of QuiverMatrix provided by the pool
 */
public class QuiverKey<V> {
	private final int rows;
	private final int cols;
	private final Class<V> clazz;

	public QuiverKey(int rows, int cols, Class<V> clazz) {
		this.rows = rows;
		this.cols = cols;
		this.clazz = clazz;
	}

	public Class<V> getClassObject() {
		return clazz;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (obj.getClass() != getClass())
			return false;
		QuiverKey<?> rhs = (QuiverKey<?>) obj;
		return (rows == rhs.rows) && (cols == rhs.cols) && (clazz == rhs.clazz);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(57, 97).append(rows).append(cols).append(clazz).toHashCode();
	}
}
