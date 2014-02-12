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
public class LinkableQuiverMatrixHolder {
	
	
	private final QuiverMatrix[] mList;
	
	public LinkableQuiverMatrixHolder(int size) {
		mList = new QuiverMatrix[size];
	}
	
	public QuiverMatrix[] getLinksList() {
		return mList;
	}
	
	public boolean hasLink(int index){
		return mList[index] != null;
	}
	
	public QuiverMatrix getLinkAt(int index){
		return mList[index];
	}
	
	public void setLinkAt(int index, QuiverMatrix link){
		if(mList[index]!= null){
			throw new RuntimeException("Link already set");
		}
		mList[index]= link;
	}
	
	public boolean isComplete() {
		for(int i = 0; i < mList.length; i++){
			if(mList[i] == null){
				return false;
			}
		}
		return true;
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
		LinkableQuiverMatrixHolder rhs = (LinkableQuiverMatrixHolder) obj;
		return new EqualsBuilder().append(mList, rhs.mList).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(43, 57).append(mList).toHashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.lineSeparator()).append("With links ");
		for(int i = 0; i < mList.length; i ++){
			sb.append(i).append("->").append(mList[i] != null);
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
