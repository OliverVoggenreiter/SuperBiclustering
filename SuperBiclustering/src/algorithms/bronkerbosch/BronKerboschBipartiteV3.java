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

public class BronKerboschBipartiteV3 extends BronKerboschBipartiteV2 {

	@Override
	protected void findBiclusters() {

		int[] vert =
				DegeneracyOrdering.orderGraph(inputMatrix, true);

		// row nodes have indices between [0, numberOfRows) in vert
		Nodes rowsData =
				new Nodes(NodeType.ROW, numRows, vert, 0, numRows);
		// col nodes have indices between [numberOfRows, numberOfRows
		// + numberOfColumns) in vert
		Nodes colsData =
				new Nodes(NodeType.COL, numCols, vert, numRows,
						numRows + numCols);

		bkv3(rowsData, colsData, vert);
	}

	/**
	 * Main call of the Bron-Kerbosch Algorithm (Version 3).
	 */
	private void bkv3(Nodes rowsData, Nodes colsData, int[] vert) {
		int idrow = 0;
		int idcol = 0;
		for (int v = 0; v < vert.length; ++v) {
			if (vert[v] < numRows) {
				extendSelection(rowsData, colsData, idrow);
				idrow++;
			} else {
				extendSelection(colsData, rowsData, idcol);
				idcol++;
			}
		}
	}

}
