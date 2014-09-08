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
import java.util.Random;

import datatype.bicluster.BinaryVector;
import datatype.bicluster.BitSetBinaryVector;
import datatype.matrix.BinaryMatrix;

/**
 * This is an implementation of
 * "An O(m) Algorithm for Cores Decomposition of Networks" by
 * Vladimir Batagelj and Matjaz Zaversnik (2003). Given a
 * BinaryMatrix, it will create an ordering of the rows and columns
 * such that the arboricity of the induced graph is minimal. The
 * algorithm runs in linear time and is the basis for improving the
 * performance of various graph related algorithms.
 *
 * @author "Oliver Voggenreiter"
 * @date Feb 25, 2013
 *
 */
public class MatrixDegeneracy {

	private static final Random RANDOM = new Random(1337);

	/**
	 * Calculates a degeneracy ordering of the rows and columns of
	 * matrix and returns it as an integer array.
	 */
	public static int[]
			computeDegeneracyOrdering(BinaryMatrix matrix) {
		int[] counts = computeNodeDegrees(matrix);
		int[] ordering = computeOrdering(counts, matrix);

		return ordering;
	}

	/**
	 * Calculates the number of connections each row/col has in the
	 * matrix. Columns are placed in the 2nd half of the array
	 * starting at matrix.getNumRows() offset.
	 */
	public static int[] computeNodeDegrees(BinaryMatrix matrix) {
		int[] connectivityCounts =
				new int[matrix.getNumRows() + matrix.getNumColumns()];

		for (int iRow = 0; iRow < matrix.getNumRows(); iRow++) {
			for (int iColumn = 0; iColumn < matrix.getNumColumns(); iColumn++) {
				if (matrix.get(iRow, iColumn)) {
					connectivityCounts[iRow]++;
					connectivityCounts[matrix.getNumRows() + iColumn]++;
				}
			}
		}

		return connectivityCounts;
	}

	private static int[] computeOrdering(int[] connectivityValues,
			BinaryMatrix matrix) {
		int[] degeneracyOrdering =
				initializeAscendingArray(matrix.getNumRows()
						+ matrix.getNumColumns());
		int[] connectivity = connectivityValues.clone();
		BinaryVector rowsLeft =
				new BitSetBinaryVector(matrix.getNumRows(), true);
		BinaryVector columnsLeft =
				new BitSetBinaryVector(matrix.getNumColumns(), true);
		List<List<Integer>> degeneracyGroups =
				computeDegeneracyGroups(connectivity);

		for (int iPosition = 0; iPosition < degeneracyOrdering.length; iPosition++) {
			int firstNonEmptyGroup =
					computeIndexOfFirstNonEmptyList(degeneracyGroups);
			int chosenPosition =
					chooseRandomNodePositionInGroup(
							firstNonEmptyGroup, degeneracyGroups);
			int chosenNode =
					degeneracyGroups.get(firstNonEmptyGroup).remove(
							chosenPosition);
			connectivity[chosenNode] = 0;

			if (chosenNode < matrix.getNumRows()) {
				// Choice is a Row!
				rowsLeft.set(chosenNode, false);
				updateColumnNeighbours(chosenNode, degeneracyGroups,
						connectivity, columnsLeft, matrix);
			} else {
				// Choice is Column!
				columnsLeft.set(chosenNode - matrix.getNumRows(),
						false);
				updateRowNeighbours(
						chosenNode - matrix.getNumRows(),
						degeneracyGroups, connectivity, rowsLeft,
						matrix);
			}

			updateDegeneracyOrder(degeneracyOrdering, iPosition,
					chosenNode);
		}

		return degeneracyOrdering;
	}

	/**
	 * Creates an integer array with numbers from 0 to (N - 1).
	 */
	public static int[] initializeAscendingArray(int size) {
		int[] array = new int[size];
		for (int i = 0; i < size; i++) {
			array[i] = i;
		}
		return array;
	}

	/**
	 * Takes a set of degree counts for rows and columns of a matrix
	 * and arranges the indices accordingly. For every type of degree
	 * count that exists in the matrix, we have an arraylist that
	 * stores the corresponding indices.
	 *
	 */
	public static List<List<Integer>> computeDegeneracyGroups(
			int[] connectivityValues) {
		List<List<Integer>> degeneracyGroups =
				new ArrayList<List<Integer>>();

		for (int iNode = 0; iNode < connectivityValues.length; iNode++) {
			while (degeneracyGroups.size() <= connectivityValues[iNode]) {
				degeneracyGroups.add(new ArrayList<Integer>());
			}
			degeneracyGroups.get(connectivityValues[iNode]).add(
					iNode);
		}

		return degeneracyGroups;
	}

	/**
	 * Takes one of the group lists generated by
	 * computeDegeneracyGroups and determines the first arraylist in
	 * that list of groups that is non-empty.
	 *
	 */
	public static int computeIndexOfFirstNonEmptyList(
			List<List<Integer>> lists) {
		int index = 0;
		while (lists.get(index).size() == 0) {
			index++;
		}
		return index;
	}

	/**
	 * Chooses one of the indices given in the first non-empty group
	 * of the degeneracy group list.
	 */
	public static int chooseRandomNodePositionInGroup(
			int groupIndex, List<List<Integer>> groups) {
		int groupSize = groups.get(groupIndex).size();
		int randomPosition = RANDOM.nextInt(groupSize);
		return randomPosition;
	}

	private static void updateColumnNeighbours(int row,
			List<List<Integer>> degeneracyGroups,
			int[] connectivityValues, BinaryVector columns,
			BinaryMatrix matrix) {
		for (int column : columns) {
			if (matrix.get(row, column)) {
				int connectedColumn = matrix.getNumRows() + column;
				int connectedColumnPosition =
						degeneracyGroups.get(
								connectivityValues[connectedColumn])
								.indexOf(connectedColumn);
				degeneracyGroups.get(
						connectivityValues[connectedColumn]).remove(
								connectedColumnPosition);
				connectivityValues[connectedColumn]--;
				degeneracyGroups.get(
						connectivityValues[connectedColumn]).add(
								connectedColumn);
			}
		}
	}

	private static void updateRowNeighbours(int column,
			List<List<Integer>> degeneracyGroups,
			int[] connectivity, BinaryVector rows,
			BinaryMatrix matrix) {
		for (int row : rows) {
			if (matrix.get(row, column)) {
				int connectedRowPosition =
						degeneracyGroups.get(connectivity[row])
						.indexOf(row);
				degeneracyGroups.get(connectivity[row]).remove(
						connectedRowPosition);
				connectivity[row]--;
				degeneracyGroups.get(connectivity[row]).add(row);
			}
		}
	}

	private static void updateDegeneracyOrder(
			int[] degeneracyOrdering, int position, int chosenNode) {
		int chosenNodePosition =
				computeChosenNodePosition(position,
						degeneracyOrdering, chosenNode);
		swapIndices(degeneracyOrdering, chosenNodePosition, position);
	}

	private static int computeChosenNodePosition(int startPosition,
			int[] degeneracyOrdering, int chosenNode) {
		int search = startPosition;
		while (degeneracyOrdering[search] != chosenNode) {
			search++;
		}
		return search;
	}

	private static void swapIndices(int[] list, int index1,
			int index2) {
		int temp = list[index1];
		list[index1] = list[index2];
		list[index2] = temp;
	}
}
