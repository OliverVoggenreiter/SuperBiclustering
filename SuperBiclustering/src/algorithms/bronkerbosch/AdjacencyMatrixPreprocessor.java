/* Bimax 2 - A biclustering algorithm based on the Bron-Kerbosch
 * Maximal Clique Enumeration Algorithm.
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

import java.util.BitSet;

import datatype.matrix.BinaryMatrix;
import datatype.matrix.BitSetBinaryMatrix;

public class AdjacencyMatrixPreprocessor {
	/**
	 * The method takes as input the connectivity matrix of a
	 * bipartite graph and returns the binary matrix with the rows
	 * that don t have more than minR 1's removed. Similarly, the
	 * cols that don t have more than minC 1's are removed. The
	 * arrays rowMapId and colMapId hold a mapping of the new
	 * indices(in matrix out) to the old indices (in matrix adj)
	 */
	public static BinaryMatrix reduce(BinaryMatrix adj, int minRows,
			int minColumns, int[] rowMapId, int[] colMapId) {
		// No special rows or columns are required in the output
		// matrix, so the bitsets contain only 0s.
		BitSet reqRows = new BitSet(adj.getNumRows());
		BitSet reqCols = new BitSet(adj.getNumColumns());

		return reduceRequired(adj, minRows, minColumns, reqRows,
				reqCols, rowMapId, colMapId);
	}

	/**
	 * Same as above, but the output matrix must contain only the
	 * rows and columns that have 1s within the corresponding mask
	 * (given by the bitsets reqRows, reqCols)
	 */
	public static BinaryMatrix reduce(BinaryMatrix adj, int minR,
			int minC, BitSet reqRows, BitSet reqCols,
			int[] rowMapId, int[] colMapId) {

		return reduceRequired(adj, minR, minC, reqRows, reqCols,
				rowMapId, colMapId);
	}

	/**
	 * The actual implementation of the reduce method (for bipartite
	 * matrices).
	 */
	private static BinaryMatrix reduceRequired(BinaryMatrix adj,
			int minRows, int minColumns, BitSet requiredRows,
			BitSet requiredColumns, int[] rowMapId, int[] colMapId) {

		int numRows = adj.getNumRows();
		int numCols = adj.getNumColumns();

		BitSet rows = new BitSet(numRows);
		rows.set(0, numRows);
		BitSet cols = new BitSet(numCols);
		cols.set(0, numCols);
		// Is true if columns were marked for removal.
		boolean colChanged = true;
		while (colChanged) {
			for (int r = rows.nextSetBit(0); r > -1; r =
					rows.nextSetBit(r + 1)) {
				// test if the row r meets the requirements (it
				// contains at least minC columns and also the
				// required columns)
				if (removeNode(adj, minColumns, requiredColumns, r,
						NodeType.ROW, cols) == true) {
					// mark r to be deleted
					rows.set(r, false);
				}
			}
			colChanged = false;
			for (int c = cols.nextSetBit(0); c > -1; c =
					cols.nextSetBit(c + 1)) {
				// test if column c meets the requirements
				if (removeNode(adj, minRows, requiredRows, c,
						NodeType.COL, rows) == true) {
					// mark c to be deleted
					cols.set(c, false);
					colChanged = true;
				}
			}
		}

		// rows or cols were marked for deletion
		if (rows.cardinality() != numRows
				|| cols.cardinality() != numCols) {
			if (rows.cardinality() == 0 || cols.cardinality() == 0) {
				return null;
			}

			BinaryMatrix out =
					new BitSetBinaryMatrix(rows.cardinality(), cols
							.cardinality());
			removeRowsAndCols(adj, rows, cols, out);
			updateMapIds(rows, numRows, rowMapId);
			updateMapIds(cols, numCols, colMapId);
			return out;
		}
		// There were no rows or columns deleted, so the map is
		// identity.
		for (int r = 0; r < numRows; ++r) {
			rowMapId[r] = r;
		}
		for (int c = 0; c < numCols; ++c) {
			colMapId[c] = c;
		}
		return adj;
	}

	/**
	 * The method updates the nodesMapId.
	 */
	private static void updateMapIds(BitSet nodes, int dimension,
			int[] nodesMapId) {
		int idTrue = 0;
		int idFalse = nodes.cardinality();
		for (int i = 0; i < dimension; ++i) {
			if (nodes.get(i) == true) {
				nodesMapId[idTrue] = i;
				idTrue++;
			} else {
				nodesMapId[idFalse] = i;
				idFalse++;
			}
		}
	}

	/**
	 * The method returns true if the node nodeId must be deleted if
	 * it does not meet the requirements.
	 */
	private static boolean removeNode(BinaryMatrix adj, int minOnes,
			BitSet requiredNodes, int nodeId, NodeType nt,
			BitSet consideredNodes) {
		for (int i = requiredNodes.nextSetBit(0); i > -1; i =
				requiredNodes.nextSetBit(i + 1)) {
			if ((nt == NodeType.ROW && adj.get(nodeId, i) == false)
					|| !consideredNodes.get(i)
					|| (nt == NodeType.COL && adj.get(i, nodeId) == false)) {
				return true;
			}
		}
		int cntOnes = 0;
		for (int i = consideredNodes.nextSetBit(0); i > -1; i =
				consideredNodes.nextSetBit(i + 1)) {
			if ((nt == NodeType.ROW && adj.get(nodeId, i) == true)
					|| (nt == NodeType.COL && adj.get(i, nodeId) == true)) {
				cntOnes++;
			}
		}
		if (cntOnes < minOnes)
			return true;
		return false;
	}

	/***
	 * The matrix out contains only the rows and columns of matrix
	 * adj that are set to true in the corresponding bitset.
	 */
	private static void removeRowsAndCols(BinaryMatrix adj,
			BitSet rows, BitSet cols, BinaryMatrix out) {
		int idr = 0;
		int idc = 0;
		for (int r = rows.nextSetBit(0); r > -1; r =
				rows.nextSetBit(r + 1)) {
			idc = 0;
			for (int c = cols.nextSetBit(0); c > -1; c =
					cols.nextSetBit(c + 1)) {
				out.set(idr, idc, adj.get(r, c));
				idc++;
			}
			idr++;
		}
	}

	/**
	 * This method takes as input the connectivity matrix of a normal
	 * graph and returns the binary matrix with the nodes that don t
	 * have more than minNodes 1's removed. The array nodeMapId holds
	 * a mapping of the new indices(in matrix out) to the old indices
	 * (in matrix adj).
	 */
	public static BinaryMatrix reduce(BinaryMatrix adj,
			int minNodes, int[] nodeMapId) {
		// No special rows or columns are required in the output
		// matrix, so the bitsets contain only 0s.
		BitSet reqNodes = new BitSet(adj.getNumRows());

		return reduceRequired(adj, minNodes, reqNodes, nodeMapId);
	}

	/**
	 * This method takes as input the connectivity matrix of a normal
	 * graph and a required set of nodes that must be contained in
	 * the graph and returns the binary matrix with the nodes that
	 * don t have more than minNodes 1's removed. The array nodeMapId
	 * holds a mapping of the new indices(in matrix out) to the old
	 * indices (in matrix adj).
	 */
	public static BinaryMatrix reduce(BinaryMatrix adj,
			int minNodes, BitSet reqNodes, int[] nodeMapId) {

		return reduceRequired(adj, minNodes, reqNodes, nodeMapId);
	}

	/**
	 * The reduce method for normal matrices.
	 */
	private static BinaryMatrix reduceRequired(BinaryMatrix adj,
			int minNodes, BitSet requiredNodes, int[] nodeMapId) {

		int numNodes = adj.getNumRows();

		BitSet nodesToKeep = new BitSet(numNodes);
		nodesToKeep.set(0, numNodes);

		for (int i = 0; i < numNodes; i++) {
			int count = 0;
			for (int j = 0; j < numNodes; j++) {
				if (adj.get(i, j)) {
					count++;
				}
			}
			if (count < minNodes && !requiredNodes.get(i)) {
				nodesToKeep.clear(i);
			}
		}

		if (nodesToKeep.cardinality() < numNodes) {
			if (nodesToKeep.cardinality() == 0) {
				return null;
			}
			BinaryMatrix out =
					new BitSetBinaryMatrix(
							nodesToKeep.cardinality(), nodesToKeep
							.cardinality());
			int currentRow = 0;
			for (int i = nodesToKeep.nextSetBit(0); i >= 0; i =
					nodesToKeep.nextSetBit(i + 1)) {
				int currentColumn = 0;
				for (int j = nodesToKeep.nextSetBit(0); j >= 0; j =
						nodesToKeep.nextSetBit(j + 1)) {
					out.set(currentRow, currentColumn, adj.get(i, j));
					currentColumn++;
				}
				currentRow++;
			}
			updateMapIds(nodesToKeep, numNodes, nodeMapId);
			return out;
		}

		for (int n = 0; n < numNodes; n++) {
			nodeMapId[n] = n;
		}

		return adj;
	}

}
