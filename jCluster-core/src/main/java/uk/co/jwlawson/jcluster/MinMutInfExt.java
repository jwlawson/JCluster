/**
 * Copyright 2014 John Lawson
 * 
 * MinMutInfExt.java is part of JCluster. Licensed under the Apache License, Version 2.0 (the
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

import java.util.ArrayList;
import java.util.Collection;

import uk.co.jwlawson.jcluster.data.EquivQuiverMatrix;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

import com.google.common.base.Preconditions;


/**
 * @author John Lawson
 * 
 */
public class MinMutInfExt extends RunMutationClass {

	protected MinMutInfExt(Builder<?> builder) {
		super(builder);
		RunMinMutInfExtensionsFactory<EquivQuiverMatrix> extFac =
				new RunMinMutInfExtensionsFactory<EquivQuiverMatrix>();
		for (MatrixTaskFactory<QuiverMatrix> fac : builder.mFactories) {
			extFac.addTaskFactory(fac);
		}
		extFac.setResultHandler(builder.mHandler);
		addTaskFactory(extFac);
	}

	public static abstract class Builder<A extends Builder<A>> extends RunMutationClass.Builder<A> {

		private final Collection<MatrixTaskFactory<QuiverMatrix>> mFactories =
				new ArrayList<MatrixTaskFactory<QuiverMatrix>>();
		private MatrixInfoResultHandler mHandler;

		public A addTaskFactory(MatrixTaskFactory<QuiverMatrix> fac) {
			mFactories.add(fac);
			return self();
		}

		public A withMinMutResultHandler(MatrixInfoResultHandler handler) {
			mHandler = handler;
			return self();
		}

		public static Builder<?> builder() {
			return new Builder2();
		}

		@Override
		protected A validate() {
			super.validate();
			Preconditions
					.checkNotNull(mHandler,
							"Result handler for results of tasks run on the MinMutInf matrices cannot be null");
			return self();
		}

		@Override
		public MinMutInfExt build() {
			validate();
			return new MinMutInfExt(this);
		}

		private static class Builder2 extends Builder<Builder2> {

			@Override
			protected Builder2 self() {
				return this;
			}

		}

	}

}
