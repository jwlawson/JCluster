/**
 * Copyright 2014 John Lawson
 * 
 * AbstractMutClassSizeTask.java is part of JCluster. Licensed under the Apache License, Version 2.0
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.pool.Pool;

/**
 * 
 * @author John Lawson
 * 
 * @param <T>
 */
public abstract class AbstractMutClassSizeTask<T extends QuiverMatrix> implements MatrixTask<T> {

	/** Value returned when the calculation was stopped prematurely. */
	public static final int STOP = Integer.MIN_VALUE;

	/** Value returned if the mutation class is found to be infinite. */
	public static final int INFINITE = -1;

	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Initial matrix to find mutation class of. */
	private T mInitialMatrix;
	/** True if the calculation should be continued. */
	private boolean mShouldRun;
	/** Number of iterations to run between updating the stats. */
	private int mIterationsBetweenStats;
	/** List of listeners waiting for the stats to be updated. */
	private final List<StatsListener> mStatsListeners;

	/**
	 * Create a new instance. Initialises the listeners and adds a logger listener.
	 */
	public AbstractMutClassSizeTask() {
		mStatsListeners = new ArrayList<AbstractMutClassSizeTask.StatsListener>();
		addListener(new StatsLogger());
	}

	/**
	 * Create a new instance with an initial matrix already set.
	 * 
	 * @param matrix Initial matrix to find the mutation class of
	 */
	public AbstractMutClassSizeTask(final T matrix) {
		this();
		setMatrix(matrix);
	}

	@Override
	public final void setMatrix(final T matrix) {
		mInitialMatrix = matrix;
	}

	@Override
	public void reset() {
		mShouldRun = true;
	}

	/**
	 * Add a listener to changes to the Stats object associated to this task.
	 * 
	 * @param listener The StatsListener to add
	 * */
	public final void addListener(final StatsListener listener) {
		mStatsListeners.add(listener);
	}

	/**
	 * Set the number of iterations calculated between updates to the stats object.
	 * 
	 * <p>
	 * For the standard size finder which uses no equivalence this is 50000 by default as otherwise
	 * the method is called an excessive amount of times.
	 * 
	 * @param number Number of iterations between Stats updates
	 */
	public final void setIterationsBetweenStats(final int number) {
		mIterationsBetweenStats = number;
	}

	/**
	 * Find the number of matrices in the mutation class of the initial matrix. If the matrix is
	 * mutation-infinite then -1 is returned.
	 * 
	 * @return The size of the mutation class, or -1 if infinite
	 * @throws Exception if something goes wrong in the calculation
	 */
	@Override
	public final MatrixInfo call() throws Exception {
		log.debug("MutClassSizeTask started for {}", mInitialMatrix);
		MatrixInfo result = handleResult(getMatrixInfo(), getMutationClassSize());
		return result;
	}

	/**
	 * Insert the result from the calculation into the MatrixInfo object which will be returned from
	 * the {@link AbstractMutClassSizeTask#call()} method. This can be used to ensure that the
	 * result is inserted at the right point in the MatrixInfo, or to provide a different MatrixInfo
	 * object if required.
	 * 
	 * @param info MatrixInfo containing initial matrix and any known info about it
	 * @param result Mutation class size just calculated
	 * @return MatrixInfo object to be returned by the call() method
	 */
	protected abstract MatrixInfo handleResult(final MatrixInfo info, final int result);

	/**
	 * Get the MatrixInfo object which should be returned by call().
	 * 
	 * @return Matrix info object
	 */
	protected final MatrixInfo getMatrixInfo() {
		MatrixInfo result = new MatrixInfo(mInitialMatrix);
		return result;
	}

