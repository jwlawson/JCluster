/**
 * Copyright 2014 John Lawson
 * 
 * MatrixInfo.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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
package uk.co.jwlawson.jcluster;

import com.google.common.base.Optional;

/**
 * @author John Lawson
 * 
 */
public class MatrixInfo {

	private final QuiverMatrix matrix;
	private Optional<Boolean> finite = Optional.absent();
	private Optional<Integer> mutClassSize = Optional.absent();
	private Optional<Boolean> minMutInf = Optional.absent();
	private Optional<DynkinDiagram> diagram = Optional.absent();

	public MatrixInfo(QuiverMatrix matrix) {
		this.matrix = matrix;
	}

	/**
	 * Get the matrix whose information is stored here.
	 * 
	 * @return Matrix whose information is stored here
	 */
	public QuiverMatrix getMatrix() {
		return matrix;
	}


	/**
	 * Check whether it is known if the matrix is mutation finite or not.
	 * 
	 * @return true if the mutation class of the matrix is known
	 */
	public boolean hasFiniteSet() {
		return finite.isPresent();
	}

	/**
	 * Set whether the matrix is mutation finite.
	 * 
	 * @param finite true if the matrix is mutation finite
	 */
	public void setFinite(boolean finite) {
		if (finite) {
			noSideSetMinMutInf(false);
		}
		noSideSetFinite(finite);
	}

	private void noSideSetFinite(boolean finite) {
		this.finite = Optional.of(finite);
	}

	/**
	 * Get whether the matrix is mutation finite
	 * 
	 * @return true if mutation finite
	 * @throws IllegalStateException if this has not been set
	 */
	public boolean isFinite() {
		return finite.get();
	}

	/**
	 * Check whether the size of the mutation class has been set.
	 * 
	 * @return true if the size has been set
	 */
	public boolean hasMutationClassSize() {
		return mutClassSize.isPresent();
	}

	/**
	 * Set the value of the size of mutation class of this matrix.
	 * 
	 * @param mutClassSize Size of the mutation class
	 */
	public void setMutationClassSize(int mutClassSize) {
		if (mutClassSize > 0) {
			noSideSetFinite(true);
		}
		noSideSetMutationClassSize(mutClassSize);
	}

	/**
	 * Set the size of the mutation class without setting any of the side effects.
	 * 
	 * @param mutClassSize Size of the mutation class
	 */
	private void noSideSetMutationClassSize(int mutClassSize) {
		this.mutClassSize = Optional.of(mutClassSize);
	}

	/**
	 * Get the size of the mutation class of the matrix
	 * 
	 * @return The size of the mutation class
	 * @throws IllegalStateException if the size has not been set
	 */
	public int getMutationClassSize() {
		return mutClassSize.get();
	}

	/**
	 * Check whether it is known that the matrix is minimally mutation infinite.
	 * 
	 * @return true if the value is known
	 */
	public boolean hasMinMutInfSet() {
		return minMutInf.isPresent();
	}

	/**
	 * Set whether the matrix is minimally mutation infinite.
	 * 
	 * @param result true if the matrix is minimally mutation infinite
	 */
	public void setMinMutInf(boolean result) {
		if (result) {
			noSideSetFinite(false);
			noSideSetMutationClassSize(-1);
		}
		noSideSetMinMutInf(result);
	}

	/**
	 * Set whether the matrix is minimally mutation infinite without setting any of the side effects.
	 * 
	 * @param result true if the matrix is minimally mutation infinite
	 */
	private void noSideSetMinMutInf(boolean result) {
		minMutInf = Optional.of(result);
	}

	/**
	 * Get whether the matrix is minimally mutation infinite.
	 * 
	 * @return true if minimally mutation infinite
	 * @throws IllegalStateException if not set
	 */
	public int isMinMutInf() {
		return mutClassSize.get();
	}

	/**
	 * Check whether it is known if the matrix is a Dynkin Diagram.
	 * 
	 * @return true if it is known
	 */
	public boolean hasDynkinDiagram() {
		return diagram.isPresent();
	}

	/**
	 * Set the dynkin diagram that the matrix is.
	 * 
	 * @param diagram The diagram that the matrix is
	 */
	public void setDynkinDiagram(DynkinDiagram diagram) {
		noSideSetFinite(true);
		noSideSetMinMutInf(false);
		noSideSetDynkinDiagram(diagram);
	}

	/**
	 * Set the dynkin diagram that the matrix is without setting any of the side effects.
	 * 
	 * @param diagram The diagram that the matrix is
	 */
	private void noSideSetDynkinDiagram(DynkinDiagram diagram) {
		this.diagram = Optional.of(diagram);
	}

	/**
	 * Get the dynkin diagram that the matrix is.
	 * 
	 * @return The diagram of the matrix
	 * @throws IllegalStateException if not known
	 */
	public DynkinDiagram getDynkinDiagram() {
		return diagram.get();
	}
}
