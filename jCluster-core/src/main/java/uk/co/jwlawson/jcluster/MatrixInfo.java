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

	/** Matrix with these properties. */
	private final QuiverMatrix matrix;
	/** Whether the matrix is mutation finite. */
	private Optional<Boolean> finite = Optional.absent();
	/** Size of the matrix mutation class. */
	private Optional<Integer> mutClassSize = Optional.absent();
	/** Size of the matrix mutation class up to reordering rows and columns. */
	private Optional<Integer> equivMutSize = Optional.absent();
	/** Whether the matrix is minumally mutation infinite. */
	private Optional<Boolean> minMutInf = Optional.absent();
	/** The dynkin diagram the matrix is (if any). */
	private Optional<DynkinDiagram> diagram = Optional.absent();

	/**
	 * Create a new MatrixInfo for the provided matrix.
	 * 
	 * @param m Matrix
	 */
	public MatrixInfo(final QuiverMatrix m) {
		this.matrix = m;
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
	 * @param f true if the matrix is mutation finite
	 */
	public void setFinite(final boolean f) {
		if (f) {
			noSideSetMinMutInf(false);
		}
		noSideSetFinite(f);
	}

	/**
	 * Set if finite without setting any of the side effects.
	 * 
	 * @param f if finite
	 */
	private void noSideSetFinite(final boolean f) {
		this.finite = Optional.of(f);
	}

	/**
	 * Get whether the matrix is mutation finite.
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
	 * @param classSize Size of the mutation class
	 */
	public void setMutationClassSize(final int classSize) {
		if (classSize > 0) {
			noSideSetFinite(true);
		}
		if (classSize == -1) {
			noSideSetFinite(false);
		}
		noSideSetMutationClassSize(classSize);
	}

	/**
	 * Set the size of the mutation class without setting any of the side effects.
	 * 
	 * @param classSize Size of the mutation class
	 */
	private void noSideSetMutationClassSize(final int classSize) {
		this.mutClassSize = Optional.of(classSize);
	}

	/**
	 * Get the size of the mutation class of the matrix.
	 * 
	 * @return The size of the mutation class
	 * @throws IllegalStateException if the size has not been set
	 */
	public int getMutationClassSize() {
		return mutClassSize.get();
	}

	/**
	 * Check whether the size of the mutation class up to reordering rows and columns has been set.
	 * 
	 * @return true if the size has been set
	 */
	public boolean hasEquivMutationClassSize() {
		return equivMutSize.isPresent();
	}

	/**
	 * Set the value of the size of mutation class of this matrix up to reordering rows and columns.
	 * 
	 * @param classSize Size of the mutation class
	 */
	public void setEquivMutationClassSize(final int classSize) {
		if (classSize > 0) {
			noSideSetFinite(true);
		}
		if (classSize == -1) {
			noSideSetFinite(false);
		}
		noSideSetEquivMutationClassSize(classSize);
	}

	/**
	 * Set the size of the mutation class up to reordering rows and columns without setting any of the
	 * side effects.
	 * 
	 * @param classSize Size of the mutation class
	 */
	private void noSideSetEquivMutationClassSize(final int classSize) {
		this.equivMutSize = Optional.of(classSize);
	}

	/**
	 * Get the size of the mutation class of the matrix up to reordering rows and columns.
	 * 
	 * @return The size of the mutation class
	 * @throws IllegalStateException if the size has not been set
	 */
	public int getEquivMutationClassSize() {
		return equivMutSize.get();
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
	public void setMinMutInf(final boolean result) {
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
	private void noSideSetMinMutInf(final boolean result) {
		minMutInf = Optional.of(result);
	}

	/**
	 * Get whether the matrix is minimally mutation infinite.
	 * 
	 * @return true if minimally mutation infinite
	 * @throws IllegalStateException if not set
	 */
	public boolean isMinMutInf() {
		return minMutInf.get();
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
	 * @param d The diagram that the matrix is
	 */
	public void setDynkinDiagram(final DynkinDiagram d) {
		noSideSetFinite(true);
		noSideSetMinMutInf(false);
		noSideSetDynkinDiagram(d);
	}

	/**
	 * Set the dynkin diagram that the matrix is without setting any of the side effects.
	 * 
	 * @param d The diagram that the matrix is
	 */
	private void noSideSetDynkinDiagram(final DynkinDiagram d) {
		this.diagram = Optional.of(d);
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

	/**
	 * Combine the info on the matrix stored in the two MatrixInfo objects.
	 * 
	 * @param info MatrixInfo object containing information about the same matrix
	 * @throws IllegalArgumentException if {@code info} contains information about a different matrix
	 */
	public void combine(final MatrixInfo info) {
		if (!matrix.equals(info.matrix)) {
			throw new IllegalArgumentException(
					"Cannot combine two MatrixInfo objects with different matrices");
		}
		if (!hasFiniteSet() && info.hasFiniteSet()) {
			noSideSetFinite(info.isFinite());
		}
		if (!hasMutationClassSize() && info.hasMutationClassSize()) {
			noSideSetMutationClassSize(info.getMutationClassSize());
		}
		if (!hasEquivMutationClassSize() && info.hasEquivMutationClassSize()) {
			noSideSetEquivMutationClassSize(info.getEquivMutationClassSize());
		}
		if (!hasMinMutInfSet() && info.hasMinMutInfSet()) {
			noSideSetMinMutInf(info.isMinMutInf());
		}
		if (!hasDynkinDiagram() && info.hasDynkinDiagram()) {
			noSideSetDynkinDiagram(info.getDynkinDiagram());
		}
	}
}