	/**
	 * Calculate the size of the mutation class.
	 * 
	 * @return Size of the mutation class
	 */
	protected final Integer getMutationClassSize() {
		int size = getSize(mInitialMatrix);
		int numMatrices = 0;

		Map<T, LinkHolder<T>> matrixSet = getMatrixMap(size);
		Pool<T> quiverPool = getQuiverPool();
		Pool<LinkHolder<T>> holderPool = getHolderPool(size);
		Queue<T> incompleteQuivers = new ArrayDeque<T>((int) Math.pow(2, 3 * size - 3));

		T m = quiverPool.getObj();
		m.set(mInitialMatrix);

		LinkHolder<T> initial = holderPool.getObj();
		initial.setMatrix(m);
		matrixSet.put(m, initial);

		incompleteQuivers.add(m);

		setUp(m);

		Stats stats = new Stats();
		mShouldRun = true;
		try {
			T mat;
			T newMatrix;
			int i;
			stats.start();
			do {
				mat = incompleteQuivers.poll();
				for (i = 0; i < size && mShouldRun; i++) {
					if (shouldMutateAt(mat, i, matrixSet)) {
						newMatrix = quiverPool.getObj();
						newMatrix = mat.mutate(i, newMatrix);
						if (matrixSeenBefore(newMatrix, matrixSet)) {
							handleSeenMatrix(matrixSet, mat, newMatrix, i);
							if (isMatrixComplete(newMatrix, matrixSet)) {
								removeComplete(newMatrix, quiverPool, holderPool, matrixSet);
							} else {
								returnMatrix(newMatrix, quiverPool);
							}
						} else {
							if (newMatrix.isInfinite()) {
								log.debug("Infinite matrix found {} in classs of {}", newMatrix,
										mInitialMatrix);
								return INFINITE;
							}
							handleUnseenMatrix(matrixSet, incompleteQuivers, holderPool, mat,
									newMatrix, i);
						}
					}
				}
				removeHandledQuiver(mat, quiverPool, holderPool, matrixSet);
				if (numMatrices % mIterationsBetweenStats == 0 && numMatrices != 0) {
					stats.update(matrixSet, numMatrices);
				}
				numMatrices++;
				stats.iterationComplete();
			} while (!incompleteQuivers.isEmpty() && mShouldRun);
			log.info("Graph completed. Vertices: {}", numMatrices);
			if (mShouldRun) {
				return numMatrices;
			} else {
				return STOP;
			}
		} finally {
			teardown(quiverPool, holderPool, matrixSet);
		}
	}

	/**
	 * Called just before the task is started.
	 * <p>
	 * The matrix a copy of the initial matrix that the task is starting from, but the instance has
	 * come from the thread local pool to ensure that it can be returned once the task is complete.
	 * 
	 * @param pooledInitial Copy of the initial matrix from the thread local pool
	 */
	protected void setUp(final T pooledInitial) {}

	/**
	 * Request that the calculation be stopped at the next convenient place. If called the
	 * calculation will return {@link AbstractMutClassSizeTask#STOP}.
	 */
	@Override
	public final void requestStop() {
		log.debug("{} has been requested to stop", getClass().getSimpleName());
		mShouldRun = false;
	}

	/**
	 * Get the {@link Pool} which provides {@link QuiverMatrix} objects of type T.
	 * 
	 * @return Pool of quiver objects
	 */
	protected final Pool<T> getQuiverPool() {
		return Pools.getQuiverMatrixPool(getRows(), getCols(), getMatrixClass());
	}

	/**
	 * Get the {@link Pool} which provides {@link LinkHolder} objects which expect matrices of Type
	 * T.
	 * 
	 * @param size Number of links in each {@link LinkHolder}
	 * @return Pool of {@link LinkHolder} objects
	 */
	protected final Pool<LinkHolder<T>> getHolderPool(final int size) {
		return Pools.getHolderPool(size, getMatrixClass());
	}

	/**
	 * Convenience method to get the number of unfrozen vertices in the quiver.
	 * 
	 * @param matrix Matrix to get the size of
	 * @return Number of unfrozen vertices in the quiver
	 */
	private int getSize(final QuiverMatrix matrix) {
		return Math.min(matrix.getNumRows(), matrix.getNumCols());
	}

	/**
	 * Get the Type of {@link QuiverMatrix} which is being used.
	 * 
	 * @return the Type of {@link QuiverMatrix}
	 */
	@SuppressWarnings("unchecked")
	protected final Class<T> getMatrixClass() {
		return (Class<T>) mInitialMatrix.getClass();
	}

	/**
	 * Get the number of rows in the initial matrix.
	 * 
	 * @return Number of rows
	 */
	protected final int getRows() {
		return mInitialMatrix.getNumRows();
	}

