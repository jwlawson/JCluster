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
	private final int[] NO_PERMUTATION = new int[0];

	private static LoadingCache<Integer, EquivalenceChecker> sInstanceCache = CacheBuilder
			.newBuilder().maximumWeight(1000000).weigher(new Weigher<Integer, EquivalenceChecker>() {

				public int weigh(Integer key, EquivalenceChecker value) {
					return value.mPermMatrices.length * value.mPermMatrices[0].getNumRows();
				}
			}).build(new CacheLoader<Integer, EquivalenceChecker>() {

				@Override
				public EquivalenceChecker load(Integer key) throws Exception {
					log.info("New equivalenceChecker of size {} created", key);
					return new EquivalenceChecker(key);
				}

			});

	private final LoadingCache<IntMatrixPair, Boolean> mPermCache = CacheBuilder.newBuilder()
			.maximumSize(500000).build(new CacheLoader<IntMatrixPair, Boolean>() {

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

	private int factorial(int num) {
		if (num == 1) {
			return 1;
		}
		return num * factorial(num - 1);
	}

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
	 * @return Whether the two are equivalent
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
