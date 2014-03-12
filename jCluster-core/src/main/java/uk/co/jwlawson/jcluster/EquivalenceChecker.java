package uk.co.jwlawson.jcluster;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;

public class EquivalenceChecker {

	private final static Logger log = LoggerFactory.getLogger(EquivalenceChecker.class);

	/** Constant value which represents an invalid permutation. */
	private final int[] NO_PERMUTATION = new int[0];

	/**
	 * The cache which stores {@link EquivalenceChecker} instances. There is a maximum bound on it to
	 * prevent unused instances filling memory, which roughly corresponds to how much memory is being
	 * used by the instance.
	 */
	private static LoadingCache<Integer, EquivalenceChecker> sInstanceCache = CacheBuilder
			.newBuilder().maximumWeight(10000000).weigher(new Weigher<Integer, EquivalenceChecker>() {

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
	 * the time spent multiplying matrices together becomes prohibitive, so caching helps to speed up
	 * the checks.
	 */
	private final LoadingCache<IntMatrixPair, Boolean> mPermCache = CacheBuilder.newBuilder()
			.maximumSize(500000).build(new CacheLoader<IntMatrixPair, Boolean>() {

				/*
				 * When a new cache entry is loaded, also load the same result with the pair switched over.
				 * This means that fewer calculations have to be done, but more memory is used.
				 * 
				 * The pair cannot be made agnostic to the order of its matrices as that results in a much
				 * weaker hashcode and mistakes in the cache. (non-Javadoc)
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
	 * @return An array of column numbers indicating the positions of the 1s, or NO_PERMUTATION if an
	 *         invalid id is provided
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
