/**
 * Copyright 2014 John Lawson
 * 
 * CheckFiniteTask.java is part of JCluster.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.jwlawson.jcluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import nf.fr.eraasoft.pool.ObjectPool;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Lawson
 * 
 */
public class CheckFiniteTask implements Callable<QuiverMatrix> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final QuiverMatrix mInitialMatrix;
	private final int mSize;

	public CheckFiniteTask(QuiverMatrix matrix) {
		mInitialMatrix = matrix;
		mSize = Math.min(matrix.getNumCols(), matrix.getNumRows());
	}

	public QuiverMatrix call() throws Exception {
		log.debug("CheckFiniteTask started");
		Graph<QuiverMatrix, MutationEdge> graph = new Multigraph<QuiverMatrix, MutationEdge>(
				MutationEdge.class);
		graph.addVertex(mInitialMatrix);
		boolean vertexAdded;
		log.debug("Graph at start: {}", graph);
		Collection<QuiverMatrix> matrices = new ArrayList<QuiverMatrix>(1000);
		ObjectPool<QuiverMatrix> quiverPool = Pools.getQuiverMatrixPool(mInitialMatrix.getNumRows(),
				mInitialMatrix.getNumCols());
		do {
			matrices.clear();
			vertexAdded = false;
			matrices.addAll(graph.vertexSet());
			for (QuiverMatrix mat : matrices) {
				for (int i = 0; i < mSize; i++) {
					if (shouldMutateAt(graph, mat, i)) {
						QuiverMatrix m = mat.mutate(i, quiverPool.getObj());
						if (graph.containsVertex(m)) {
							MutationEdge edge = new MutationEdge(i, m, mat);
							graph.addEdge(mat, m, edge);
						} else {
							vertexAdded = true;
							graph.addVertex(m);
							MutationEdge edge = new MutationEdge(i, m, mat);
							graph.addEdge(mat, m, edge);
						}
					}
				}
			}
		} while (vertexAdded);
		log.info("Graph complete: No. vertices: {}, No. edges: {}", graph.vertexSet().size(), graph
				.edgeSet().size());
		for (QuiverMatrix m : graph.vertexSet()) {
			if (m == mInitialMatrix) {
				continue;
			}
			quiverPool.returnObj(m);
		}
		return null;
	}

	private boolean shouldMutateAt(Graph<QuiverMatrix, MutationEdge> graph, final QuiverMatrix mat,
			int i) {
		Collection<MutationEdge> edges = graph.edgesOf(mat);
		for (MutationEdge e : edges) {
			if (i == e.getLabel()) {
				return false;
			}
		}
		return true;
	}

	private class MutationEdge extends DefaultEdge {
		private final QuiverMatrix mMat1;
		private final QuiverMatrix mMat2;
		private final int mMutationLabel;

		public MutationEdge(int label, QuiverMatrix m1, QuiverMatrix m2) {
			mMat1 = m1;
			mMat2 = m2;
			mMutationLabel = label;
		}

		public int getLabel() {
			return mMutationLabel;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj.getClass() != getClass()) {
				return false;
			}
			MutationEdge rhs = (MutationEdge) obj;
			boolean res1 = new EqualsBuilder().append(mMat1, rhs.mMat1).append(mMat2, rhs.mMat2)
					.append(mMutationLabel, rhs.mMutationLabel).isEquals();
			boolean res2 = new EqualsBuilder().append(mMat1, rhs.mMat2).append(mMat2, rhs.mMat1)
					.append(mMutationLabel, rhs.mMutationLabel).isEquals();
			return res1 || res2;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(37, 51).append(mMat1).append(mMat2).append(mMat2)
					.append(mMat1).append(mMutationLabel).toHashCode();
		}

		@Override
		public String toString() {
			return "" + mMutationLabel + " from " + mMat1 + " to " + mMat2;
		}
	}
}
