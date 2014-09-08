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

/**
 * The class is used for storing the indices that are computed in the
 * method selectPivot(..).
 * */
public class BKIndices {

	private final static int MAX_DISCONNECTIONS = 30000;

	/**
	 * Minimum number of disconnections that a node in X u P has. The
	 * corresponding node will be chosen as pivot. The disconnections
	 * are computed with respect to the nodes P.
	 * */
	int minDisconnections;
	/**
	 * The node that is first extended and then added to X. If the
	 * pivot is from P, the selectedNode is the pivot itself, and
	 * afterwards the selectedNode is initialized iteratively with
	 * all the nodes in P not connected with the pivot. If the pivot
	 * is from X, the selectedNode will be chosen only from the set
	 * of nodes in P not connected with the pivot.
	 * */
	int selectedNode;
	/**
	 * The node from X u P with the most connections with the
	 * candidate nodes. (equivalent with the node with the least
	 * discontinuities with the candidates nodes)
	 * */
	int pivot;
	/**
	 * The variable can have the value 0 or 1. If the value is 0, the
	 * pivot was chosen from the X. If the value is 1, the pivot was
	 * chosen from P and needs to be first added to be first extended
	 * and then added to set X.
	 */
	boolean pivotFromP;

	NodeType selType; // type of the selected node (ROW or COL)
	NodeType pivotType; // type of the pivot node (ROW or COL)

	BKIndices() {
		minDisconnections = MAX_DISCONNECTIONS;
		selectedNode = -1;
		pivot = -1;
		pivotFromP = false;
		selType = null;
		pivotType = null;
	}

}
