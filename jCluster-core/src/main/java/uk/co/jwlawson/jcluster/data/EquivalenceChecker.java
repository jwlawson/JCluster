/**
 * Copyright 2014 John Lawson
 * 
 * EquivalenceChecker.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.pool.Pool;
import uk.co.jwlawson.jcluster.pool.Pools;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;

/**
 * Checks whether two {@link IntMatrix} objects are equivalent up to permutations of their rows and
 * columns. Results are cached as the calculation can be slow especially for large matrices.
 * 
 * <p>
 * Instances of {@link EquivalenceChecker} are cached as each instance contains all permutation
 * matrices of the size of the {@link EquivalenceChecker}.
 * 
 * @author John Lawson
 * 
 */
public final class EquivalenceChecker {

	/** Logger. */
	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Constant value which represents an invalid permutation. */
	private static final int[] NO_PERMUTATION = new int[0];

	/**
	 * The cache which stores {@link EquivalenceChecker} instances. There is a maximum bound on it
	 * to prevent unused instances filling memory, which roughly corresponds to how much memory is
	 * being used by the instance.
	 */
	private static LoadingCache<Integer, EquivalenceChecker> sInstanceCache = CacheBuilder
			.newBuilder().maximumWeight(400000000) // Max num for 10x10 is 363 million
			.weigher(new Weigher<Integer, EquivalenceChecker>() {

				@Override
				public int weigh(final Integer key, final EquivalenceChecker value) {
					return value.mPermMatrices.length * key * key;
				}
			}).build(new CacheLoader<Integer, EquivalenceChecker>() {
				private final Logger log = LoggerFactory.getLogger(getClass());

				@Override
				public EquivalenceChecker load(final Integer key) throws Exception {
					if (key == 0) {
						throw new IllegalArgumentException(
								"Cannot have an EquivalenceChecker with size 0");
					}
					log.info("New EquivalenceChecker of size {} created", key);
					return new EquivalenceChecker(key);
				}

			});

	/**
	 * Cache storing previously checked equivalences between pairs of matrices. For larger matrices
	 * the time spent multiplying matrices together becomes prohibitive, so caching helps to speed
	 * up the checks.
	 */
	private final LoadingCache<IntMatrixPair, Boolean> mPermCache = CacheBuilder.newBuilder()
			.maximumSize(500000).build(new CacheLoader<IntMatrixPair, Boolean>() {

				/*
				 * When a new cache entry is loaded, also load the same result with the pair
				 * switched over. This means that fewer calculations have to be done, but more
				 * memory is used.
				 * 
				 * The pair cannot be made agnostic to the order of its matrices as that results in
				 * a much weaker hashcode and mistakes in the cache. (non-Javadoc)
				 * 
				 * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
				 */
				@Override
				public Boolean load(final IntMatrixPair key) throws Exception {
					boolean result = areUncachedEquivalent(key.getA(), key.getB());
					IntMatrixPair opp = new IntMatrixPair();
					opp.set(key.getB(), key.getA());
					mPermCache.put(opp, result);
					return result;
				}

			});

	/** Array of all permutation matrices of this size. */
	private IntMatrix[] mPermMatrices;
	/** Matrix storing the multiplication. */
	private final IntMatrix mMatrixAP;
	/** Matrix string the multiplication. */
	private final IntMatrix mMatrixPB;
	/** Pool of matrix pairs. */
	private final Pool<IntMatrixPair> mPairPool;

	/**
	 * Get an instance of {@link EquivalenceChecker} of the provided size. The instances are cached,
	 * so it is likely that the same instance is provided if called multiple times, but that is not
	 * guaranteed.
	 * 
	 * @param size Size of matrices which the {@link EquivalenceChecker} will check
	 * @return An instance of {@link EquivalenceChecker}
	 */
	public static EquivalenceChecker getInstance(final int size) {
		return sInstanceCache.getUnchecked(size);
	}

	/**
	 * Class to test whether matrices are equivalent up to permutation of their rows and columns.
	 * Constructor creates all permutation matrices of the required size, so should not be
	 * instantiated many times but rather cached.
	 * 
	 * @param size The size of the matrices which will be checked for equivalence
	 */
	private EquivalenceChecker(final int size) {
		setPermutations(size);

		mMatrixAP = new IntMatrix(size, size);
		mMatrixPB = new IntMatrix(size, size);
		mPairPool = Pools.getIntMatrixPairPool();
	}

	/**
	 * Generate all permutation matrices of the required size.
	 * 
	 * @param size The size of the matrices to construct
	 */
	private void setPermutations(final int size) {
		int fac = factorial(size);
		mPermMatrices = new IntMatrix[fac];
		int count = 0;
		for (int i = 0; i < Math.pow(size, size); i++) {
			int[] vals = getPermValues(size, i);
			if (vals == NO_PERMUTATION) {
				continue;
			}
			mPermMatrices[count] = new IntMatrix(size, size);
			for (int j = 0; j < size; j++) {
				mPermMatrices[count].set(j, vals[j], 1);
			}
			count++;
		}
		if (count != fac) {
			throw new RuntimeException("Wrong number of permutations");
		}
	}

