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
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import map.KeyValuePair;
import map.MapAlgorithm;
import datatype.bicluster.Bicluster;
import datatype.bicluster.BinaryVector;
import datatype.bicluster.BitSetBicluster;
import datatype.matrix.BinaryMatrix;

public class GreedyDegenerativeBlockTask
implements
MapAlgorithm<KeyValuePair<Integer, Bicluster>, KeyValuePair<Integer, Bicluster>> {

	private static float MIN_DENSITY;
	private static BinaryMatrix MATRIX;
	private static boolean RANDOMIZE_CHOICE;
	private Random rand = new Random();

	public GreedyDegenerativeBlockTask(float minDensity,
			BinaryMatrix matrix, boolean randomChoice) {
		MIN_DENSITY = minDensity;
		MATRIX = matrix;
		RANDOMIZE_CHOICE = randomChoice;
	}

	@Override
	public List<KeyValuePair<Integer, Bicluster>> map(
			List<KeyValuePair<Integer, Bicluster>> keyValuePairs) {
		List<KeyValuePair<Integer, Bicluster>> tuples =
				new ArrayList<KeyValuePair<Integer, Bicluster>>();

		for (KeyValuePair<Integer, Bicluster> bicluster : keyValuePairs) {
			Bicluster denseBicluster = bicluster.getValue().clone();

			tuples.add(new KeyValuePair<Integer, Bicluster>(1,
					getDenseSubPartThingy(denseBicluster, MATRIX)));
		}

		return tuples;
	}

	private Bicluster getDenseSubPartThingy(Bicluster bicluster,
			BinaryMatrix matrix) {
		BinaryVector rows = bicluster.getRows();
		BinaryVector rowsToIterate = bicluster.getRows();
		BinaryVector columns = bicluster.getColumns();
		BinaryVector columnsToIterate = bicluster.getColumns();
		int[] rowCardinalities = new int[rows.cardinality()];
		int[] columnCardinalities = new int[columns.cardinality()];
		Map<Integer, Integer> rowsToIndex =
				new TreeMap<Integer, Integer>();
		Map<Integer, Integer> columnsToIndex =
				new TreeMap<Integer, Integer>();

		int columnIndex = 0;
		for (int column : columns) {
			columnsToIndex.put(column, columnIndex);
			columnIndex++;
		}

		int rowIndex = 0;
		for (int row : rows) {
			rowsToIndex.put(row, rowIndex);
			for (int column : columns) {
				if (matrix.get(row, column)) {
					rowCardinalities[rowIndex]++;
					columnCardinalities[columnsToIndex.get(column)]++;
				}
			}
			rowIndex++;
		}

		while (getDensity(rows, columns, matrix) < MIN_DENSITY) {
			int maxOnes = 0;
			int bestNode = -1;
			boolean isRow = true;
			for (int row : rowsToIterate) {
				if (rowCardinalities[rowsToIndex.get(row)] > maxOnes) {
					if (!RANDOMIZE_CHOICE || rand.nextFloat() > 0.5f) {
						maxOnes =
								rowCardinalities[rowsToIndex
								                 .get(row)];
						bestNode = row;
					}
				}
			}
			for (int column : columnsToIterate) {
				if (columnCardinalities[columnsToIndex.get(column)] > maxOnes) {
					if (!RANDOMIZE_CHOICE || rand.nextFloat() > 0.5f) {
						maxOnes =
								columnCardinalities[columnsToIndex
								                    .get(column)];
						bestNode = column;
						isRow = false;
					}
				}
			}
			if (bestNode == -1) {
				break;
			}
			if (isRow) {
				for (int column : columns) {
					if (matrix.get(bestNode, column)) {
						columnCardinalities[columnsToIndex
						                    .get(column)]--;
					} else {
						columnsToIterate.set(column, false);
						columns.set(column, false);
					}
				}
				rowsToIterate.set(bestNode, false);
			} else {
				for (int row : rows) {
					if (matrix.get(row, bestNode)) {
						rowCardinalities[rowsToIndex.get(row)]--;
					} else {
						rowsToIterate.set(row, false);
						rows.set(row, false);
					}
				}
				columnsToIterate.set(bestNode, false);
			}
		}

		Bicluster denseBlock = new BitSetBicluster();
		denseBlock.addRows(rows);
		denseBlock.addColumns(columns);
		return denseBlock;
	}

	private float getDensity(BinaryVector rows,
			BinaryVector columns, BinaryMatrix matrix) {
		int size = rows.cardinality() * columns.cardinality();
		int count = 0;
		for (int row : rows) {
			for (int column : columns) {
				if (matrix.get(row, column))
					count++;
			}
		}
		return (float) count / size;
	}

	@SuppressWarnings("unused")
	private Bicluster goDownOneLevel(Bicluster bicluster,
			BinaryMatrix matrix, Bicluster theOne) {
		List<BitSet> connections = new ArrayList<BitSet>();
		int[] ids =
				new int[bicluster.getNumberOfRows()
				        + bicluster.getNumberOfColumns()];
		int[] degree = new int[ids.length];
		int[] nodeMap = new int[ids.length];
		List<Integer> nodeOrder = new ArrayList<Integer>(ids.length);
		int index = 0;
		for (int row : bicluster.getRows()) {
			nodeOrder.add(index);
			nodeMap[index] = index;
			ids[index] = row;
			BitSet columnConnections = new BitSet();
			int columnIndex = 0;
			for (int column : bicluster.getColumns()) {
				if (matrix.get(row, column)) {
					columnConnections.set(columnIndex);
				}
				columnIndex++;
			}
			connections.add(columnConnections);
			degree[index] = columnConnections.cardinality();
			index++;
		}
		for (int column : bicluster.getColumns()) {
			nodeOrder.add(index);
			nodeMap[index] = index;
			ids[index] = column;
			BitSet rowConnections = new BitSet();
			int rowIndex = 0;
			for (int row : bicluster.getRows()) {
				if (matrix.get(row, column)) {
					rowConnections.set(rowIndex);
				}
				rowIndex++;
			}
			connections.add(rowConnections);
			degree[index] = rowConnections.cardinality();
			index++;
		}

		while (!nodeOrder.isEmpty()) {
			Collections
			.sort(nodeOrder, new DegreeComparator(degree));
			for (int i =
					connections.get(nodeOrder.get(0)).nextSetBit(0); i >= 0; i =
					connections.get(nodeOrder.get(0)).nextSetBit(
							i + 1)) {
				if (nodeOrder.get(0) < bicluster.getNumberOfRows()) {
					degree[bicluster.getNumberOfRows() + i]--;
					connections.get(bicluster.getNumberOfRows() + i)
					.set(nodeOrder.get(0), false);
				} else {
					degree[i]--;
					connections.get(i).set(
							nodeOrder.get(0)
							- bicluster.getNumberOfRows(),
							false);
				}
			}
			nodeOrder.remove(0);
		}

		int maxDegen = 0;
		int bestIndex = 0;
		for (int i = 0; i < degree.length; i++) {
			if (degree[i] > maxDegen) {
				maxDegen = degree[i];
				bestIndex = i;
			}
		}

		Bicluster newBicluster = new BitSetBicluster();
		if (bestIndex < bicluster.getNumberOfRows()) {
			theOne.addRow(ids[bestIndex]);
			BinaryVector rows = bicluster.getRows().clone();
			rows.set(ids[bestIndex], false);
			newBicluster.addRows(rows);
			for (int column : bicluster.getColumns()) {
				if (matrix.get(ids[bestIndex], column)) {
					newBicluster.addColumn(column);
				}
			}
		} else {
			BinaryVector columns = bicluster.getColumns().clone();
			theOne.addColumn(ids[bestIndex]);
			columns.set(ids[bestIndex], false);
			newBicluster.addColumns(columns);
			for (int row : bicluster.getRows()) {
				if (matrix.get(row, ids[bestIndex])) {
					newBicluster.addRow(row);
				}
			}
		}

		return newBicluster;
	}

	private class DegreeComparator implements Comparator<Integer> {
		private int[] degrees;

		public DegreeComparator(int[] degrees) {
			this.degrees = degrees;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return this.degrees[o1] - this.degrees[o2];
		}

	}

}
