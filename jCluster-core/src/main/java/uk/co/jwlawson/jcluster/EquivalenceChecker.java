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
package uk.co.jwlawson.jcluster;

import java.util.Arrays;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class EquivalenceChecker {

	private final static Logger log = LoggerFactory.getLogger(EquivalenceChecker.class);

	/** Constant value which represents an invalid permutation. */
	private final int[] NO_PERMUTATION = new int[0];

	/**
	 * The cache which stores {@link EquivalenceChecker} instances. There is a maximum bound on it
	 * to prevent unused instances filling memory, which roughly corresponds to how much memory is
	 * being used by the instance.
	 */
	private static LoadingCache<Integer, EquivalenceChecker> sInstanceCache = CacheBuilder
			.newBuilder().maximumWeight(10000000)
			.weigher(new Weigher<Integer, EquivalenceChecker>() {

				public int weigh(Integer key, EquivalenceChecker value) {
					return value.mPermMatrices.length * value.mPermMatrices[0].getNumRows()
							* value.mPermMatrices[0].getNumCols();
				}
			}).build(new CacheLoader<Integer, EquivalenceChecker>() {

				@Override
				public EquivalenceChecker load(Integer key) throws Exception {
					log.info("New equivalenceChecker of size {} created", key);
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
				public Boolean load(IntMatrixPair key) throws Exception {
					boolean result = areUncachedEquivalent(key.a, key.b);
					IntMatrixPair opp = new IntMatrixPair();
					opp.set(key.b, key.a);
					mPermCache.put(opp, result);
					return result;
				}

			});

	private IntMatrix[] mPermMatrices;
	private final IntMatrix mMatrixPA;
	private final IntMatrix mMatrixBP;
	private final ObjectPool<IntMatrixPair> mPairPool;

	/**
	 * Get an instance of {@link EquivalenceChecker} of the provided size. The instances are cached,
	 * so it is likely that the same instance is provided if called multiple times, but that is not
	 * guaranteed.
	 * 
	 * @param size Size of matrices which the {@link EquivalenceChecker} will check
	 * @return An instance of {@link EquivalenceChecker}
	 */
	public static EquivalenceChecker getInstance(int size) {
		return sInstanceCache.getUnchecked(size);
	}

	/**
	 * Class to test whether matrices are equivalent up to permutation of their rows and columns.
	 * Constructor creates all permutation matrices of the required size, so should not be
	 * instantiated many times but rather cached.
	 * 
	 * @param size The size of the matrices which will be checked for equivalence
	 */
	private EquivalenceChecker(int size) {
		setPermutations(size);

		mMatrixPA = new IntMatrix(size, size);
		mMatrixBP = new IntMatrix(size, size);
		mPairPool = Pools.getIntMatrixPairPool();
	}

	/**
	 * Generate all permutation matrices of the required size.
	 * 
	 * @param size The size of the matrices to construct
	 */
	private void setPermutations(int size) {
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
			log.debug("" + mPermMatrices[count]);
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
	private int[] getPermValues(int size, int i) {
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
	private int factorial(int num) {
		if (num == 1) {
			return 1;
		}
		return num * factorial(num - 1);
	}

	/**
	 * Calculate directly whether the matrices are equivalent up to permutation of rows and columns
	 * without looking up in the cache.
	 * 
	 * @param a The first matrix
	 * @param b The second matrix
	 * @return true if the matrices are equivalent
	 */
	private Boolean areUncachedEquivalent(IntMatrix a, IntMatrix b) {
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
		if (!areArraysEquivalent(aRowSum, bRowSum))
			return false;
		if (!areArraysEquivalent(aColSum, bColSum))
			return false;
		if (!areArraysEquivalent(aAbsRowSum, bAbsRowSum))
			return false;
		if (!areArraysEquivalent(aAbsColSum, bAbsColSum))
			return false;

		for (int aInd = 0; aInd < aRows; aInd++) {
			int inRow = numberIn(bRowSum, aRowSum[aInd]);
			if (inRow == 1) {
				int bInd = getIndexOf(bRowSum, aRowSum[aInd]);
				int[] bRowVals = b.getRow(bInd);
				int[] aRowVals = a.getRow(aInd);
				if (!areArraysEquivalent(aRowVals, bRowVals)) {
					return false;
				}
			} else {
				int inAbs = numberIn(bAbsRowSum, aAbsRowSum[aInd]);
				int index = -1;
				int[] aRowVals = a.getRow(aInd);
				boolean foundEquiv = false;
				for (int i = 0; i < inAbs; i++) {
					index = getNextIndexOf(bAbsRowSum, aAbsRowSum[aInd], index);
					if (bRowSum[index] != aRowSum[aInd]) {
						continue;
					}
					int[] bRowVals = b.getRow(index);
					if (areArraysEquivalent(aRowVals, bRowVals)) {
						foundEquiv = true;
						break;
					}
				}
				if (!foundEquiv) {
					return false;
				}
			}
		}

		for (int aInd = 0; aInd < aCols; aInd++) {
			int inCol = numberIn(bColSum, aColSum[aInd]);
			if (inCol == 1) {
				int bInd = getIndexOf(bColSum, aColSum[aInd]);
				int[] bColVals = b.getCol(bInd);
				int[] aColVals = a.getCol(aInd);
				if (!areArraysEquivalent(aColVals, bColVals)) {
					return false;
				}
			} else {
				int inAbs = numberIn(bAbsColSum, aAbsColSum[aInd]);
				int index = -1;
				int[] aColVals = a.getCol(aInd);
				boolean foundEquiv = false;
				for (int i = 0; i < inAbs; i++) {
					index = getNextIndexOf(bAbsColSum, aAbsColSum[aInd], index);
					if (bColSum[index] != aColSum[aInd]) {
						continue;
					}
					int[] bColVals = b.getCol(index);
					if (areArraysEquivalent(aColVals, bColVals)) {
						foundEquiv = true;
						break;
					}
				}
				if (!foundEquiv) {
					return false;
				}
			}
		}


		for (IntMatrix p : mPermMatrices) {
			// Check if PA == BP
			// or PAP^(-1) == B
			synchronized (this) {
				a.multLeft(p, mMatrixPA);
				b.multRight(p, mMatrixBP);
				if (IntMatrix.areEqual(mMatrixBP, mMatrixPA)) {
					return true;
				}
			}
		}
		return false;
	}

	private int numberIn(int[] arr, int val) {
		int count = 0;
		for (int i : arr) {
			if (i == val) {
				count++;
			}
		}
		return count;
	}

	private int getIndexOf(int[] arr, int val) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == val) {
				return i;
			}
		}
		return -1;
	}

	private int getNextIndexOf(int[] arr, int val, int prev) {
		for (int i = prev + 1; i < arr.length; i++) {
			if (arr[i] == val) {
				return i;
			}
		}
		return -1;
	}

	private boolean areArraysEquivalent(int[] a, int[] b) {
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
	public boolean areEquivalent(IntMatrix a, IntMatrix b) {
		IntMatrixPair pair = null;
		try {
			pair = mPairPool.getObj();
			pair.set(a, b);
			return mPermCache.getUnchecked(pair);
		} catch (PoolException e) {
			log.error("Error getting IntMatrixPair instance", e);
			return areUncachedEquivalent(a, b);
		} finally {
			if (pair != null) {
				mPairPool.returnObj(pair);
			}
		}
	}
}
