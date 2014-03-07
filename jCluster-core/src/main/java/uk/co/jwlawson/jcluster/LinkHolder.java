/**
 * 
 */
package uk.co.jwlawson.jcluster;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author John
 * 
 */
public class LinkHolder<T extends QuiverMatrix> {

	private final boolean[] mList;
	private T mMatrix;

	public LinkHolder(int size) {
		mList = new boolean[size];
	}

	public boolean[] getLinksList() {
		return mList;
	}

	public void setMatrix(T matrix) {
		mMatrix = matrix;
	}

	public T getQuiverMatrix() {
		return mMatrix;
	}

	public boolean hasLink(int index) {
		return mList[index] != false;
	}

	public boolean getLinkAt(int index) {
		return mList[index];
	}

	public void setLinkAt(int index) {
//		if (mList[index] != false) {
//			throw new RuntimeException("Link already set");
//		}
		mList[index] = true;
	}

	public boolean isComplete() {
		for (int i = 0; i < mList.length; i++) {
			if (mList[i] == false) {
				return false;
			}
		}
		return true;
	}

	public void clear() {
		for (int i = 0; i < mList.length; i++) {
			mList[i] = false;
		}
		mMatrix = null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		LinkHolder rhs = (LinkHolder) obj;
		return new EqualsBuilder().append(mMatrix, rhs.mMatrix)
				.append(mList, rhs.mList).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(43, 57).append(mMatrix).append(mList)
				.toHashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(mMatrix.toString());
		sb.append(System.lineSeparator()).append("With links ");
		for (int i = 0; i < mList.length; i++) {
			sb.append(i).append("->").append(mList[i] != false);
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
