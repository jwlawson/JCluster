/**
 * Copyright 2014 John Lawson
 * 
 * AllSubFiniteCheck.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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



/**
 * @author John Lawson
 * 
 */
public class AllSubFiniteCheck<T extends QuiverMatrix> extends RunSubmatrices<T> {

	public static <S extends QuiverMatrix> AllSubFiniteCheck<S> getInstance(S matrix) {
		return Builder.builder().withInitial(matrix).build(matrix);
	}

	public AllSubFiniteCheck(Builder<T, ?> builder) {
		super(builder);
		setResultHandler(builder.mResultHandler);
	}

	public abstract static class Builder<T extends QuiverMatrix, A extends Builder<T, A>> extends
			RunSubmatrices.Builder<T, A> {

		private MatrixInfoResultHandler mResultHandler;

		@Override
		protected abstract A self();

		public A setResultHandler(MatrixInfoResultHandler handler) {
			mResultHandler = handler;
			return self();
		}

		@Override
		protected Builder<T, A> validate() {
			super.validate();

			if (mResultHandler == null) {
				mResultHandler = new AllFiniteResultHandler(new MatrixInfo(mInitial));
			}
			addFactory(new FiniteCheckTaskFactory<T>());
			return self();
		}

		@Override
		public AllSubFiniteCheck<T> build() {
			validate();
			return new AllSubFiniteCheck<T>(this);
		}

		@SuppressWarnings("unchecked")
		private <S extends QuiverMatrix> AllSubFiniteCheck<S> build(S matrix) {
			return new AllSubFiniteCheck<S>((Builder<S, ?>) this);
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
