/**
 * 
 */
package uk.co.jwlawson.jcluster;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John
 *
 */
public class LinkHolder {
	
	private static final ObjectPool<EqualsBuilder> sBuilderPool = Pools.getEqualsBuilerPool();
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final boolean[] mList;
	private QuiverMatrix mMatrix;
	
	public LinkHolder(int size) {
		mList = new boolean[size];
	}
	
	public boolean[] getLinksList() {
		return mList;
	}
	
	public void setMatrix(QuiverMatrix matrix){
		mMatrix = matrix;
	}
	
	public QuiverMatrix getQuiverMatrix(){
		return mMatrix;
	}
	
	public boolean hasLink(int index){
		return mList[index] != false;
	}
	
	public boolean getLinkAt(int index){
		return mList[index];
	}
	
	public void setLinkAt(int index){
		if(mList[index]!= false){
			throw new RuntimeException("Link already set");
		}
		mList[index]= true;
	}
	
	public boolean isComplete() {
		for(int i = 0; i < mList.length; i++){
			if(mList[i] == false){
				return false;
			}
		}
		return true;
	}
	
	public void clear() {
		for(int i = 0; i < mList.length; i++){
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
		EqualsBuilder builder = null;
		try{
			builder = sBuilderPool.getObj();
			return builder.append(mMatrix, rhs.mMatrix).append(mList, rhs.mList).isEquals();
		} catch (PoolException e) {
			log.error("Error getting equals builder from pool" + e.getMessage(), e);
			return new EqualsBuilder().append(mMatrix, rhs.mMatrix).append(mList, rhs.mList).isEquals();
		} finally {
			if(builder != null){
				sBuilderPool.returnObj(builder);
			}
		}
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(43, 57).append(mMatrix).append(mList).toHashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.lineSeparator()).append("With links ");
		for(int i = 0; i < mList.length; i ++){
			sb.append(i).append("->").append(mList[i] != false);
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
