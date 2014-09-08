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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import map.KeyValuePair;
import map.MapAlgorithm;
import datatype.bicluster.Bicluster;
import datatype.bicluster.BitSetBicluster;
import datatype.matrix.BinaryMatrix;

/**
 * This is a MapAlgorithm for splitting blocks into smaller denser
 * blocks. Given any block with below 'minDensity', we start by
 * ordering its rows and columns by density creating an artificial
 * gradient from top-left to bottom-right. We then choose a row
 * offset and a column offset to split by which will create 4 new
 * blocks in the process. The hope if that the top-left block will
 * become much more dense and will be output in the next iteration
 * while the bottom-left block is usually less dense than the
 * starting block and will eventually be thrown away as it is split
 * until it is too small to be output as a new block. The other two
 * blocks that are generated lie somewhere in between the first and
 * the second block and will most likely continue to be split.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class SplitTask implements
MapAlgorithm<Bicluster, KeyValuePair<Integer, Bicluster>> {

	private final BinaryMatrix matrix;
	private final float percentCutMargin = 0.5f;
	private final float percentMinCut = 0.2f;
	private final float percentMaxCut = 0.8f;
	private final int minSize;
	private final float minDensity;

	public SplitTask(BinaryMatrix matrix, int minSize,
			float minDensity) {
		this.matrix = matrix;
		this.minSize = minSize;
		this.minDensity = minDensity;
	}

	@Override
	public List<KeyValuePair<Integer, Bicluster>> map(
			List<Bicluster> biclusters) {
		List<KeyValuePair<Integer, Bicluster>> denseBiclusters =
				new ArrayList<KeyValuePair<Integer, Bicluster>>();

		for (Bicluster bicluster : biclusters) {
			denseBiclusters
			.addAll(computeDenseSubBiclusters(bicluster));
		}

		return denseBiclusters;
	}

	private List<KeyValuePair<Integer, Bicluster>>
	computeDenseSubBiclusters(Bicluster bicluster) {
		Integer[] rows = new Integer[bicluster.getNumberOfRows()];
		Integer[] columns =
				new Integer[bicluster.getNumberOfColumns()];
		int index = 0;
		for (int row : bicluster.getRows()) {
			rows[index] = row;
			index++;
		}
		index = 0;
		for (int column : bicluster.getColumns()) {
			columns[index] = column;
			index++;
		}
		int[] rowCounts = new int[matrix.getNumRows()];
		int[] columnCounts = new int[matrix.getNumColumns()];
		int rowStart = 0;
		int rowEnd = rows.length;
		int columnStart = 0;
		int columnEnd = columns.length;
		int level = 0;

		return split(level, rowStart, rowEnd, columnStart,
				columnEnd, rows, rowCounts, columns, columnCounts);
	}

	private List<KeyValuePair<Integer, Bicluster>> split(int level,
			int rowStart, int rowEnd, int columnStart,
			int columnEnd, Integer[] rows, int[] rowCounts,
			Integer[] columns, int[] columnCounts) {

		List<KeyValuePair<Integer, Bicluster>> denseSubBiclusters =
				new ArrayList<KeyValuePair<Integer, Bicluster>>();

		// CONVENIENCE VARIABLES
		int numberOfRows = rowEnd - rowStart;
		int numberOfColumns = columnEnd - columnStart;

		// TOO SMALL ? EXIT : CONTINUE
		if (numberOfRows < minSize || numberOfColumns < minSize) {
			return denseSubBiclusters;
		}

		// CLEAR DENSITY INFORMATION
		for (int iRow = rowStart; iRow < rowEnd; iRow++) {
			rowCounts[rows[iRow]] = 0;
		}
		for (int iColumn = columnStart; iColumn < columnEnd; iColumn++) {
			columnCounts[columns[iColumn]] = 0;
		}

		// CALCULATE DENSITY
		float totalDensity = 0;
		for (int iRow = rowStart; iRow < rowEnd; iRow++) {
			for (int iColumn = columnStart; iColumn < columnEnd; iColumn++) {
				if (matrix.get(rows[iRow], columns[iColumn])) {
					rowCounts[rows[iRow]]++;
					columnCounts[columns[iColumn]]++;
					totalDensity++;
				}
			}
		}
		totalDensity /= numberOfRows;
		totalDensity /= numberOfColumns;

		// DENSE ENOUGH ? RETURN BICLUSTER : CONTINUE
		if (totalDensity >= minDensity) {
			return generateBicluster(rowStart, rowEnd, columnStart,
					columnEnd, level, rows, columns);
		}

		// SORT ROWS / COLUMNS
		Arrays.sort(rows, rowStart, rowEnd, new DensityComparator(
				rowCounts));
		Arrays.sort(columns, columnStart, columnEnd,
				new DensityComparator(columnCounts));

		// DETERMINE SPLIT FOR ROWS / COLUMNS
		int rowSplit =
				computeSplit(rowStart, rowEnd,
						(int) (numberOfColumns * percentCutMargin),
						numberOfRows, rows, rowCounts);
		int columnSplit =
				computeSplit(columnStart, columnEnd,
						(int) (numberOfRows * percentCutMargin),
						numberOfColumns, columns, columnCounts);

		// RECURSE DENSE -> SEMI -> SEMI -> NON-DENSE
		denseSubBiclusters.addAll(split(level + 1, rowStart,
				rowSplit, columnStart, columnSplit, rows, rowCounts,
				columns, columnCounts));
		denseSubBiclusters.addAll(split(level + 1, rowSplit, rowEnd,
				columnStart, columnSplit, rows, rowCounts, columns,
				columnCounts));
		denseSubBiclusters.addAll(split(level + 1, rowStart,
				rowSplit, columnSplit, columnEnd, rows, rowCounts,
				columns, columnCounts));
		denseSubBiclusters.addAll(split(level + 1, rowSplit, rowEnd,
				columnSplit, columnEnd, rows, rowCounts, columns,
				columnCounts));
		return denseSubBiclusters;
	}

	private int computeSplit(int start, int end, int minOnes,
			int size, Integer[] index, int[] counts) {
		int split = start;
		while (split < end && counts[index[split]] >= minOnes) {
			split++;
		}
		split =
				start
				+ (int) (Math.max((float) (split - start)
						/ size, percentMinCut) * size);
		split =
				start
				+ (int) (Math.min((float) (split - start)
						/ size, percentMaxCut) * size);
		return split;
	}

	private List<KeyValuePair<Integer, Bicluster>>
	generateBicluster(int rowStart, int rowEnd,
			int columnStart, int columnEnd, int level,
			Integer[] rows, Integer[] columns) {
		List<KeyValuePair<Integer, Bicluster>> denseSubBicluster =
				new ArrayList<KeyValuePair<Integer, Bicluster>>();
		Bicluster bicluster = new BitSetBicluster();
		for (int iRow = rowStart; iRow < rowEnd; iRow++) {
			bicluster.addRow(rows[iRow]);
		}
		for (int iColumn = columnStart; iColumn < columnEnd; iColumn++) {
			bicluster.addColumn(columns[iColumn]);
		}
		denseSubBicluster.add(new KeyValuePair<Integer, Bicluster>(
				level, bicluster));
		return denseSubBicluster;
	}

	private class DensityComparator implements Comparator<Integer> {

		private int[] counts;

		public DensityComparator(int[] counts) {
			this.counts = counts;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return counts[o2] - counts[o1];
		}

	}
}
