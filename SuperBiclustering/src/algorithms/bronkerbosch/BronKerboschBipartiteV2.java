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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import datatype.bicluster.Bicluster;
import datatype.bicluster.BitSetBicluster;
import datatype.matrix.BinaryMatrix;

/**
 * Bron-Kerbosch Algorithm (Version 2) adapted for bipartite graphs.
 */
public class BronKerboschBipartiteV2 implements
BronKerboschBipartite {

	private static final BitSet EMPTY_BITSET = new BitSet();

	// The number of rows and columns that are left in the
	// preprocessed input matrix
	protected int numRows;
	protected int numCols;

	// Preprocessed adjacency matrix
	protected BinaryMatrix inputMatrix;

	protected int minRows = 10; // minimum nbr of rows in a bicluster
	protected int minCols = 10; // minimum nbr of columns in a
	// bicluster

	// Mapping of the indices from the reduced input matrix to the
	// initial input matrix
	protected int[] rowMapId;
	protected int[] colMapId;

	// The variable keeps track of the number of row (column) nodes
	// currently in the set R
	protected int cntRowsInClique = 0;
	protected int cntColsInClique = 0;

	// The arrays grow dynamically and represent set R for rows and
	// respectively columns
	private List<Integer> compsubRows = new ArrayList<Integer>();
	private List<Integer> compsubCols = new ArrayList<Integer>();

	protected List<Bicluster> biclusters =
			new ArrayList<Bicluster>();

	// total count of the number of biclusters
	protected long numBiclusters = 0;

	// maximal count of biclusters to report
	private long maxBiclusters = Long.MAX_VALUE;

	/*
	 * Method for calling a biclustering of the input matrix with
	 * given minimum size restrictions on the number of rows and
	 * columns for each bicluster.
	 */
	@Override
	public List<Bicluster> findBiclusters(
			BinaryMatrix connectivityMatrix) {
		return findBiclusters(connectivityMatrix, EMPTY_BITSET,
				EMPTY_BITSET);
	}

	/*
	 * Method for calling a biclustering of the input matrix
	 * conditioned on the occurence of at least one row from
	 * 'requiredRows' and one column from 'requiredColumns' being in
	 * each bicluster.
	 */
	@Override
	public List<Bicluster> findBiclusters(
			BinaryMatrix connectivityMatrix, BitSet requiredRows,
			BitSet requiredCols) {
		boolean validInput =
				initialize(connectivityMatrix, requiredRows,
						requiredCols);
		if (validInput) {
			findBiclusters();
		}
		return biclusters;
	}

	private boolean initialize(BinaryMatrix connectivityMatrix,
			BitSet requiredRows, BitSet requiredCols) {

		if (connectivityMatrix == null) {
			throw new IllegalArgumentException(
					"connectivityMatrix must not be null");
		}
		if (minRows <= 0
				|| minRows > connectivityMatrix.getNumRows()) {
			throw new IllegalArgumentException(
					"invalid minRows value");
		}
		if (minCols <= 0
				|| minCols > connectivityMatrix.getNumColumns()) {
			throw new IllegalArgumentException(
					"invalid minCols value");
		}

		this.rowMapId = new int[connectivityMatrix.getNumRows()];
		this.colMapId = new int[connectivityMatrix.getNumColumns()];

		biclusters = new ArrayList<Bicluster>();
		numBiclusters = 0;

		this.inputMatrix =
				AdjacencyMatrixPreprocessor.reduce(
						connectivityMatrix, minRows, minCols,
						requiredRows, requiredCols, rowMapId,
						colMapId);

		if (inputMatrix != null) {
			numRows = inputMatrix.getNumRows();
			numCols = inputMatrix.getNumColumns();
		}
		return inputMatrix != null;
	}

	protected void findBiclusters() {
		Nodes rowsData = new Nodes(NodeType.ROW, numRows);
		Nodes colsData = new Nodes(NodeType.COL, numCols);
		bkv2(rowsData, colsData);
	}

	/**
	 * Main recursive call of algorithm. Prerequisite: there are
	 * candidate nodes in either rows or columns yet to be processed.
	 */
	private long bkv2(Nodes rows, Nodes columns) {

		// abort algorithm when maximal number of biclusters is
		// exceeded
		if (numBiclusters >= maxBiclusters) {
			return 0;
		}

		long biclustersFound = 0;
		Pivot pivot = new Pivot();

		if (columns.candidatesEnd > columns.notEnd
				&& rows.candidatesEnd > rows.notEnd) {
			// look if pivot can be row
			pivot.findPivot(rows, columns, inputMatrix);
			// look if pivot can be column
			pivot.findPivot(columns, rows, inputMatrix);
		} else if (columns.candidatesEnd <= columns.notEnd) {
			// if columns have no candidates, and rows have
			// candidates choose pivot from rows only
			pivot.findPivot(rows, columns, inputMatrix);
		} else if (rows.candidatesEnd <= rows.notEnd) {
			// if rows have no candidates, and columns have
			// candidates choose pivot from columns only
			pivot.findPivot(columns, rows, inputMatrix);
		}

		int selectedNode = pivot.selectedNode;
		int node;

		if (pivot.pivotFromP == true) {
			if (pivot.selType.equals(NodeType.ROW)) {
				biclustersFound +=
						extendSelection(rows, columns, selectedNode);
			} else {
				biclustersFound +=
						extendSelection(columns, rows, selectedNode);
			}
		}
		if (pivot.pivotType.equals(NodeType.ROW)) {
			for (node = pivot.minDisconnections; node >= 1; node--) {
				for (selectedNode = columns.notEnd; selectedNode < columns.candidatesEnd
						&& inputMatrix.get(pivot.pivot,
								columns.nodes[selectedNode]); selectedNode++) {
					;
				}
				biclustersFound +=
						extendSelection(columns, rows, selectedNode);
			}
		} else {
			for (node = pivot.minDisconnections; node >= 1; node--) {
				for (selectedNode = rows.notEnd; selectedNode < rows.candidatesEnd
						&& inputMatrix.get(rows.nodes[selectedNode],
								pivot.pivot); selectedNode++) {
					;
				}
				biclustersFound +=
						extendSelection(rows, columns, selectedNode);
			}
		}
		return biclustersFound;
	}

	/**
	 * The selected node is from the set selectedSet and it will be
	 * added to the X set. The X,P sets are updated for both rows and
	 * columns.
	 */
	protected long extendSelection(Nodes selectedSet,
			Nodes checkSet, int s) {
		long biclustersFound = 0;
		int p = selectedSet.nodes[s];
		selectedSet.nodes[s] = selectedSet.nodes[selectedSet.notEnd];
		int sel = selectedSet.nodes[selectedSet.notEnd] = p;

		// update the new indices
		boolean selectionIsRow = false;
		if (selectedSet.nodeType.equals(NodeType.ROW))
			selectionIsRow = true;

		int[] newCheck = new int[checkSet.candidatesEnd];
		int newNotEndCheck = 0;
		int newCandidateEndCheck = 0;

		// Update X and P for the checkSet
		switch (selectedSet.nodeType) {
		case ROW:
			// intersect X with neighbors of the selected node
			for (int i = 0; i < checkSet.notEnd; i++) {
				if (inputMatrix.get(sel, checkSet.nodes[i]))
					newCheck[newNotEndCheck++] = checkSet.nodes[i];
			}
			newCandidateEndCheck = newNotEndCheck;
			// intersect P with neighbors of the selected node
			for (int i = checkSet.notEnd; i < checkSet.candidatesEnd; i++) {
				if (inputMatrix.get(sel, checkSet.nodes[i]))
					newCheck[newCandidateEndCheck++] =
					checkSet.nodes[i];
			}
			break;
		case COL:
			// similar as above, but for columns
			for (int i = 0; i < checkSet.notEnd; i++) {
				if (inputMatrix.get(checkSet.nodes[i], sel))
					newCheck[newNotEndCheck++] = checkSet.nodes[i];
			}
			newCandidateEndCheck = newNotEndCheck;
			for (int i = checkSet.notEnd; i < checkSet.candidatesEnd; i++) {
				if (inputMatrix.get(checkSet.nodes[i], sel))
					newCheck[newCandidateEndCheck++] =
					checkSet.nodes[i];
			}
			break;
		default:
			break;
		}

		int newNotEndSelected = selectedSet.notEnd;
		int newCandidateEndSelected = 0;
		if (selectedSet.candidatesEnd > 0)
			newCandidateEndSelected = selectedSet.candidatesEnd - 1;

		boolean hasOtherNodes = false;
		int totRows = -1;
		int totCols = -1;

		switch (selectedSet.nodeType) {
		case ROW:
			compsubRows.add(sel);
			cntRowsInClique++;
			hasOtherNodes = cntColsInClique > 0;
			totRows =
					newCandidateEndSelected - newNotEndSelected
					+ cntRowsInClique;
			totCols =
					newCandidateEndCheck - newNotEndCheck
					+ cntColsInClique;
			break;
		case COL:
			compsubCols.add(sel);
			cntColsInClique++;
			hasOtherNodes = cntRowsInClique > 0;
			totRows =
					newCandidateEndCheck - newNotEndCheck
					+ cntRowsInClique;
			totCols =
					newCandidateEndSelected - newNotEndSelected
					+ cntColsInClique;
			break;
		default:
			break;
		}

		if (totRows >= minRows && totCols >= minCols) {
			if (((newCandidateEndSelected == 0 && newCandidateEndCheck == 0)
					|| (newCandidateEndSelected == 0 && newNotEndCheck == 0) || (newCandidateEndCheck == 0 && newNotEndSelected == 0))) {
				numBiclusters++;
				biclustersFound++;
				Bicluster bc = new BitSetBicluster();

				for (int i = 0; i < compsubRows.size(); ++i)
					bc.addRow(rowMapId[compsubRows.get(i)]);
				for (int i = 0; i < compsubCols.size(); ++i)
					bc.addColumn(colMapId[compsubCols.get(i)]);

				if (selectionIsRow) {
					// add all rows connected with the rest
					for (int i = newNotEndSelected + 1; i <= newCandidateEndSelected; ++i) {
						bc.addRow(rowMapId[selectedSet.nodes[i]]);
					}
					for (int i = newNotEndCheck; i < newCandidateEndCheck; ++i) {
						bc.addColumn(colMapId[newCheck[i]]);
					}
				} else {
					for (int i = newNotEndSelected + 1; i <= newCandidateEndSelected; ++i) {
						bc.addColumn(colMapId[selectedSet.nodes[i]]);
					}
					for (int i = newNotEndCheck; i < newCandidateEndCheck; ++i) {
						bc.addRow(rowMapId[newCheck[i]]);
					}
				}
				biclusters.add(bc);
			} else {
				boolean hasCandidatesPivot =
						(newNotEndSelected < newCandidateEndSelected);
				boolean hasCandidatesCheck =
						(newNotEndCheck < newCandidateEndCheck);

				if ((hasCandidatesPivot && hasCandidatesCheck)
						|| // we have candidates in rows and cols
						(hasCandidatesPivot && !hasCandidatesCheck
								&& hasOtherNodes && newNotEndSelected == 0)
								|| (hasCandidatesCheck
										&& !hasCandidatesPivot && newNotEndCheck == 0)) {

					int[] newSelected =
							new int[selectedSet.candidatesEnd];
					// Update X and P for the set which contains the
					// selected node.
					// The selected node is eliminated from X and P,
					// the rest of the elements are left unchanged.
					System.arraycopy(selectedSet.nodes, 0,
							newSelected, 0, selectedSet.notEnd);
					if (selectedSet.candidatesEnd != 0) {
						System.arraycopy(selectedSet.nodes,
								selectedSet.notEnd + 1, newSelected,
								selectedSet.notEnd,
								newCandidateEndSelected
								- newNotEndSelected);
					}

					Nodes newselected =
							new Nodes(selectedSet.nodeType,
									newSelected,
									newCandidateEndSelected,
									newNotEndSelected);
					Nodes newcheck =
							new Nodes(checkSet.nodeType, newCheck,
									newCandidateEndCheck,
									newNotEndCheck);

					if (selectedSet.nodeType.equals(NodeType.ROW)) {
						biclustersFound +=
								bkv2(newselected, newcheck);
					} else {
						biclustersFound +=
								bkv2(newcheck, newselected);
					}
				}
			}
		}

		switch (selectedSet.nodeType) {
		case ROW:
			compsubRows.remove(compsubRows.size() - 1);
			cntRowsInClique--;
			break;
		case COL:
			compsubCols.remove(compsubCols.size() - 1);
			cntColsInClique--;
			break;
		default:
			break;
		}

		selectedSet.notEnd++;
		return biclustersFound;
	}

	@Override
	public void setMinRows(int minRows) {
		this.minRows = minRows;
	}

	@Override
	public void setMinColumns(int minColumns) {
		this.minCols = minColumns;
	}

	@Override
	public void setMaxBiclusters(int maxBiclusters) {
		this.maxBiclusters = maxBiclusters;
	}

}
