/**
 * Copyright 2014 John Lawson
 * 
 * LinkHolder.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
package uk.co.jwlawson.jcluster.data;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *uk.co.jwlawson.jcluster.data.QuiverMatrixons of a {@link QuiverMatrix} have been considered.
 * 
 * @author John Lawson
 * 
 * @param <T> Type of matrix expected in the holder
 * 
 */
public class LinkHolder<T extends QuiverMatrix> {

	private final boolean[] mList;
	private T mMatrix;

	/**
	 * Create a new {@link LinkHolder} with {@code size} number of links.
	 * 
	 * @param size Number of links
	 */
	public LinkHolder(final int size) {
		mList = new boolean[size];
	}

	/**
	 * Set the {@link QuiverMatrix} which the {@link LinkHolder} is tracking.
	 * 
	 * @param matrix Matrix to set
	 */
	public void setMatrix(final T matrix) {
		mMatrix = matrix;
	}

	/**
	 * Get the matrix.
	 * 
	 * @return Matrix
	 */
	public T getQuiverMatrix() {
		return mMatrix;
	}

	/**
	 * Check if the link at {@code index} has been considered.
	 * 
	 * @param index Index to check
	 * @return true if the link has been set
	 */
	public boolean hasLink(final int index) {
		return mList[index];
	}

	/**
	 * Set the link at {@code index} to be true.
	 * 
	 * @param index Index to set
	 * @throws IllegalArgumentException if the link has already has been set. This allows the program
	 *         to fail quickly as the algorithms used should not be setting the same link twice.
	 */
	public void setLinkAt(final int index) {
		if (mList[index]) {
			throw new RuntimeException("Link already set in " + this);
		}
		mList[index] = true;
	}

	/**
	 * Check whether all links have been considered.
	 * 
	 * @return true if all links are set
	 */
	public boolean isComplete() {
		for (int i = 0; i < mList.length; i++) {
			if (!mList[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Reset the holder so that no links are set and no matrix is stored.
	 */
	public void clear() {
		for (int i = 0; i < mList.length; i++) {
			mList[i] = false;
		}
		mMatrix = null;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		LinkHolder<?> rhs = (LinkHolder<?>) obj;
		return new EqualsBuilder().append(mMatrix, rhs.mMatrix).append(mList, rhs.mList).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(43, 57).append(mMatrix).append(mList).toHashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(mMatrix.toString());
		sb.append(System.lineSeparator()).append("With links ");
		for (int i = 0; i < mList.length; i++) {
			sb.append(i).append("->").append(mList[i]);
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
