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
public abstract class EquivalenceChecker {

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
					return factorial(key) * key * key;
				}
			}).build(new CacheLoader<Integer, EquivalenceChecker>() {
				private final Logger log = LoggerFactory.getLogger(getClass());

				@Override
				public EquivalenceChecker load(final Integer key) throws Exception {
					if (key == 0) {
						log.debug("New EquivalenceChecker of size {} created", key);
						return new EquivalenceCheckerZero();
					}
					log.debug("New EquivalenceChecker of size {} created", key);
					return new EquivalenceCheckerImpl(key);
				}

			});

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
	 * Find the value of num!
	 * 
	 * @param num Number to start calculating from
	 * @return The value of num factorial
	 */
	private static int factorial(final int num) {
		if (num == 1) {
			return 1;
		}
		if (num == 0) {
			return 0;
		}
		return num * factorial(num - 1);
	}

	/**
	 * Check whether two matrices are equivalent up to permutations of the rows and columns.
	 * 
	 * @param a The first matrix
	 * @param b The second matrix
	 * @return true if the two are equivalent
	 */
	public abstract boolean areEquivalent(final IntMatrix a, final IntMatrix b);

}