	/**
	 * Calculate the columns that the 1 in each row of the permutation matrix should go into. If the
	 * provided id is not a valid permutation then the constant
	 * {@link EquivalenceChecker.NO_PERMUTATION} is returned.
	 * 
	 * @param size Size of the permutation matrix required
	 * @param i Id for the permutation matrix
	 * @return An array of column numbers indicating the positions of the 1s, or NO_PERMUTATION if
	 *         an invalid id is provided
	 */
	private int[] getPermValues(final int size, final int i) {
		int[] result = new int[size];
		int id = i;
		for (int j = 0; j < size; j++) {
			result[j] = id % (size);
			for (int k = 0; k < j; k++) {
				if (result[j] == result[k]) {
					return NO_PERMUTATION;
				}
			}
			id /= size;
		}
		return result;
	}

	/**
	 * Find the value of num!
	 * 
	 * @param num Number to start calculating from
	 * @return The value of num factorial
	 */
	private int factorial(final int num) {
		if (num == 1) {
			return 1;
		}
		if (num == 0) {
			return 0;
		}
		return num * factorial(num - 1);
	}

	/**
	 * Calculate directly whether the matrices are equivalent up to permutation of rows and columns
	 * without looking up in the cache.
	 * 
	 * <p>
	 * A lot of calculation is done before blindly multiplying matrices as for larger matrices this
	 * is horrifically slow. As the sum of each row is invariant under permutations of rows and
	 * columns these values are calculated and it is checked that each matrix has the same number of
	 * rows with the same sum. These also show narrow which permutations could potentially be the
	 * right ones so the invalid ones are not considered.
	 * 
	 * @param a The first matrix
	 * @param b The second matrix
	 * @return true if the matrices are equivalent
	 */
	private Boolean areUncachedEquivalent(final IntMatrix a, final IntMatrix b) {
		if (IntMatrix.areEqual(a, b)) {
			return true;
		}
		int aRows = a.getNumRows();
		int aCols = a.getNumCols();
		int[] aRowSum = new int[aRows];
		int[] aColSum = new int[aCols];
		int[] aAbsRowSum = new int[aRows];
		int[] aAbsColSum = new int[aCols];

		int[] bRowSum = new int[aRows];
		int[] bColSum = new int[aCols];
		int[] bAbsRowSum = new int[aRows];
		int[] bAbsColSum = new int[aCols];
		for (int i = 0; i < aRows; i++) {
			for (int j = 0; j < aCols; j++) {
				int aVal = a.unsafeGet(i, j);
				aRowSum[i] += aVal;
				aColSum[j] += aVal;
				aAbsRowSum[i] += Math.abs(aVal);
				aAbsColSum[j] += Math.abs(aVal);

				int bVal = b.unsafeGet(i, j);
				bRowSum[i] += bVal;
				bColSum[j] += bVal;
				bAbsRowSum[i] += Math.abs(bVal);
				bAbsColSum[j] += Math.abs(bVal);
			}
		}
		if (!areArraysEquivalent(aRowSum, bRowSum)) {
			return false;
		}
		if (!areArraysEquivalent(aColSum, bColSum)) {
			return false;
		}
		if (!areArraysEquivalent(aAbsRowSum, bAbsRowSum)) {
			return false;
		}
		if (!areArraysEquivalent(aAbsColSum, bAbsColSum)) {
			return false;
		}

		int[] rowMappings = getMappingArray(aRows);
		int[] colMappings = getMappingArray(aCols);

		boolean rowsMatch =
				checkRowsMatch(a, b, aRows, aRowSum, aAbsRowSum, bRowSum, bAbsRowSum, rowMappings);
		if (!rowsMatch) {
			return false;
		}

		boolean columnsMatch =
				checkColumnsMatch(a, b, aCols, aColSum, aAbsColSum, bColSum, bAbsColSum,
						colMappings);
		if (!columnsMatch) {
			return false;
		}
		int[] colCopy = colMappings.clone();
		for (IntMatrix p : mPermMatrices) {
			for (int i = 0; i < rowMappings.length; i++) {
				colCopy[i] = colMappings[i];
			}
			boolean notValid = !isPermutationValid(aRows, aCols, colCopy, p);
			if (notValid) {
				continue;
			}
			// Check if PA == BP or PAP^(-1) == B
			synchronized (this) {
				a.multRight(p, mMatrixAP);
				b.multLeft(p, mMatrixPB);
				if (IntMatrix.areEqual(mMatrixPB, mMatrixAP)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get an array to store the mapping information in.
	 * 
	 * @param length Length of the array
	 * @return Array
	 */
	private int[] getMappingArray(final int length) {
		int[] arr = new int[length];
		for (int i = 0; i < length; i++) {
			arr[i] = 57;
		}
		return arr;
	}

	/**
	 * Check that the columns in the matrices match up to permutations.
	 * 
	 * @param a First matrix
	 * @param b Second matrix
	 * @param aCols number of columns
	 * @param aColSum Sums of the first matrices columns
	 * @param aAbsColSum Sums of the abs value of the first matrices columns
	 * @param bColSum Sums of the second matrices columns
	 * @param bAbsColSum Sums of the abs value of the second matrices columns
	 * @param colMappings Array of column mappings to update
	 * @return true if the matrices have matching columns
	 */
	private boolean checkColumnsMatch(final IntMatrix a, final IntMatrix b, final int aCols,
			final int[] aColSum, final int[] aAbsColSum, final int[] bColSum,
			final int[] bAbsColSum, final int[] colMappings) {
		boolean columnsMatch = true;
		for (int aInd = 0; aInd < aCols; aInd++) {
			int inCol = numberIn(bColSum, aColSum[aInd]);
			int index = -1;
			int[] aColVals = a.getCol(aInd);
			boolean foundEquiv = false;
			for (int i = 0; i < inCol; i++) {
				index = getNextIndexOf(bColSum, aColSum[aInd], index);
				if (bAbsColSum[index] != aAbsColSum[aInd]) {
					// Columns will only be equivalent if they have the same absolute value sum
					continue;
				}
				int[] bColVals = b.getCol(index);
				if (areArraysEquivalent(aColVals, bColVals)) {
					foundEquiv = true;
					updateMapping(aCols, colMappings, index, aInd);
				}
			}
			if (!foundEquiv) {
				columnsMatch = false;
			}
		}
		return columnsMatch;
	}

	private boolean checkRowsMatch(final IntMatrix a, final IntMatrix b, final int aRows,
			final int[] aRowSum, final int[] aAbsRowSum, final int[] bRowSum,
			final int[] bAbsRowSum, final int[] rowMappings) {
		boolean rowsMatch = true;
		for (int aInd = 0; aInd < aRows && rowsMatch; aInd++) {
			int inRow = numberIn(bRowSum, aRowSum[aInd]);
			int index = -1;
			int[] aRowVals = a.getRow(aInd);
			boolean foundEquiv = false;
			for (int i = 0; i < inRow; i++) {
				index = getNextIndexOf(bRowSum, aRowSum[aInd], index);
				if (bAbsRowSum[index] != aAbsRowSum[aInd]) {
					// Rows will only be equivalent if they have the same absolute value sum
					continue;
				}
				int[] bRowVals = b.getRow(index);
				if (areArraysEquivalent(aRowVals, bRowVals)) {
					foundEquiv = true;
					updateMapping(aRows, rowMappings, aInd, index);
				}
			}
			if (!foundEquiv) {
				rowsMatch = false;
			}
		}
		return rowsMatch;
	}

	private void updateMapping(final int numRows, final int[] rowMappings, final int aIndex,
			final int bIndex) {
		rowMappings[bIndex] *= numRows;
		rowMappings[bIndex] += aIndex;
	}

	private boolean isPermutationValid(final int aRows, final int aCols, final int[] colMappings,
			final IntMatrix p) {
		for (int i = 0; i < colMappings.length; i++) {
			if (!isColumnValid(aCols, colMappings, p, i)) {
				return false;
			}
		}
		return true;
	}

	private boolean isColumnValid(final int num, final int[] mappings, final IntMatrix perm,
			final int index) {
		while (mappings[index] != 57) {
			if (perm.unsafeGet(index, mappings[index] % num) == 1) {
				return true;
			}
			mappings[index] /= num;
		}
		return false;
	}

	private int numberIn(final int[] arr, final int val) {
		int count = 0;
		for (int i : arr) {
			if (i == val) {
				count++;
			}
		}
		return count;
	}

	private int getNextIndexOf(final int[] arr, final int val, final int prev) {
		for (int i = prev + 1; i < arr.length; i++) {
			if (arr[i] == val) {
				return i;
			}
		}
		return -1;
	}

	private boolean areArraysEquivalent(final int[] a, final int[] b) {
		int[] aCopy = Arrays.copyOf(a, a.length);
		int[] bCopy = Arrays.copyOf(b, b.length);
		Arrays.sort(aCopy);
		Arrays.sort(bCopy);
		return Arrays.equals(aCopy, bCopy);

	}

	/**
	 * Check whether two matrices are equivalent up to permutations of the rows and columns.
	 * 
	 * @param a The first matrix
	 * @param b The second matrix
	 * @return true if the two are equivalent
	 */
	public boolean areEquivalent(final IntMatrix a, final IntMatrix b) {
		IntMatrixPair pair = null;
		try {
			pair = mPairPool.getObj();
			pair.set(a, b);
			return mPermCache.getUnchecked(pair);
		} catch (RuntimeException e) {
			log.error("Error getting IntMatrixPair instance", e);
			return areUncachedEquivalent(a, b);
		} finally {
			if (pair != null) {
				mPairPool.returnObj(pair);
			}
		}
	}
}
