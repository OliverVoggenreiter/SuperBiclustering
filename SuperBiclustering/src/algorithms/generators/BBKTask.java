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
import java.util.List;

import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class BBKTask {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(columns);
		result = prime * result + Arrays.hashCode(rows);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BBKTask other = (BBKTask) obj;
		if (!Arrays.equals(columns, other.columns))
			return false;
		if (!Arrays.equals(rows, other.rows))
			return false;
		return true;
	}

	private static BinaryMatrix matrix;

	public static void setMatrix(BinaryMatrix mat) {
		matrix = mat;
	}

	private List<Bicluster> biclusters;

	private int minSize = 1;

	public BBKTask setMinSize(int size) {
		this.minSize = size;
		return this;
	}

	private int maxBiclusters = 1;

	public BBKTask setMaxBiclustersPerOne(int max) {
		this.maxBiclusters = max;
		return this;
	}

	int[] rows;
	int[] columns;

	public List<Bicluster> run() {
		biclusters = new ArrayList<Bicluster>(maxBiclusters);
		bicluster(1, rows.length, 1, columns.length);
		return biclusters;
	}

	private void bicluster(int rowTEnd, int rowEnd, int colTEnd, int colEnd) {
		if (biclusters.size() > maxBiclusters) {
			return;
		}
		if (rowTEnd >= minSize && colTEnd >= minSize) {
			biclusters.add(BiclusterFactory.newBicluster(rows, rowTEnd, columns, colTEnd));
			return;
		}
		if (rowEnd < minSize || colEnd < minSize) {
			return;
		}

		if (rowTEnd > colTEnd && (colEnd - colTEnd) > 0) {
			// Choose Column
			chooseColumn(rowTEnd, rowEnd, colTEnd, colEnd);
		} else if (colTEnd > rowTEnd && (rowEnd - rowTEnd) > 0) {
			// Choose Row
			chooseRow(rowTEnd, rowEnd, colTEnd, colEnd);
		} else if ((rowEnd - rowTEnd) > 0) {
			// Choose Row
			chooseRow(rowTEnd, rowEnd, colTEnd, colEnd);
		} else if ((colEnd - colTEnd) > 0) {
			// Choose Column
			chooseColumn(rowTEnd, rowEnd, colTEnd, colEnd);
		}
	}

	private void chooseRow(int rowTEnd, int rowEnd, int colTEnd, int colEnd) {
		while (rowTEnd < rowEnd) {
			int j = colTEnd;
			int newColEnd = colEnd;
			while (j < newColEnd) {
				if (matrix.get(rows[rowTEnd], columns[j])) {
					j++;
				} else {
					newColEnd--;
					swap(columns, newColEnd, j);
				}
			}
			bicluster(rowTEnd + 1, rowEnd, colTEnd, newColEnd);
			rowEnd--;
			swap(rows, rowTEnd, rowEnd);
		}
	}

	private void chooseColumn(int rowTEnd, int rowEnd, int colTEnd, int colEnd) {
		while (colTEnd < colEnd) {
			int j = rowTEnd;
			int newRowEnd = rowEnd;
			while (j < newRowEnd) {
				if (matrix.get(rows[j], columns[colTEnd])) {
					j++;
				} else {
					newRowEnd--;
					swap(rows, newRowEnd, j);
				}
			}
			bicluster(rowTEnd, newRowEnd, colTEnd + 1, colEnd);
			colEnd--;
			swap(columns, colEnd, colTEnd);
		}
	}

	private void swap(int[] array, int a, int b) {
		int temp = array[a];
		array[a] = array[b];
		array[b] = temp;
	}

}
