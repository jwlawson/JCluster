/**
 * Copyright 2014 John Lawson
 * 
 * RunMinMutInfExtensions.java is part of JCluster. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.co.jwlawson.jcluster.data.MatrixInfo;
import uk.co.jwlawson.jcluster.data.QuiverMatrix;

import com.google.common.base.Preconditions;

/**
 * @author John Lawson
 * 
 */
public class RunMinMutInfExtensions<T extends QuiverMatrix> implements MatrixTask<T> {

	private RunAllExtensions<T> mExtTask;
	private RunMinMutInfResults mMinMutInfSubmitter;
	private final ExecutorService mExtTaskExec;
	private final boolean mShutdownExtExec;

	private RunMinMutInfExtensions(Builder<T> builder) {
		setMatrix(builder.mMatrix);
		for (MatrixTaskFactory<QuiverMatrix> fac : builder.mFactories) {
			addTaskFactory(fac);
		}
		setResultHandler(builder.mResultHandler);
		mExtTaskExec = builder.mExtTaskExec;
		mShutdownExtExec = builder.mShutdownExtExec;
	}

	public final void addTaskFactory(MatrixTaskFactory<QuiverMatrix> factory) {
		mMinMutInfSubmitter.addTaskFactory(factory);
	}

	public final void setResultHandler(TECSResultHandler handler) {
		mMinMutInfSubmitter.setResultHandler(handler);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setMatrix(T matrix) {
		mExtTask =
				(RunAllExtensions<T>) RunAllExtensions.Builder.builder()
						.addFactory(new MinMutInfCheckFactory()).withInitial(matrix).build();
		mMinMutInfSubmitter =
				RunMinMutInfResults.Builder.builder().withSubmittingTask(mExtTask).build();
	}

	@Override
	public void reset() {}

	@Override
	public final void requestStop() {
		mExtTask.requestStop();
		mMinMutInfSubmitter.requestStop();
	}

	@Override
	public MatrixInfo call() throws Exception {
		mExtTaskExec.submit(mExtTask);
		try {
			return mMinMutInfSubmitter.call();
		} finally {
			if (mShutdownExtExec) {
				mExtTaskExec.shutdownNow();
			}
		}
	}

	public static class Builder<T extends QuiverMatrix> {

		private T mMatrix;
		private final List<MatrixTaskFactory<QuiverMatrix>> mFactories;
		private TECSResultHandler mResultHandler;
		private ExecutorService mExtTaskExec;
		private boolean mShutdownExtExec = false;


		private Builder() {
			mFactories = new ArrayList<MatrixTaskFactory<QuiverMatrix>>();
		}

		public static <T extends QuiverMatrix> Builder<T> builder() {
			return new Builder<T>();
		}

		public <S extends QuiverMatrix> Builder<S> withInitial(S matrix) {
			@SuppressWarnings("unchecked")
			Builder<S> self = (Builder<S>) this;
			self.mMatrix = matrix;
			return self;
		}

		public Builder<T> addTaskFactory(MatrixTaskFactory<QuiverMatrix> factory) {
			mFactories.add(factory);
			return this;
		}

		public Builder<T> withResultHandler(TECSResultHandler handler) {
			mResultHandler = handler;
			return this;
		}

		public Builder<T> withExtensionExecutor(ExecutorService exec) {
			mExtTaskExec = exec;
			return this;
		}

		public RunMinMutInfExtensions<T> build() {
			validate();
			return new RunMinMutInfExtensions<T>(this);

		}

		private void validate() {
			Preconditions.checkNotNull(mMatrix, "Initial matrix cannot be null");
			Preconditions.checkNotNull(mResultHandler, "Result handler cannot be null");
			if (mExtTaskExec == null) {
				mExtTaskExec = Executors.newSingleThreadExecutor();
				mShutdownExtExec = true;
			}
		}
	}
}
