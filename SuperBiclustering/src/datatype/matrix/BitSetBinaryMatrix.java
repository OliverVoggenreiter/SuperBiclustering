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

package datatype.matrix;

import java.util.Arrays;

import datatype.bicluster.BinaryVector;
import datatype.bicluster.BitSetBinaryVector;

@SuppressWarnings("serial")
public class BitSetBinaryMatrix implements BinaryMatrix {

	private int numRows, numColumns;
	private BitSetBinaryVector[] data;

	public BitSetBinaryMatrix(int numRows, int numColumns) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		data = createMatrix(numRows, numColumns);
	}

	// private constructor, used for efficient cloning
	private BitSetBinaryMatrix(int numRows, int numColumns,
			BitSetBinaryVector[] data) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.data = data;
	}

	private BitSetBinaryVector[] createMatrix(int numRows,
			int numColumns) {
		BitSetBinaryVector[] matrix =
				new BitSetBinaryVector[numRows];
		for (int iRow = 0; iRow < numRows; iRow++) {
			matrix[iRow] = new BitSetBinaryVector(numColumns);
		}
		return matrix;
	}

	@Override
	public int getNumRows() {
		return numRows;
	}

	@Override
	public int getNumColumns() {
		return numColumns;
	}

	@Override
	public boolean get(int rowIndex, int columnIndex) {
		if (rowIndex >= numRows || rowIndex < 0) {
			throw new IndexOutOfBoundsException("invalid rowIndex");
		}
		if (columnIndex >= numColumns || columnIndex < 0) {
			throw new IndexOutOfBoundsException(
					"invalid columnIndex");
		}
		return data[rowIndex].get(columnIndex);
	}

	@Override
	public void set(int rowIndex, int columnIndex, boolean value) {
		if (rowIndex >= numRows || rowIndex < 0) {
			throw new IndexOutOfBoundsException("invalid rowIndex");
		}
		if (columnIndex >= numColumns || columnIndex < 0) {
			throw new IndexOutOfBoundsException(
					"invalid columnIndex");
		}
		data[rowIndex].set(columnIndex, value);
	}

	@Override
	public void set(int rowIndex, int columnIndex) {
		this.set(rowIndex, columnIndex, true);
	}

	@Override
	public float getDensity() {
		int numOnes = 0;
		for (int iRow = 0; iRow < numRows; iRow++) {
			for (int iColumn = 0; iColumn < numColumns; iColumn++) {
				if (get(iRow, iColumn)) {
					numOnes++;
				}
			}
		}
		return numOnes / (float) (numRows * numColumns);
	}

	@Override
	public void transpose() {
		BitSetBinaryVector[] transposed =
				createMatrix(numColumns, numRows);

		for (int iRow = 0; iRow < numRows; iRow++) {
			for (int iColumn = 0; iColumn < numColumns; iColumn++) {
				transposed[iColumn].set(iRow, data[iRow]
						.get(iColumn));
			}
		}
		data = transposed;
		int t = numRows;
		numRows = numColumns;
		numColumns = t;
	}

	@Override
	public BinaryMatrix getSubMatrix(BinaryVector rows,
			BinaryVector columns) {
		BitSetBinaryMatrix subMatrix =
				new BitSetBinaryMatrix(rows.cardinality(), columns
						.cardinality());

		int dstRowIndex = 0;
		for (int srcRowIndex : rows) {
			int dstColumnIndex = 0;
			for (int srcColumnIndex : columns) {
				subMatrix.set(dstRowIndex, dstColumnIndex, this.get(
						srcRowIndex, srcColumnIndex));
				dstColumnIndex++;
			}
			dstRowIndex++;
		}
		return subMatrix;
	}

	@Override
	public BinaryMatrix getSubRows(BinaryVector rows) {
		BitSetBinaryVector[] subData =
				new BitSetBinaryVector[rows.cardinality()];

		int dstRowIndex = 0;
		for (int srcRowIndex : rows) {
			subData[dstRowIndex] = data[srcRowIndex].clone();
			dstRowIndex++;
		}
		return new BitSetBinaryMatrix(rows.cardinality(),
				numColumns, subData);
	}

	@Override
	public BinaryMatrix getSubColumns(BinaryVector columns) {
		BinaryVector rows = new BitSetBinaryVector(numRows, true);
		return getSubMatrix(rows, columns);
	}

	@Override
	public BitSetBinaryMatrix clone() {
		BitSetBinaryVector[] clonedData =
				new BitSetBinaryVector[numRows];
		for (int iRow = 0; iRow < numRows; iRow++) {
			clonedData[iRow] = data[iRow].clone();
		}
		return new BitSetBinaryMatrix(numRows, numColumns,
				clonedData);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + numColumns;
		result = prime * result + numRows;
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
		BitSetBinaryMatrix other = (BitSetBinaryMatrix) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (numColumns != other.numColumns)
			return false;
		if (numRows != other.numRows)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BitSetBinaryMatrix [numRows=" + numRows
				+ ", numColumns=" + numColumns + ", data="
				+ Arrays.toString(data) + "]";
	}

}
