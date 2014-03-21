/**
 * Copyright 2014 John Lawson
 * 
 * RunMinMutInfResults.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

/**
 * @author John Lawson
 * 
 */
public class RunMinMutInfResults extends RunOnResults {

	protected RunMinMutInfResults(Builder<?> builder) {
		super(builder);
	}

	@Override
	public void setMatrix(QuiverMatrix matrix) {}

	@Override
	protected RunOnResultsHandler getSubmittingResultsHandler(MatrixInfo initial) {
		return new MinMutInfResultHandler(initial);
	}

	private class MinMutInfResultHandler extends RunOnResultsHandler {

		public MinMutInfResultHandler(MatrixInfo initial) {
			super(initial);
		}

		@Override
		protected boolean shouldSubmit(MatrixInfo matrix) {
			return matrix.hasMinMutInfSet() && matrix.isMinMutInf();
		}

	}

	public static abstract class Builder<A extends Builder<A>> extends RunOnResults.Builder<A> {

		@Override
		protected abstract A self();

		@Override
		protected Builder<A> validate() {
			super.validate();
			return self();
		}

		public RunMinMutInfResults build() {
			validate();
			return new RunMinMutInfResults(this);
		}

		public static Builder<?> builder() {
			return new Builder2();
		}

		private static class Builder2 extends Builder<Builder2> {

			@Override
			protected Builder2 self() {
				return this;
			}

		}
	}
}
