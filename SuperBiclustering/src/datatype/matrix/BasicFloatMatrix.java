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

import util.ArrayUtils;
import util.MatrixUtils;

@SuppressWarnings("serial")
public class BasicFloatMatrix implements FloatMatrix {

	protected int numRows;
	protected int numColumns;
	protected float[][] data;

	public BasicFloatMatrix(int numRows, int numColumns) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		data = new float[numRows][numColumns];
	}

	public BasicFloatMatrix(int numRows, int numColumns,
			float[][] data) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.data = data;
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
	public float get(int row, int column) {
		// implicit range check is performed
		return data[row][column];
	}

	@Override
	public void set(int row, int column, float value) {
		// implicit range check is performed
		data[row][column] = value;
	}

	@Override
	public void setRow(int iRow, float[] values) {
		if (values.length != numColumns) {
			throw new IllegalArgumentException(
					"Invalid number of columns");
		}
		// implicit range check is performed
		data[iRow] = values;
	}

	@Override
	public float[] getRow(int iRow) {
		return data[iRow];
	}

	@Override
	// TODO extract subX methods to common base class or util class
	public
	BasicFloatMatrix getSubMatrix(int[] rowIndices,
			int[] columnIndices) {
		int subRows = rowIndices.length;
		int subColumns = columnIndices.length;
		float[][] subData = new float[subRows][subColumns];

		for (int iRow = 0; iRow < subRows; iRow++) {
			for (int iColumn = 0; iColumn < subColumns; iColumn++) {
				subData[iRow][iColumn] =
						data[rowIndices[iRow]][columnIndices[iColumn]];
			}
		}
		return new BasicFloatMatrix(subRows, subColumns, subData);
	}

	@Override
	public BasicFloatMatrix getSubRows(int[] rowIndices) {
		int subRows = rowIndices.length;
		float[][] subData = new float[subRows][numColumns];

		for (int iRow = 0; iRow < subRows; iRow++) {
			for (int iColumn = 0; iColumn < numColumns; iColumn++) {
				subData[iRow] = data[rowIndices[iRow]].clone();
			}
		}
		return new BasicFloatMatrix(subRows, numColumns, subData);
	}

	@Override
	public BasicFloatMatrix getSubColumns(int[] columnIndices) {
		return getSubMatrix(ArrayUtils.getSequence(numRows),
				columnIndices);
	}

	@Override
	public void transpose() {
		int x = numRows;
		numRows = numColumns;
		numColumns = x;
		data = MatrixUtils.transpose(data);
	}

	@Override
	public BasicFloatMatrix clone() {
		return new BasicFloatMatrix(numRows, numColumns, MatrixUtils
				.deepClone(data));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numRows;
		result = prime * result + numColumns;
		result = prime * result + Arrays.deepHashCode(data);
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
		BasicFloatMatrix other = (BasicFloatMatrix) obj;
		if (numRows != other.numRows)
			return false;
		if (numColumns != other.numColumns)
			return false;
		if (!Arrays.deepEquals(data, other.data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BasicFloatMatrix [numRows=" + numRows
				+ ", numColumns=" + numColumns + ", data="
				+ Arrays.deepToString(data) + "]";
	}

}