	/**
	 * Get the number of columns in the initial matrix.
	 * 
	 * @return Number of columns
	 */
	protected final int getCols() {
		return mInitialMatrix.getNumCols();
	}

	/**
	 * Check whether the matrix has all its links full.
	 * 
	 * @param remove Matrix to check
	 * @param mMatrixSet Map containing the links
	 * @return true if the matrix has all links full
	 */
	protected final boolean isMatrixComplete(final T remove, final Map<T, LinkHolder<T>> mMatrixSet) {

		LinkHolder<T> holder = mMatrixSet.get(remove);
		return holder != null && holder.isComplete();
	}

	/**
	 * Remove a matrix from the map once it has been handled by the task. This should only be called
	 * once the quiver is handled, as it makes a lot of assumptions about the state concerning the
	 * matrix. Any other time
	 * {@link AbstractMutClassSizeTask#checkRemoveUnhandledQuiver(QuiverMatrix, Pool, Pool, Map, Queue, int)}
	 * should be used instead.
	 * 
	 * @param remove Handled matrix to remove
	 * @param quiverPool Pool to return matrix to
	 * @param holderPool Pool to return link holder to
	 * @param matrixSet Map to remove quiver from
	 */
	protected abstract void removeHandledQuiver(final T remove, final Pool<T> quiverPool,
			final Pool<LinkHolder<T>> holderPool, final Map<T, LinkHolder<T>> matrixSet);

	/**
	 * Remove a complete matrix from the map.
	 * 
	 * <p>
	 * Does not remove from the Queue, as the Queue is not backed by a hashtable, so the remove is
	 * slow.
	 * 
	 * @param remove Matrix to remove
	 * @param quiverPool Pool to return matrix to
	 * @param holderPool Pool to return holder to
	 * @param matrixSet Map to remove matrix from
	 */
	protected abstract void removeComplete(final T remove, final Pool<T> quiverPool,
			final Pool<LinkHolder<T>> holderPool, final Map<T, LinkHolder<T>> matrixSet);

	/**
	 * Return a quiver to the pool.
	 * 
	 * @param remove Matrix to return
	 * @param quiverPool Pool to return to
	 */
	protected final void returnMatrix(final T remove, final Pool<T> quiverPool) {
		quiverPool.returnObj(remove);
	}

	/**
	 * Get the {@link Map} which holds the {@link QuiverMatrix} objects which have been seen before
	 * and the {@link LinkHolder} associated to them.
	 * 
	 * @param size Size of the matrices which will be stored in the map
	 * @return Map between matrices and link holders
	 */
	protected abstract Map<T, LinkHolder<T>> getMatrixMap(int size);

	/**
	 * Handle a matrix which has been seen before in the mutation process.
	 * 
	 * @param matrixSet {@link Map} containing the matrices and which mutations from them have been
	 *        considered
	 * @param mat Matrix mutated to get the seen before matrix
	 * @param newMatrix Matrix which has been seen before
	 * @param i Index mutated at to get to newMatrix
	 */
	protected abstract void handleSeenMatrix(Map<T, LinkHolder<T>> matrixSet, T mat, T newMatrix,
			int i);

	/**
	 * Handle matrix not yet seen before in the mutation process.
	 * 
	 * @param matrixSet {@link Map} containing the matrices and which mutations from them have been
	 *        considered
	 * @param incompleteQuivers {@link Queue} containing quivers not yet handled
	 * @param holderPool {@link Pool} providing {@link LinkHolder} objects
	 * @param mat Matrix mutated to get the unseen matrix
	 * @param newMatrix Unseen matrix
	 * @param i Index mutated at to get unseen matrix
	 */
	protected abstract void handleUnseenMatrix(Map<T, LinkHolder<T>> matrixSet,
			Queue<T> incompleteQuivers, Pool<LinkHolder<T>> holderPool, T mat, T newMatrix, int i);

	/**
	 * Check whether the matrix has been seen before in the mutation process.
	 * 
	 * @param newMatrix Matrix to check
	 * @param matrixSet Map containing the seen matrices which are currently incomplete
	 * @return true if the matrix has been seen before
	 */
	protected abstract boolean matrixSeenBefore(T newMatrix, Map<T, LinkHolder<T>> matrixSet);

