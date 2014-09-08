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
import java.util.Collection;
import java.util.List;

import datatype.matrix.BinaryMatrix;

public class BBKTaskFactory {

	private static BinaryMatrix matrix;
	private static int minSize;

	public static void setMatrix(BinaryMatrix binMat) {
		matrix = binMat;
		BBKTask.setMatrix(binMat);
		BBKPWorker.setBinaryMatrix(binMat);
	}

	public static void setMinSize(int size) {
		minSize = size;
	}

	public static BBKTask createTask(int row, int column) {
		List<Integer> rowSet = new ArrayList<Integer>();
		for (int iRow = 0; iRow < matrix.getNumRows(); iRow++) {
			if (matrix.get(iRow, column)) {
				rowSet.add(iRow);
			}
		}

		List<Integer> columnSet = new ArrayList<Integer>();
		for (int iColumn = 0; iColumn < matrix.getNumColumns(); iColumn++) {
			if (matrix.get(row, iColumn)) {
				columnSet.add(iColumn);
			}
		}

		BBKTask task = new BBKTask();
		task.setMinSize(minSize);
		task.rows = convertIntegers(rowSet, row);
		task.columns = convertIntegers(columnSet, column);

		return task;
	}

	public static Collection<Integer> getTasks() {
		List<Integer> rows = new ArrayList<Integer>();
		for (int iRow = 0; iRow < matrix.getNumRows(); iRow++) {
			rows.add(iRow);
		}
		return rows;
	}

	@Deprecated
	public static Collection<BBKTask> createTasks(BinaryMatrix matrix, int minSize) {
		List<BBKTask> tasks = new ArrayList<BBKTask>();

		List<List<Integer>> columnSets = new ArrayList<List<Integer>>();

		for (int iRow = 0; iRow < matrix.getNumRows(); iRow++) {
			List<Integer> ones = new ArrayList<Integer>();
			for (int iColumn = 0; iColumn < matrix.getNumColumns(); iColumn++) {
				if (matrix.get(iRow, iColumn)) {
					ones.add(iColumn);
				}
			}
			columnSets.add(ones);
		}

		List<List<Integer>> rowSets = new ArrayList<List<Integer>>();

		for(int iColumn = 0; iColumn < matrix.getNumColumns(); iColumn++) {
			List<Integer> ones = new ArrayList<Integer>();
			for(int iRow = 0; iRow < matrix.getNumRows(); iRow++) {
				if(matrix.get(iRow, iColumn)) {
					ones.add(iRow);
				}
			}
			rowSets.add(ones);
		}

		for(int iRow = 0; iRow < matrix.getNumRows(); iRow++) {
			for(int iColumn = 0; iColumn < matrix.getNumColumns(); iColumn++) {
				if(matrix.get(iRow, iColumn)) {
					BBKTask task = new BBKTask();
					task.setMinSize(minSize);
					task.rows = convertIntegers(rowSets.get(iColumn), iRow);
					task.columns = convertIntegers(columnSets.get(iRow), iColumn);
					tasks.add(task);
				}
			}
		}

		BBKTask.setMatrix(matrix);

		return tasks;
	}

	public static int[] convertIntegers(List<Integer> integers, int chosen) {
		int[] ret = new int[integers.size()];
		ret[0] = chosen;
		int index = 1;
		for (Integer n : integers) {
			if (n != chosen) {
				ret[index] = n;
				index++;
			}
		}
		return ret;
	}

}
