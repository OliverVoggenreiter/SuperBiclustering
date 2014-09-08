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

/**
 * Class for storing the set of nodes in X u P (corresponding to rows
 * or to columns).
 **/
public class Nodes implements Cloneable {

	public NodeType nodeType;
	/**
	 * The nodes in X u P are stored in an array. [0,...,notEnd_-1]
	 * are the nodes in X, [notEnd_,...,candidatesEnd_-1] are the
	 * nodes in P.
	 */
	public int[] nodes;
	public int notEnd;
	public int candidatesEnd;

	public Nodes(NodeType nodeType, int[] nodes,
			int candidateEndPivot, int notEndPivot) {
		this.nodeType = nodeType;
		this.nodes = nodes;
		this.notEnd = notEndPivot;
		this.candidatesEnd = candidateEndPivot;
	}

	/**
	 * Constructor which is called only in the beginning, before the
	 * first call to bkv2(..)
	 * */
	public Nodes(NodeType nodeType, int numNodes) {
		this.nodeType = nodeType;
		this.notEnd = 0;
		this.candidatesEnd = numNodes;

		nodes = new int[numNodes];
		for (int n = 0; n < numNodes; ++n) {
			nodes[n] = n;
		}
	}

	/**
	 * Constructor which is called only in the beginning, before the
	 * first call to bkv3(..)
	 */
	public Nodes(NodeType nodeType, int numNodes, int[] vert,
			int first, int last) {
		this.nodeType = nodeType;
		this.notEnd = 0;
		this.candidatesEnd = numNodes;

		nodes = new int[numNodes];
		int id = 0;
		for (int n = 0; n < vert.length; ++n) {
			if (first <= vert[n] && vert[n] < last) {
				nodes[id] = vert[n] - first;
				id++;
			}
		}
	}

	Nodes(NodeType nodeType, int numNodes, BitSet consideredNodes) {
		this.nodeType = nodeType;
		this.notEnd = 0;
		this.candidatesEnd = numNodes;

		nodes = new int[numNodes];
		int id = 0;
		for (int n = consideredNodes.nextSetBit(0); n > -1; n =
				consideredNodes.nextSetBit(n + 1)) {
			nodes[id] = n;
			id++;
		}
	}

	@Override
	public Nodes clone() {
		return new Nodes(nodeType, nodes.clone(), candidatesEnd,
				notEnd);
	}

}