	/**
	 * Called after the calculation is complete. Can be used to clean up and return objects to their
	 * pools.
	 * 
	 * @param quiverPool Pool of quiver objects
	 * @param holderPool Pool of link holder objects
	 * @param matrixSet Map containing quivers and holders
	 */
	protected abstract void teardown(Pool<T> quiverPool, Pool<LinkHolder<T>> holderPool,
			Map<T, LinkHolder<T>> matrixSet);

	/**
	 * Check whether we need to mutate the vertex at the specified index, or whether that mutation
	 * has already been considered.
	 * 
	 * @param matrix Matrix to check
	 * @param i Index to mutate at
	 * @param matrixSet Map containing {@link LinkHolder} objects
	 * @return true if should mutate at the index
	 */
	private boolean shouldMutateAt(final T matrix, final int i,
			final Map<T, LinkHolder<T>> matrixSet) {
		LinkHolder<T> holder = matrixSet.get(matrix);
		return holder != null && !holder.hasLink(i);
	}

	/**
	 * Class which stores information on the progress of the {@link MutClassSizeTask} which is
	 * running.
	 * 
	 * <p>
	 * Listeners can be added with the {@link AbstractMutClassSizeTask#addListener(StatsListener)}
	 * method and the time between updates can be set using
	 * {@link AbstractMutClassSizeTask#setIterationsBetweenStats(int)}.
	 * 
	 * @author John Lawson
	 * 
	 */
	public class Stats {
		/** Number of matrices handled so far. */
		private int numConsidered;
		/** Number of matrices seen but not handled. */
		private int numInMap;
		/** Time the calculation started. */
		private long startTime;
		/** Time the last iteration was completed. */
		private long lastIteration;
		/** Number of iterations completed. */
		private int numIterations;

		/**
		 * Start the Stats recording.
		 */
		private void start() {
			startTime = System.nanoTime();
			lastIteration = startTime;
		}

		/**
		 * Register that one iteration has been completed.
		 */
		private void iterationComplete() {
			numIterations++;
			lastIteration = System.nanoTime() - lastIteration;
		}

		/**
		 * Update the Stats object with new information.
		 * 
		 * @param map Map containing unhandled matrices
		 * @param num Number of matrices handled so far
		 */
		private void update(final Map<T, LinkHolder<T>> map, final int num) {
			numConsidered = num;
			numInMap = map.size();
			for (StatsListener lis : mStatsListeners) {
				lis.statsUpdated(this);
			}
		}

		@Override
		public final String toString() {
			String.format("Handled %d matrices with %d found but not handled", numConsidered,
					numInMap);
			return String.format("Handled %d matrices with %d found but not handled",
					numConsidered, numInMap);
		}

		/**
		 * Get the time taken by the calculation so far in nanoseconds.
		 * 
		 * @return Time the calculation has taken up until now
		 */
		public final long getCalculationTime() {
			long now = System.nanoTime();
			return now - startTime;
		}

		/**
		 * Get the average time taken to handle each matrix since the calculation began.
		 * 
		 * @return The average iteration time
		 */
		public final long getAvgIterationTime() {
			long time = getCalculationTime();
			return time / numIterations;
		}

		/**
		 * Get the time taken for the very last iteration.
		 * 
		 * @return The time taken by the last iterations
		 */
		public final long getLastIterationTime() {
			return lastIteration;
		}

	}

	/** Listener to receive updates when the Stats object of this task is refreshed. */
	public interface StatsListener {
		/**
		 * Called when the stats object is updated to hold the most recent information.
		 * <p>
		 * The time between these calls can be set using the
		 * {@link AbstractMutClassSizeTask#setIterationsBetweenStats(int)} method.
		 * 
		 * @param stats Statistics object with methods for accessing the data
		 */
		void statsUpdated(AbstractMutClassSizeTask<?>.Stats stats);
	}

	/** Simple class which provides logging of the task statistics each time it is updated. */
	private class StatsLogger implements StatsListener {

		/**
		 * Called each time the Stats are updated.
		 * 
		 * @param stats The Stats object which has just been updated
		 */
		@Override
		public void statsUpdated(final AbstractMutClassSizeTask<?>.Stats stats) {
			log.debug(stats.toString());
		}
	}
}
