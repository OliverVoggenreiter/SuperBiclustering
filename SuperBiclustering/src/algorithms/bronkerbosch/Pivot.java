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

import datatype.matrix.BinaryMatrix;

/**
 * The class is used for storing the indices that are computed in the
 * method selectPivot(..).
 * */
public class Pivot {

	private final static int MAX_DISCONNECTIONS = 30000;

	/**
	 * Minimum number of disconnections that a node in X u P has. The
	 * corresponding node will be chosen as pivot. The disconnections
	 * are computed with respect to the nodes P.
	 * */
	public int minDisconnections = MAX_DISCONNECTIONS;

	/**
	 * The node that is first extended and then added to X. If the
	 * pivot is from P, the selectedNode is the pivot itself, and
	 * afterwards the selectedNode is initialized iteratively with
	 * all the nodes in P not connected with the pivot. If the pivot
	 * is from X, the selectedNode will be chosen only from the set
	 * of nodes in P not connected with the pivot.
	 * */
	public int selectedNode = -1;

	/**
	 * The node from X u P with the most connections with the
	 * candidate nodes. (equivalent with the node with the least
	 * discontinuities with the candidates nodes)
	 * */
	public int pivot = -1;

	/**
	 * The variable can have the value 0 or 1. If the value is 0, the
	 * pivot was chosen from the X. If the value is 1, the pivot was
	 * chosen from P and needs to be first added to be first extended
	 * and then added to set X.
	 */
	public boolean pivotFromP = false;

	// type of the selected node (ROW or COL)
	public NodeType selType;
	// type of the pivot node (ROW or COL)
	public NodeType pivotType;

	/**
	 * This method is called only if we still have elements in
	 * setCheckDisconnections. Ouput: The method updates the
	 * BKIndices structure which contains bk.minnod = minimum number
	 * of disconnections bk.selectedNode = index of the selected
	 * node; bk.pivot = index of the selected node; bk.node = 0 or 1;
	 * The node is 0 if the pivot is from X, or 1 otherwise.
	 */
	public void findPivot(Nodes setPossiblePivots,
			Nodes setCheckDisconnections,
			BinaryMatrix preprocessedMatrix) {
		int count;
		int pos = 0;
		int p;
		boolean pivotIsRow = false;
		if (setPossiblePivots.nodeType.equals(NodeType.ROW)) {
			pivotIsRow = true;
		}
		// iterate over X u P in rows
		for (int i = 0; i < setPossiblePivots.candidatesEnd
				&& minDisconnections != 0; i++) {
			p = setPossiblePivots.nodes[i];
			count = 0;
			// find the disconections in P cols
			for (int j = setCheckDisconnections.notEnd; j < setCheckDisconnections.candidatesEnd
					&& count < minDisconnections; j++) {
				// if it is a row
				if (pivotIsRow) {
					if (preprocessedMatrix.get(p,
							setCheckDisconnections.nodes[j]) == false) {
						count++;
						pos = j;
					}
				} else {
					if (preprocessedMatrix.get(
							setCheckDisconnections.nodes[j], p) == false) {
						count++;
						pos = j;
					}
				}
			}
			if (count < minDisconnections) {
				pivot = p;
				minDisconnections = count;
				pivotType = setPossiblePivots.nodeType;
				if (i < setPossiblePivots.notEnd) {
					// the selected node is from the checkSet (e.g
					// first non neighbor)
					selectedNode = pos;
					selType = setCheckDisconnections.nodeType;
					pivotFromP = false;
				} else {
					// the selected node is from the pivot set
					selectedNode = i;
					pivotFromP = true;
					selType = setPossiblePivots.nodeType;
					;
				}
			}
		}
	}

}
