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

package algorithms.generators;

import java.util.ArrayList;
import java.util.List;

import datatype.bicluster.Bicluster;
import datatype.bicluster.BinaryVector;
import datatype.bicluster.BitSetBicluster;
import datatype.bicluster.BitSetBinaryVector;
import datatype.matrix.BinaryMatrix;

/**
 * This alternate form of the MatrixDegeneracy forgoes calculating the degeneracy order and instead creates biclusters based on the minimum
 * threshold given. This threshold defines how many ones must exist in each column and row of a bicluster at minimum. In order to prevent
 * the creation of too many incorrect biclusters, we can use the degeneracy to quickly remove irrelevant nodes as they show up during
 * execution.
 *
 * @author "Oliver Voggenreiter"
 * @date Feb 25, 2013
 *
 */
public class RestrictiveMatrixDegeneracy {

	/**
	 * Performs the combined degeneracy calculation and row/column removal in order to produce blocks that contain all the ones that take
	 * part in biclusters of size at least the minSize.
	 *
	 */
	public static List<Bicluster> computeDegenerateBlocks(BinaryMatrix matrix, int minSize) {
		int[] counts = MatrixDegeneracy.computeNodeDegrees(matrix);
		List<Bicluster> biclusters = computeBlocks(counts, matrix, minSize);

		return biclusters;
	}

	private static List<Bicluster> computeBlocks(int[] connectivityValues, BinaryMatrix matrix, int minThreshold) {
		List<Bicluster> biclusters = new ArrayList<Bicluster>();
		int[] connectivity = connectivityValues.clone();
		BinaryVector rowsLeft = new BitSetBinaryVector(matrix.getNumRows(), true);
		BinaryVector columnsLeft = new BitSetBinaryVector(matrix.getNumColumns(), true);
		List<List<Integer>> degeneracyGroups = MatrixDegeneracy.computeDegeneracyGroups(connectivity);

		while (rowsLeft.cardinality() >= minThreshold && columnsLeft.cardinality() >= minThreshold) {
			int firstNonEmptyGroup = MatrixDegeneracy.computeIndexOfFirstNonEmptyList(degeneracyGroups);

			int chosenPosition = MatrixDegeneracy.chooseRandomNodePositionInGroup(firstNonEmptyGroup, degeneracyGroups);
			int chosenNode = degeneracyGroups.get(firstNonEmptyGroup).remove(chosenPosition);
			connectivity[chosenNode] = 0;

			if (chosenNode < matrix.getNumRows()) {
				// Choice is a Row!
				rowsLeft.set(chosenNode, false);
				if (firstNonEmptyGroup >= minThreshold) {
					Bicluster bicluster = getDegenerateBicluster(chosenNode, rowsLeft, columnsLeft, matrix, minThreshold);
					if (bicluster.getNumberOfRows() >= minThreshold)
						biclusters.add(bicluster);
				}
				updateColumnNeighbours(chosenNode, degeneracyGroups, connectivity, columnsLeft, matrix);
			} else {
				// Choice is Column!
				columnsLeft.set(chosenNode - matrix.getNumRows(), false);
				if (firstNonEmptyGroup >= minThreshold) {
					Bicluster bicluster = getDegenerateBicluster(chosenNode, rowsLeft, columnsLeft, matrix, minThreshold);
					if (bicluster.getNumberOfColumns() >= minThreshold)
						biclusters.add(bicluster);
				}
				updateRowNeighbours(chosenNode - matrix.getNumRows(), degeneracyGroups, connectivity, rowsLeft, matrix);
			}

		}

		return biclusters;
	}

	private static Bicluster getDegenerateBicluster(int chosenNode, BinaryVector rowsLeft, BinaryVector columnsLeft, BinaryMatrix matrix,
			int minThreshold) {
		Bicluster bicluster = new BitSetBicluster();

		if (chosenNode < matrix.getNumRows()) {
			bicluster.addRow(chosenNode);
			for (int column : columnsLeft) {
				if (matrix.get(chosenNode, column))
					bicluster.addColumn(column);
			}
			for (int row : rowsLeft) {
				int columnCount = 0;
				for (int column : bicluster.getColumns()) {
					if (matrix.get(row, column)) {
						columnCount++;
						if (columnCount >= minThreshold) {
							//						if ((float) columnCount / bicluster.getNumberOfColumns() >= 0.4f) {//minThreshold) {
							bicluster.addRow(row);
							break;
						}
					}
				}
			}
		} else {
			bicluster.addColumn(chosenNode - matrix.getNumRows());
			for (int row : rowsLeft) {
				if (matrix.get(row, chosenNode - matrix.getNumRows()))
					bicluster.addRow(row);
			}
			for (int column : columnsLeft) {
				int rowCount = 0;
				for (int row : bicluster.getRows()) {
					if (matrix.get(row, column)) {
						rowCount++;
						if (rowCount >= minThreshold) {
							//						if ((float) rowCount / bicluster.getNumberOfRows() >= 0.4f){ //minThreshold) {
							bicluster.addColumn(column);
							break;
						}
					}
				}
			}
		}

		return bicluster;
	}

	private static void updateColumnNeighbours(int row, List<List<Integer>> degeneracyGroups, int[] connectivityValues,
			BinaryVector columns, BinaryMatrix matrix) {
		for (int column : columns) {
			if (matrix.get(row, column)) {
				int connectedColumn = matrix.getNumRows() + column;
				int connectedColumnPosition = degeneracyGroups.get(connectivityValues[connectedColumn]).indexOf(connectedColumn);
				degeneracyGroups.get(connectivityValues[connectedColumn]).remove(connectedColumnPosition);
				connectivityValues[connectedColumn]--;
				degeneracyGroups.get(connectivityValues[connectedColumn]).add(connectedColumn);
			}
		}
	}

	private static void updateRowNeighbours(int column, List<List<Integer>> degeneracyGroups, int[] connectivity, BinaryVector rows,
			BinaryMatrix matrix) {
		for (int row : rows) {
			if (matrix.get(row, column)) {
				int connectedRowPosition = degeneracyGroups.get(connectivity[row]).indexOf(row);
				degeneracyGroups.get(connectivity[row]).remove(connectedRowPosition);
				connectivity[row]--;
				degeneracyGroups.get(connectivity[row]).add(row);
			}
		}
	}
}
