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

package algorithms.transformers;

import java.util.ArrayList;
import java.util.List;

import map.MapAlgorithm;
import datatype.bicluster.Bicluster;
import datatype.bicluster.BinaryVector;
import datatype.bicluster.BitSetBicluster;
import datatype.matrix.BinaryMatrix;
import datatype.matrix.BitSetBinaryMatrix;

public class GreedySplitTask implements
MapAlgorithm<Bicluster, Bicluster> {

	private float minDensity = 1f;
	private int minSize = 10;
	private BinaryMatrix matrix;

	public void setMinDensity(float density) {
		this.minDensity = density;
	}

	public void setMinSize(int size) {
		this.minSize = size;
	}

	public void setMatrix(BinaryMatrix binaryMatrix) {
		this.matrix = binaryMatrix;
	}

	@Override
	public List<Bicluster> map(List<Bicluster> inputValues) {
		List<Bicluster> denseBiclusters = new ArrayList<Bicluster>();

		for (Bicluster bicluster : inputValues) {
			denseBiclusters.addAll(breakBicluster(bicluster));
		}

		return denseBiclusters;
	}

	private List<Bicluster> breakBicluster(Bicluster bicluster) {
		List<Bicluster> biclusterParts = new ArrayList<Bicluster>();
		BinaryMatrix coverage =
				new BitSetBinaryMatrix(matrix.getNumRows(), matrix
						.getNumColumns());

		for (int iRow : bicluster.getRows()) {
			for (int iColumn : bicluster.getColumns()) {
				if (matrix.get(iRow, iColumn)
						&& !coverage.get(iRow, iColumn)) {
					Bicluster part =
							buildBicluster(iRow, iColumn, bicluster);
					addCoverage(coverage, part);
					if (part.getNumberOfRows() >= minSize
							&& part.getNumberOfColumns() >= minSize) {
						biclusterParts.add(part);
					}
				}
			}
		}

		return biclusterParts;
	}

	private void addCoverage(BinaryMatrix coverage, Bicluster part) {
		for (int iRow : part.getRows()) {
			for (int iColumn : part.getColumns()) {
				coverage.set(iRow, iColumn);
			}
		}
	}

	private Bicluster buildBicluster(int row, int column,
			Bicluster bicluster) {
		Bicluster densePart = new BitSetBicluster();
		densePart.addColumn(column);
		densePart.addRow(row);
		int numberOfOnes = 1;
		int partSize = 1;

		BinaryVector columns = bicluster.getColumns().clone();
		columns.set(column, false);
		BinaryVector rows = bicluster.getRows().clone();
		rows.set(row, false);

		boolean columnsLeft = columns.cardinality() > 0;
		boolean rowsLeft = rows.cardinality() > 0;

		while (columnsLeft || rowsLeft) {
			boolean isRow = true;
			int bestScore = -1;
			int best = -1;
			if (rowsLeft) {
				for (int iRow : rows) {
					int score = 0;
					for (int iColumn : densePart.getColumns()) {
						if (matrix.get(iRow, iColumn)) {
							score += 5;
						}
					}
					for (int iColumn : columns) {
						if (matrix.get(iRow, iColumn)) {
							score += 1;
						}
					}
					if (score > bestScore) {
						best = iRow;
						bestScore = score;
					}
				}
			}
			if (columnsLeft) {
				for (int iColumn : columns) {
					int score = 0;
					for (int iRow : densePart.getRows()) {
						if (matrix.get(iRow, iColumn)) {
							score += 5;
						}
					}
					for (int iRow : rows) {
						if (matrix.get(iRow, iColumn)) {
							score += 1;
						}
					}
					if (score > bestScore) {
						best = iColumn;
						bestScore = score;
						isRow = false;
					}
				}
			}

			if (isRow) {
				int newOnes = 0;
				for (int iColumn : densePart.getColumns()) {
					if (matrix.get(best, iColumn)) {
						newOnes++;
					}
				}
				int newPartSize = densePart.getNumberOfColumns();
				if ((float) (numberOfOnes + newOnes)
						/ (partSize + newPartSize) > minDensity) {
					densePart.addRow(best);
					numberOfOnes += newOnes;
					partSize += newPartSize;
					rows.set(best, false);
					if (rows.cardinality() == 0) {
						rowsLeft = false;
					}
				} else {
					rowsLeft = false;
				}
			} else {
				int newOnes = 0;
				for (int iRow : densePart.getRows()) {
					if (matrix.get(iRow, best)) {
						newOnes++;
					}
				}
				int newPartSize = densePart.getNumberOfRows();
				if ((float) (numberOfOnes + newOnes)
						/ (partSize + newPartSize) > minDensity) {
					densePart.addColumn(best);
					numberOfOnes += newOnes;
					partSize += newPartSize;
					columns.set(best, false);
					if (columns.cardinality() == 0) {
						columnsLeft = false;
					}
				} else {
					columnsLeft = false;
				}
			}
		}

		return densePart;
	}

}
