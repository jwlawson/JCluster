/**
 * Copyright 2014 John Lawson
 * 
 * MatrixAdaptor.java is part of JCluster.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.jwlawson.jcluster;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.ejml.data.DenseMatrix64F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptor class for DenseMatrix64F which provides equals and hashcode methods.
 * @author John Lawson
 * 
 */
public class MatrixAdaptor extends DenseMatrix64F {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private int hashcode = Integer.MAX_VALUE;

	public MatrixAdaptor(int numRows, int numCols, boolean rowMajor, double... data) {
		super(numRows, numCols, rowMajor, data);
		removeNegZero();
	}

	public MatrixAdaptor(int numRows, int numCols) {
		super(numRows, numCols);
	}

	private MatrixAdaptor(DenseMatrix64F m) {
		super(m);
		removeNegZero();
	}

	// This is a bit of a hack.
	// TODO Do MatrixAdaptor copy better.
	public MatrixAdaptor copyMatrix() {
		return new MatrixAdaptor(((DenseMatrix64F) this).copy());
	}
	
	@Override
	public void set(int numRows, int numCols, boolean rowMajor, double... data) {
		super.set(numRows, numCols, rowMajor, data);
		removeNegZero();
		hashcode = Integer.MAX_VALUE;
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
		MatrixAdaptor rhs = (MatrixAdaptor) obj;
		if(data.length != rhs.data.length){
			return false;
		}
		if(hashCode() != rhs.hashCode()){
			return false;
		}
		for(int i = 0; i < data.length; i++){
			if(Double.doubleToLongBits(data[i]) != Double.doubleToLongBits(rhs.data[i])){
				return false;
			}
		}
		return true;
	}

	public void removeNegZero() {
		for (int i = 0; i < data.length; i++) {
			if (-0d == data[i]) {
				data[i] = 0d;
			}
		}
	}
	
	public void reset() {
		hashcode = Integer.MAX_VALUE;
	}

	@Override
	public int hashCode() {
		if(hashcode == Integer.MAX_VALUE){
			hashcode =  new HashCodeBuilder(17, 37).append(this.data).toHashCode();
		}
		return hashcode;
	}
}
