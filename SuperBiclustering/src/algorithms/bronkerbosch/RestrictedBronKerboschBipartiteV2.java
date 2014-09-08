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
import java.util.BitSet;
import java.util.List;

import datatype.bicluster.Bicluster;
import datatype.bicluster.BitSetBicluster;
import datatype.matrix.BinaryMatrix;

/**
 * Bron-Kerbosch algorithm V2 adapted for bipartite graphs.
 */
public class RestrictedBronKerboschBipartiteV2 implements
BronKerboschBipartite {

	private static final BitSet EMPTY_BITSET = new BitSet();

	// The number of rows and columns that are left in the
	// preprocessed 2D matrix
	protected int numRows;
	protected int numCols;

	// Preprocessed adjacency matrix
	protected BinaryMatrix inputMatrix;

	protected int minRows; // minimum nbr of rows in a bicluster
	protected int minCols; // minimum nbr of cols in a bicluster

	// if false, only the number of biclusters is kept.
	private boolean saveBiclusters = false;

	// Mapping of the indices from the reduced 2D matrix to the
	// initial 2D matrix
	protected int[] rowMapId;
	protected int[] colMapId;

	// The variable keeps track of the number of row(cols) nodes
	// currently in the set R
	protected int cntRowsInClique = 0;
	protected int cntColsInClique = 0;

	// The arrays grow dynamically and represent set R for rows and
	// respectively columns.
	private List<Integer> compsubRows = new ArrayList<Integer>();
	private List<Integer> compsubCols = new ArrayList<Integer>();

	protected List<Bicluster> biclusters =
			new ArrayList<Bicluster>();
	protected long numBiclusters = 0; // total count of the number of
	// biclusters

	// maximal count of biclusters to report; actually a number
	// slightly smaller than maxBiclusters may be reported;
	// however the condition numBiclusters > maxBiclusters can be
	// used to determine that the algorithm was interrupted due to
	// the
	// maxBiclusters limit
	private long maxBiclusters;

	private int maxLevel;

	public RestrictedBronKerboschBipartiteV2(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	@Override
	public List<Bicluster> findBiclusters(
			BinaryMatrix connectivityMatrix) {
		return findBiclusters(connectivityMatrix, EMPTY_BITSET,
				EMPTY_BITSET);
	}

	@Override
	public List<Bicluster> findBiclusters(
			BinaryMatrix connectivityMatrix, BitSet requiredRows,
			BitSet requiredCols) {
		boolean validInput =
				init(connectivityMatrix, requiredRows, requiredCols);
		if (validInput) {
			findMaxCliques();
		}
		return biclusters;
	}

	private boolean init(BinaryMatrix connectivityMatrix,
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

	protected void findMaxCliques() {
		Nodes rowsData = new Nodes(NodeType.ROW, numRows);
		Nodes colsData = new Nodes(NodeType.COL, numCols);
		bkv2(rowsData, colsData, 0);
	}

	/**
	 * Main recursive call of algorithm. Prerequisite: there are more
	 * candidate nodes in rows or columns (not necessarily in both)
	 */
	private long bkv2(Nodes rowsData, Nodes colsData,
			int level) {

		// abort algorithm when maximal number of biclusters is
		// exceeded
		if (maxBiclusters > 0 && numBiclusters > maxBiclusters) {
			return 0;
		}

		long biclustersFound = 0;
		Pivot pivot = new Pivot();
		// minDisconnections can not be greater than
		// MAXDISCONNECTIONS
		// In constructor:
		/*
		 * bk.minDisconnections = MAXDISCONNECTIONS; // 30000
		 * bk.selectedNode = -1; bk.pivot = -1; bk.node = 0;
		 * bk.selType = null; bk.pivotType = null;
		 */

		if (colsData.candidatesEnd > colsData.notEnd
				&& rowsData.candidatesEnd > rowsData.notEnd) {
			// look if pivot can be row
			pivot.findPivot(rowsData, colsData, inputMatrix);
			// look if pivot can be column
			pivot.findPivot(colsData, rowsData, inputMatrix);
		} else if (colsData.candidatesEnd <= colsData.notEnd) {
			// if columns have no candidates, and rows have
			// candidates choose pivot from rows only
			pivot.findPivot(rowsData, colsData, inputMatrix);
		} else if (rowsData.candidatesEnd <= rowsData.notEnd) {
			pivot.findPivot(colsData, rowsData, inputMatrix);
		}

		int selectedNode = pivot.selectedNode;
		int node;

		if (pivot.pivotFromP == true) {
			if (pivot.selType.equals(NodeType.ROW)) {
				biclustersFound +=
						extendSelection(rowsData, colsData,
								selectedNode, level + 1);
			} else {
				biclustersFound +=
						extendSelection(colsData, rowsData,
								selectedNode, level + 1);
			}
		}
		if (pivot.pivotType.equals(NodeType.ROW)) {
			for (node = pivot.minDisconnections; node >= 1; node--) {
				for (selectedNode = colsData.notEnd; selectedNode < colsData.candidatesEnd
						&& inputMatrix.get(pivot.pivot,
								colsData.nodes[selectedNode]); selectedNode++) {
					;
				}
				biclustersFound +=
						extendSelection(colsData, rowsData,
								selectedNode, level + 1);
			}
		} else {
			for (node = pivot.minDisconnections; node >= 1; node--) {
				for (selectedNode = rowsData.notEnd; selectedNode < rowsData.candidatesEnd
						&& inputMatrix.get(
								rowsData.nodes[selectedNode],
								pivot.pivot); selectedNode++) {
					;
				}
				biclustersFound +=
						extendSelection(rowsData, colsData,
								selectedNode, level + 1);
			}
		}
		return biclustersFound;
	}

	/**
	 * This method is called only if we still have elements in
	 * setCheckDisconnections. Ouput: The method updates the
	 * BKIndices structure which contains bk.minnod = minimum number
	 * of disconnections bk.selectedNode = index of the selected
	 * node; bk.pivot = index of the selected node; bk.node = 0 or 1;
	 * The node is 0 if the pivot is from X, or 1 otherwise.
	 */
	protected void selectPivot(Nodes setPossiblePivots,
			Nodes setCheckDisconnections, Pivot pivot) {
		int count;
		int pos = 0;
		int p;
		boolean pivotIsRow = false;
		if (setPossiblePivots.nodeType.equals(NodeType.ROW)) {
			pivotIsRow = true;
		}
		// iterate over X u P in rows
		for (int i = 0; i < setPossiblePivots.candidatesEnd
				&& pivot.minDisconnections != 0; i++) {
			p = setPossiblePivots.nodes[i];
			count = 0;
			// find the disconections in P cols
			for (int j = setCheckDisconnections.notEnd; j < setCheckDisconnections.candidatesEnd
					&& count < pivot.minDisconnections; j++) {
				// if it is a row
				if (pivotIsRow) {
					if (inputMatrix.get(p,
							setCheckDisconnections.nodes[j]) == false) {
						count++;
						pos = j;
					}
				} else {
					if (inputMatrix.get(
							setCheckDisconnections.nodes[j], p) == false) {
						count++;
						pos = j;
					}
				}
			}
			if (count < pivot.minDisconnections) {
				pivot.pivot = p;
				pivot.minDisconnections = count;
				pivot.pivotType = setPossiblePivots.nodeType;
				if (i < setPossiblePivots.notEnd) {
					pivot.selectedNode = pos;
					pivot.selType = setCheckDisconnections.nodeType;
					pivot.pivotFromP = false;
				} else {
					pivot.selectedNode = i;
					pivot.pivotFromP = true;
					pivot.selType = setPossiblePivots.nodeType;
					;
				}
			}
		}
	}

	/**
	 * The selected node is from the set selectedSet and it will be
	 * added to the X set. The X,P sets are updated for both rows and
	 * columns.
	 */
	protected long extendSelection(Nodes selectedSet,
			Nodes checkSet, int s, int level) {
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
			if (saveBiclusters)
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
			if (saveBiclusters)
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
			if (((level > maxLevel)
					|| (newCandidateEndSelected == 0 && newCandidateEndCheck == 0)
					|| (newCandidateEndSelected == 0 && newNotEndCheck == 0) || (newCandidateEndCheck == 0 && newNotEndSelected == 0))) {
				numBiclusters++;
				biclustersFound++;
				if (saveBiclusters) {
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
				}
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
							new Nodes(checkSet.nodeType,
									newCheck, newCandidateEndCheck,
									newNotEndCheck);

					if (selectedSet.nodeType.equals(NodeType.ROW)) {
						biclustersFound +=
								bkv2(newselected, newcheck,
										level + 1);
					} else {
						biclustersFound +=
								bkv2(newcheck, newselected,
										level + 1);
					}
				}
			}
		}

		switch (selectedSet.nodeType) {
		case ROW:
			if (saveBiclusters)
				compsubRows.remove(compsubRows.size() - 1);
			cntRowsInClique--;
			break;
		case COL:
			if (saveBiclusters)
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
