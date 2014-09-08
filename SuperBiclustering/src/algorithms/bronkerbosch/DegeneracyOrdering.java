/* SuperBiclustering - A biclustering algorithm designed to
 * handle sparse and noisy input.
 * Copyright (C) 2014 Oliver Voggenreiter
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package algorithms.bronkerbosch;

import java.util.ArrayList;
import java.util.List;

import datatype.matrix.BinaryMatrix;

/***
 * @see "An O(m) Algorithm for Cores Decomposition of Networks" 2002 Vladimir Batagelj, Matjaz Zaversnik
 */
public class DegeneracyOrdering {
	/**
	 * The method computes the degeneracy ordering of a bipartite graph.
	 */
	public static int[] orderGraph(BinaryMatrix connected, boolean isBipartite) {
		if (connected == null || connected.getNumRows() == 0 || connected.getNumColumns() == 0) {
			throw new IllegalArgumentException("Matrix is null or of size 0.");
		}
		// Allocate enough memory for rows and columns.
		int cntNodes = connected.getNumRows();
		if (isBipartite == true) {
			// if the graph is bipartite, the input matrix is not symmetric.
			cntNodes += connected.getNumColumns();
		}
		// Adjacency list
		int[] degree = new int[cntNodes];
		// Adjacency list of connections.
		List<List<Integer>> adjL = computeAdjList(connected, isBipartite, degree);

		int maxD = 0;
		for (int i = 0; i < cntNodes; ++i) {
			if (degree[i] > maxD)
				maxD = degree[i];
		}
		// The vertices will be sorted by their degree.
		int[] vert = new int[cntNodes];
		// Position of vertices in array vert.
		int[] pos = new int[cntNodes];

		// cores decomposition.
		computeDegeneracy(adjL, degree, vert, pos);
		maxD = 0;
		for (int i = 0; i < cntNodes; ++i) {
			if (degree[i] > maxD)
				maxD = degree[i];
		}

		return vert;
	}

	/***
	 * Returns the adjacency list of a graph and also computes the degree of each vertex.
	 */
	private static List<List<Integer>> computeAdjList(BinaryMatrix connected, boolean isBipartite, int[] degree) {
		List<List<Integer>> adjL = null;

		if (isBipartite == true) { // connected is not symmetric
			// Allocate enough memory for rows and columns.
			int cntRows = connected.getNumRows();
			int cntCols = connected.getNumColumns();
			// Adjacency list of connections.
			adjL = new ArrayList<List<Integer>>(cntRows + cntCols);

			// Allocate adjacency list of rows.
			for (int i = 0; i < cntRows; ++i) {
				degree[i] = 0;
				adjL.add(new ArrayList<Integer>());
			}
			// Allocate adjacency list of cols.
			// The index of col j is j + cntRows
			for (int j = 0; j < cntCols; ++j) {
				degree[j + cntRows] = 0;
				adjL.add(new ArrayList<Integer>());
			}
			// Complete adjacency list.
			for (int i = 0; i < cntRows; ++i) {
				for (int j = 0; j < cntCols; ++j) {
					if (connected.get(i, j)) {
						degree[i]++;
						degree[j + cntRows]++;
						adjL.get(i).add(j + cntRows);
						adjL.get(j + cntRows).add(i);
					}
				}
			}
		} else { // connected is symmetric
			int cntNodes = connected.getNumRows();
			// Adjacency list
			adjL = new ArrayList<List<Integer>>(cntNodes);
			// Degree list
			for (int i = 0; i < cntNodes; ++i) {
				adjL.add(new ArrayList<Integer>());
			}
			// complete adjacency list of rows.
			for (int i = 0; i < cntNodes; ++i) {
				for (int j = i + 1; j < cntNodes; ++j) {
					if (connected.get(i, j)) {
						degree[i]++;
						degree[j]++;
						adjL.get(i).add(j);
						adjL.get(j).add(i);
					}
				}
			}
		}
		return adjL;
	}

	private static void computeDegeneracy(List<List<Integer>> adjL, int[] degree, int[] vert, int[] pos) {
		// maximum degree of a vertex.
		int maxDegree = degree[0];
		for (int i : degree) {
			if (maxDegree < i)
				maxDegree = i;
		}
		int N = adjL.size();

		// stores for each degree the position of the first vertex of that
		// degree in array vert.
		int[] bin = new int[maxDegree + 1];

		// bins the degrees
		// bin[d] stores how many vertices have degree d
		for (int v = 0; v < N; ++v) {
			bin[degree[v]]++;
		}
		// Sort the vertices in increasing order of their degree in O(n) using counting sort.

		// bin is updated to hold the cumulative sum of bin (of the degrees count)
		int start = 1;
		for (int d = 0; d <= maxDegree; ++d) {
			int num = bin[d];
			bin[d] = start;
			start += num;
		}
		// The actual sorting of the array vert.
		for (int v = 0; v < N; ++v) {
			pos[v] = bin[degree[v]] - 1;
			vert[pos[v]] = v;
			bin[degree[v]]++;
		}

		// recover the starting positions of the bins.
		for (int d = maxDegree; d >= 1; d--) {
			bin[d] = bin[d - 1];
		}

		int du, pu, pw, w;
		bin[0] = 1;
		for (int i = 0; i < N; ++i) {
			int v = vert[i];
			for (int iter = 0; iter < adjL.get(v).size(); ++iter) {
				int u = adjL.get(v).get(iter);
				if (degree[u] > degree[v]) {
					du = degree[u];
					pu = pos[u];
					pw = bin[du] - 1;
					w = vert[pw];
					if (u != w) {
						pos[u] = pw;
						pos[w] = pu;
						vert[pu] = w;
						vert[pw] = u;
					}
					bin[du]++;
					degree[u]--;
				}
			}
		}
	}
}
