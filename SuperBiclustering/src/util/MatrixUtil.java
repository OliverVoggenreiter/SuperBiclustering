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

package util;

import datatype.matrix.BinaryMatrix;
import datatype.matrix.BitSetBinaryMatrix;
import datatype.matrix.SignalMatrix;

public class MatrixUtil {

	public static SignalMatrix subtractMatrices(
			SignalMatrix treatment, SignalMatrix control) {
		if (treatment.getNumColumns() != control.getNumColumns())
			throw new IllegalArgumentException(
					"Matrices do not have same column number");
		if (treatment.getNumRows() != control.getNumRows())
			throw new IllegalArgumentException(
					"Matrices do not have same row number");
		SignalMatrix resultMatrix =
				new SignalMatrix(treatment.getNumRows(), treatment
						.getNumColumns());
		for (int row = 0; row < treatment.getNumRows(); row++) {
			resultMatrix.setRowName(row, treatment.getRowName(row));
			for (int column = 0; column < treatment.getNumColumns(); column++) {
				resultMatrix.set(row, column, treatment.get(row,
						column)
						- control.get(row, column));
			}
		}
		for (int column = 0; column < treatment.getNumColumns(); column++) {
			resultMatrix.setColumnName(column, treatment
					.getColumnName(column)
					+ "|" + control.getColumnName(column));
		}
		return resultMatrix;
	}

	public static SignalMatrix mergeMatrices(
			SignalMatrix... matrices) {
		if (matrices.length == 0)
			throw new IllegalArgumentException(
					"Trying to merge zero matrices");
		int totalWidth = 0;
		int matrixHeight = matrices[0].getNumRows();
		for (SignalMatrix matrix : matrices) {
			if (matrix.getNumRows() != matrixHeight)
				throw new IllegalArgumentException(
						"Not all matrices of same row count");
			totalWidth += matrix.getNumColumns();
		}
		SignalMatrix mergedMatrix =
				new SignalMatrix(matrixHeight, totalWidth);
		for (int row = 0; row < matrices[0].getNumRows(); row++) {
			mergedMatrix
			.setRowName(row, matrices[0].getRowName(row));
		}
		int index = 0;
		for (SignalMatrix matrix : matrices) {
			for (int column = 0; column < matrix.getNumColumns(); column++) {
				for (int row = 0; row < matrix.getNumRows(); row++) {
					mergedMatrix.set(row, index, matrix.get(row,
							column));
				}
				mergedMatrix.setColumnName(index, matrix
						.getColumnName(column));
				index++;
			}
		}
		return mergedMatrix;
	}

	public static long countOnesInMatrix(BinaryMatrix matrix) {
		long oneCount = 0;
		for (int iRow = 0; iRow < matrix.getNumRows(); iRow++) {
			for (int iColumn = 0; iColumn < matrix.getNumColumns(); iColumn++) {
				if (matrix.get(iRow, iColumn)) {
					oneCount++;
				}
			}
		}
		return oneCount;
	}

	public static long countZerosInMatrix(BinaryMatrix matrix) {
		long zeroCount = 0;
		for (int iRow = 0; iRow < matrix.getNumRows(); iRow++) {
			for (int iColumn = 0; iColumn < matrix.getNumColumns(); iColumn++) {
				if (!matrix.get(iRow, iColumn)) {
					zeroCount++;
				}
			}
		}
		return zeroCount;
	}

	public static BinaryMatrix cloneMatrix(BinaryMatrix matrix) {
		BinaryMatrix matrixCopy =
				new BitSetBinaryMatrix(matrix.getNumRows(), matrix
						.getNumColumns());
		for (int iRow = 0; iRow < matrix.getNumRows(); iRow++) {
			for (int iColumn = 0; iColumn < matrix.getNumColumns(); iColumn++) {
				if (matrix.get(iRow, iColumn)) {
					matrixCopy.set(iRow, iColumn);
				}
			}
		}

		return matrixCopy;
	}

	public static BinaryMatrix getSubMatrix(BinaryMatrix matrix,
			int rowOffset, int columnOffset, int rowHeight,
			int columnWidth) {
		if (rowOffset + rowHeight > matrix.getNumRows()
				|| rowOffset < 0 || rowHeight < 0)
			throw new IllegalArgumentException(
					"Row arguments out of range. Offset: "
							+ rowOffset + " Height: " + rowHeight);
		if (columnOffset + columnWidth > matrix.getNumColumns()
				|| columnOffset < 0 || columnWidth < 0)
			throw new IllegalArgumentException(
					"Column arguments out of range. Offset: "
							+ columnOffset + " Width: "
							+ columnWidth);

		BinaryMatrix subMatrix =
				new BitSetBinaryMatrix(rowHeight, columnWidth);
		for (int iRow = 0; iRow < rowHeight; iRow++) {
			for (int iColumn = 0; iColumn < columnWidth; iColumn++) {
				if (matrix.get(rowOffset + iRow, columnOffset
						+ iColumn)) {
					subMatrix.set(iRow, iColumn);
				}
			}
		}

		return subMatrix;
	}

	public static BinaryMatrix getRandomSubMatrix(
			BinaryMatrix matrix, int rowSize, int columnSize) {
		if (rowSize > matrix.getNumRows() || rowSize < 0)
			throw new IllegalArgumentException(
					"Row arguments out of range. RowSize: "
							+ rowSize);
		if (columnSize > matrix.getNumColumns() || columnSize < 0)
			throw new IllegalArgumentException(
					"Column arguments out of range. ColumnSize: "
							+ columnSize);

		BinaryMatrix subMatrix =
				new BitSetBinaryMatrix(rowSize, columnSize);

		Integer[] randRows =
				RandomUtil.getRandomUniqueIndices(0, matrix
						.getNumRows(), rowSize);
		Integer[] randColumns =
				RandomUtil.getRandomUniqueIndices(0, matrix
						.getNumColumns(), columnSize);

		for (int iRow = 0; iRow < rowSize; iRow++) {
			for (int iColumn = 0; iColumn < columnSize; iColumn++) {
				if (matrix.get(randRows[iRow], randColumns[iColumn])) {
					subMatrix.set(iRow, iColumn);
				}
			}
		}

		return subMatrix;
	}

}
