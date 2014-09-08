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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import map.KeyValuePair;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class Degeneracy {

	private static final Random rand = new Random(999);

	public static List<Bicluster> computeBicluster(
			int minDegeneracy, BinaryMatrix matrix) {
		MatrixStats stats = computeNodeDegree(matrix);
		stats = computeNodeOrder(stats);
		List<KeyValuePair<Integer, Integer>> biclustersToBuild =
				new ArrayList<KeyValuePair<Integer, Integer>>();
		while (stats.enoughLeft(minDegeneracy)) {
			if (stats.nextLowestIsRow()) {
				if (stats.chooseRow(matrix) > minDegeneracy)
					biclustersToBuild
					.add(new KeyValuePair<Integer, Integer>(
							stats.rowsLeft,
							stats.columnsLeft));
			} else {
				if (stats.choosecolumn(matrix) > minDegeneracy)
					biclustersToBuild
					.add(new KeyValuePair<Integer, Integer>(
							stats.rowsLeft,
							stats.columnsLeft));
			}
		}

		return new ArrayList<Bicluster>();
	}

	public static MatrixStats computeDegeneracyOrdering(
			BinaryMatrix matrix) {
		return computeDegeneracyOrdering(1, matrix);
	}

	public static MatrixStats computeDegeneracyOrdering(
			int minDegeneracy, BinaryMatrix matrix) {
		MatrixStats stats = computeNodeDegree(matrix);
		stats = computeNodeOrder(stats);

		while (stats.enoughLeft(minDegeneracy)) {
			if (stats.nextLowestIsRow()) {
				stats.chooseRow(matrix);
			} else {
				stats.choosecolumn(matrix);
			}
		}
		return stats;
	}

	public static class MatrixStats {
		private int rowsLeft;
		private int columnsLeft;
		private int[] rowsD;
		private int[] columnsD;
		private Integer[] rows;
		private Integer[] columns;
		private int[] rowDegreeIndices;
		private int[] columnDegreeIndices;

		private int chooseRow(BinaryMatrix matrix) {
			int lowestRow = rows[rowDegreeIndices[0]];
			for (int i = rowsD[lowestRow] - 1; i >= 0; i--) {
				rowDegreeIndices[i]++;
			}
			rowsLeft--;
			for (int i = columnDegreeIndices[0]; i < columns.length; i++) {
				if (matrix.get(lowestRow, columns[i])) {
					switchColumn(i);
				}
			}
			return rowsD[lowestRow];
		}

		private int choosecolumn(BinaryMatrix matrix) {
			int lowestColumn = columns[columnDegreeIndices[0]];
			for (int i = columnsD[lowestColumn] - 1; i >= 0; i--) {
				columnDegreeIndices[i]++;
			}
			columnsLeft--;
			for (int i = rowDegreeIndices[0]; i < rows.length; i++) {
				if (matrix.get(i, lowestColumn)) {
					switchRow(i);
				}
			}
			return columnsD[lowestColumn];
		}

		private void switchColumn(int index) {
			int degree = columnsD[columns[index]];
			if (degree == 1)
				rowsLeft--;
			if (index < columnDegreeIndices[degree])
				throw new IllegalArgumentException();
			int temp = columns[columnDegreeIndices[degree - 1]];
			columns[columnDegreeIndices[degree - 1]] =
					columns[index];
			columns[index] = temp;
			columnDegreeIndices[--columnsD[temp]]++;
		}

		private void switchRow(int index) {
			int degree = rowsD[rows[index]];
			if (degree == 1)
				rowsLeft--;
			if (index < rowDegreeIndices[degree])
				throw new IllegalArgumentException();
			int temp = rows[rowDegreeIndices[degree - 1]];
			rows[rowDegreeIndices[degree - 1]] = rows[index];
			rows[index] = temp;
			rowDegreeIndices[--rowsD[temp]]++;
		}

		private boolean enoughLeft(int degeneracy) {
			if (rowsLeft < degeneracy)
				return false;
			if (columnsLeft < degeneracy)
				return false;
			return true;
		}

		private boolean nextLowestIsRow() {
			int lowestRowD = rowsD[rows[rowDegreeIndices[0]]];
			int lowestColD =
					columnsD[columns[columnDegreeIndices[0]]];
			if (lowestRowD < lowestColD) {
				return true;
			}
			if (lowestColD < lowestRowD) {
				return false;
			}
			return rand.nextBoolean();
		}
	}

	private static MatrixStats
	computeNodeDegree(BinaryMatrix matrix) {
		MatrixStats md = new MatrixStats();
		md.rowsD = new int[matrix.getNumRows()];
		md.columnsD = new int[matrix.getNumColumns()];
		for (int i = 0; i < matrix.getNumRows(); i++) {
			for (int j = 0; j < matrix.getNumColumns(); j++) {
				if (matrix.get(i, j)) {
					md.rowsD[i]++;
					md.columnsD[j]++;
				}
			}
		}
		return md;
	}

	private static MatrixStats computeNodeOrder(MatrixStats ms) {
		ms.rows = computeNodeOrder(ms.rowsD);
		ms.columns = computeNodeOrder(ms.columnsD);
		ms.rowDegreeIndices = getDegreeIndices(ms.rows, ms.rowsD);
		ms.columnDegreeIndices =
				getDegreeIndices(ms.columns, ms.columnsD);
		return ms;
	}

	private static int[] getDegreeIndices(Integer[] orderedNodes,
			int[] degrees) {
		// use highest degree in order as size of index.
		int[] degreeIndices =
				new int[degrees[orderedNodes[orderedNodes.length - 1]]];
		for (int i = 0; i < orderedNodes.length; i++) {
			degreeIndices[degrees[i]]++;
		}
		// calculate end indices of each degree group.
		for (int i = 1; i < degreeIndices.length; i++) {
			degreeIndices[i] += degreeIndices[i - 1];
		}
		return degreeIndices;
	}

	private static Integer[] computeNodeOrder(int[] degrees) {
		Integer[] order = new Integer[degrees.length];
		for (int i = 0; i < degrees.length; i++) {
			order[i] = i;
		}
		Arrays.sort(order, new DegreeComparator(degrees));
		return order;
	}

	private static class DegreeComparator implements
	Comparator<Integer> {

		private final int[] degrees;

		public DegreeComparator(int[] degrees) {
			this.degrees = degrees;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return degrees[o1] - degrees[o2];
		}

	}
}
