/**
 * Copyright 2014 John Lawson
 * 
 * RunAllExtensions.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.data.QuiverMatrix;
import uk.co.jwlawson.jcluster.pool.Pool;
import uk.co.jwlawson.jcluster.pool.Pools;

import com.google.common.base.Preconditions;

/**
 * @author John Lawson
 * 
 */
public class RunAllExtensions<T extends QuiverMatrix> extends RunMultipleTask<T> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private T mMatrix;
	private T mEnlargedMatrix;
	private final Pool<T> mPool;

	protected RunAllExtensions(Builder<T, ?> builder) {
		super(builder);
		setMatrix(builder.mInitial);
		mPool = builder.mPool;
	}

	@Override
	public void setMatrix(T matrix) {
		mMatrix = matrix;
		mEnlargedMatrix = mMatrix.enlargeMatrix(1, 1, mPool.getObj());
	}

	@Override
	protected void submitAllTasks() {
		int size = Math.min(mMatrix.getNumRows(), mMatrix.getNumCols());
		for (int num = 0; num < Math.pow(5, size); num++) {
			submitTaskFor(getExtension(num, size));
		}
	}

	private T getExtension(int num, int size) {
		T matrix = mPool.getObj();
		matrix.set(mEnlargedMatrix);
		for (int i = 0; i < size; i++) {
			int val = (((int) (num / Math.pow(5, i))) % 5) - 2;
			matrix.unsafeSet(size, i, val);
			matrix.unsafeSet(i, size, -val);
		}
		log.debug("Added vertex id: {} to get matrix: {}", num, matrix);
		return matrix;
	}

	public abstract static class Builder<T extends QuiverMatrix, A extends Builder<T, A>> extends
			RunMultipleTask.Builder<T, A> {

		private T mInitial;
		private Pool<T> mPool;

		@Override
		protected abstract A self();

		public A withInitial(T initial) {
			this.mInitial = initial;
			return self();
		}

		public A withPool(Pool<T> pool) {
			this.mPool = pool;
			return self();
		}

		@Override
		protected Builder<T, A> validate() {
			super.validate();
			Preconditions.checkNotNull(mInitial, "mInitial may not be null");

			if (mPool == null) {
				@SuppressWarnings("unchecked")
				Pool<T> pool =
						(Pool<T>) Pools.getQuiverMatrixPool(mInitial.getNumRows() - 1,
								mInitial.getNumCols() - 1, mInitial.getClass());
				mPool = pool;
			}
			return this;
		}

		public RunAllExtensions<T> build() {
			validate();
			return new RunAllExtensions<T>(this);
		}

		public static <T extends QuiverMatrix> Builder<T, ?> builder() {
			return new Builder2<T>();
		}

		private static class Builder2<T extends QuiverMatrix> extends Builder<T, Builder2<T>> {

			@Override
			protected Builder2<T> self() {
				return this;
			}

		}
	}

}
