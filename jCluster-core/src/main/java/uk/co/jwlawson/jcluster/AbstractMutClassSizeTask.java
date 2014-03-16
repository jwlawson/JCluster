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

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.jwlawson.jcluster.pool.Pool;

public abstract class AbstractMutClassSizeTask<T extends QuiverMatrix> implements MatrixTask<T> {

	/** Value returned when the calculation was stopped prematurely */
	public final static int STOP = Integer.MIN_VALUE;

	/** Value returned if the mutation class is found to be infinite */
	public final static int INFINITE = -1;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private T mInitialMatrix;
	private boolean mShouldRun;
	private int mIterationsBetweenStats;
	private final List<StatsListener> mStatsListeners;

	public AbstractMutClassSizeTask() {
		mStatsListeners = new ArrayList<AbstractMutClassSizeTask.StatsListener>();
		addListener(new StatsLogger());
	}

	public AbstractMutClassSizeTask(T matrix) {
		this();
		setMatrix(matrix);
	}

	public void setMatrix(T matrix) {
		mInitialMatrix = matrix;
	}

	public void reset() {
		mShouldRun = true;
	}

	/**
	 * Add a listener to changes to the Stats object associated to this task.
	 * 
	 * @param listener The StatsListener to add
	 * */
	public void addListener(StatsListener listener) {
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
	public void setIterationsBetweenStats(int number) {
		mIterationsBetweenStats = number;
	}

	/**
	 * Find the number of matrices in the mutation class of the initial matrix. If the matrix is
	 * mutation-infinite then -1 is returned.
	 * 
	 * @return The size of the mutation class, or -1 if infinite
	 * @throws Exception
	 */
	public MatrixInfo call() throws Exception {
		log.debug("MutClassSizeTask started for {}", mInitialMatrix);
		MatrixInfo result = new MatrixInfo(mInitialMatrix);
		result.setMutationClassSize(getMutationClassSize());
		return result;
	}

	private Integer getMutationClassSize() throws PoolException {
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
								removeIncomplete(newMatrix, quiverPool);
							}
						} else {
							if (newMatrix.isInfinite()) {
								return INFINITE;
							}
							handleUnseenMatrix(matrixSet, incompleteQuivers, holderPool, mat, newMatrix, i);
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
	protected void setUp(T pooledInitial) {}

	/**
	 * Request that the calculation be stopped at the next convenient place. If called the calculation
	 * will return {@link AbstractMutClassSizeTask#STOP}.
	 */
	public void requestStop() {
		log.debug("{} has been requested to stop", getClass().getSimpleName());
		mShouldRun = false;
	}

	/**
	 * Get the {@link ObjectPool} which provides {@link QuiverMatrix} objects of type T.
	 * 
	 * @return Pool of quiver objects
	 */
	protected Pool<T> getQuiverPool() {
		return Pools.getQuiverMatrixPool(getRows(), getCols(), getMatrixClass());
	}

	/**
	 * Get the {@link ObjectPool} which provides {@link LinkHolder} objects which expect matrices of
	 * Type T.
	 * 
	 * @param size Number of links in each {@link LinkHolder}
	 * @return Pool of {@link LinkHolder} objects
	 */
	protected Pool<LinkHolder<T>> getHolderPool(int size) {
		return Pools.getHolderPool(size, getMatrixClass());
	}

	/**
	 * Convenience method to get the number of unfrozen vertices in the quiver.
	 * 
	 * @param matrix Matrix to get the size of
	 * @return Number of unfrozen vertices in the quiver
	 */
	private int getSize(QuiverMatrix matrix) {
		return Math.min(matrix.getNumRows(), matrix.getNumCols());
	}

	/**
	 * Get the {@link Type} of {@link QuiverMatrix} which is being used.
	 * 
	 * @return the {@link Type} of {@link QuiverMatrix}
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> getMatrixClass() {
		return (Class<T>) mInitialMatrix.getClass();
	}

	protected int getRows() {
		return mInitialMatrix.getNumRows();
	}

	protected int getCols() {
		return mInitialMatrix.getNumCols();
	}

	/**
	 * Check whether the matrix has all its links full.
	 * 
	 * @param remove Matrix to check
	 * @param mMatrixSet Map containing the links
	 * @return true if the matrix has all links full
	 */
	protected boolean isMatrixComplete(T remove, Map<T, LinkHolder<T>> mMatrixSet) {

		LinkHolder<T> holder = mMatrixSet.get(remove);
		return holder != null && holder.isComplete();
	}

	/**
	 * Remove a matrix from the map once it has been handled by the task. This should only be called
	 * once the quiver is handled, as it makes a lot of assumptions about the state concerning the
	 * matrix. Any other time
	 * {@link AbstractMutClassSizeTask#checkRemoveUnhandledQuiver(QuiverMatrix, ObjectPool, ObjectPool, Map, Queue, int)}
	 * should be used instead.
	 * 
	 * @param remove Handled matrix to remove
	 * @param quiverPool Pool to return matrix to
	 * @param holderPool Pool to return link holder to
	 * @param matrixSet Map to remove quiver from
	 */
	protected void removeHandledQuiver(T remove, Pool<T> quiverPool, Pool<LinkHolder<T>> holderPool,
			Map<T, LinkHolder<T>> matrixSet) {

		LinkHolder<T> holder = matrixSet.remove(remove);
		holderPool.returnObj(holder);
		removeIncomplete(remove, quiverPool);
	}

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
	protected void removeComplete(T remove, Pool<T> quiverPool, Pool<LinkHolder<T>> holderPool,
			Map<T, LinkHolder<T>> matrixSet) {

		LinkHolder<T> holder = matrixSet.remove(remove);
		holderPool.returnObj(holder);
		removeIncomplete(remove, quiverPool);
	}

	protected void removeIncomplete(T remove, Pool<T> quiverPool) {
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
	 * @param holderPool {@link ObjectPool} providing {@link LinkHolder} objects
	 * @param mat Matrix mutated to get the unseen matrix
	 * @param newMatrix Unseen matrix
	 * @param i Index mutated at to get unseen matrix
	 * @throws PoolException Thrown if there is some problem taking objects from the
	 *         {@link LinkHolder} pool
	 */
	protected abstract void handleUnseenMatrix(Map<T, LinkHolder<T>> matrixSet,
			Queue<T> incompleteQuivers, Pool<LinkHolder<T>> holderPool, T mat, T newMatrix, int i)
			throws PoolException;

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
	 * Check whether we need to mutate the vertex at the specified index, or whether that mutation has
	 * already been considered.
	 * 
	 * @param matrix Matrix to check
	 * @param i Index to mutate at
	 * @param matrixSet Map containing {@link LinkHolder} objects
	 * @return true if should mutate at the index
	 */
	private boolean shouldMutateAt(T matrix, int i, Map<T, LinkHolder<T>> matrixSet) {
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
		int numConsidered;
		int numInMap;
		long startTime;
		long lastIteration;
		int numIterations;

		private void start() {
			startTime = System.nanoTime();
			lastIteration = startTime;
		}

		private void iterationComplete() {
			numIterations++;
			lastIteration = System.nanoTime() - lastIteration;
		}

		private void update(Map<T, LinkHolder<T>> map, int num) {
			numConsidered = num;
			numInMap = map.size();
			for (StatsListener lis : mStatsListeners) {
				lis.statsUpdated(this);
			}
		}

		@Override
		public String toString() {
			String.format("Handled %d matrices with %d found but not handled", numConsidered, numInMap);
			return String.format("Handled %d matrices with %d found but not handled", numConsidered,
					numInMap);
		}

		/** Get the time taken by the calculation so far in nanoseconds */
		public long getCalculationTime() {
			long now = System.nanoTime();
			return now - startTime;
		}

		/**
		 * Get the average time taken to handle each matrix since the calculation began.
		 * 
		 * @return The average iteration time
		 */
		public long getAvgIterationTime() {
			long time = getCalculationTime();
			return time / numIterations;
		}

		/**
		 * Get the time taken for the very last iteration
		 * 
		 * @return The time taken by the last iterations
		 */
		public long getLastIterationTime() {
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
		public void statsUpdated(AbstractMutClassSizeTask<?>.Stats stats);
	}

	/** Simple class which provides logging of the task statistics each time it is updated */
	private class StatsLogger implements StatsListener {

		public void statsUpdated(AbstractMutClassSizeTask<?>.Stats stats) {
			log.debug(stats.toString());
		}
	}
}
