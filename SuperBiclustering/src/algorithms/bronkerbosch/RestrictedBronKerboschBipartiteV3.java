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

public class RestrictedBronKerboschBipartiteV3 extends
RestrictedBronKerboschBipartiteV2 {

	public RestrictedBronKerboschBipartiteV3(int maxLevel) {
		super(maxLevel);
	}

	@Override
	protected void findMaxCliques() {

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

		bkv3(rowsData, colsData, vert, 0);
	}

	/**
	 * Main recursive call of algorithm v3.
	 */
	private void bkv3(Nodes rowsData, Nodes colsData, int[] vert,
			int level) {
		int idrow = 0;
		int idcol = 0;
		for (int v = 0; v < vert.length; ++v) {
			if (vert[v] < numRows) {
				extendSelection(rowsData, colsData, idrow, level);
				idrow++;
			} else {
				extendSelection(colsData, rowsData, idcol, level);
				idcol++;
			}
		}
	}

}
