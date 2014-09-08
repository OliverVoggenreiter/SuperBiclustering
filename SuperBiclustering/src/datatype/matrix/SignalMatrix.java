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

import util.MatrixUtils;

public class SignalMatrix extends BasicFloatMatrix {
	private static final long serialVersionUID = 1L;

	private String[] rowNames;
	private String[] columnNames;

	public SignalMatrix(int numRows, int numColumns) {
		super(numRows, numColumns);

		rowNames = new String[numRows];
		columnNames = new String[numColumns];

		for (int i = 0; i < numRows; i++) {
			rowNames[i] = "row " + i;
		}
		for (int i = 0; i < numColumns; i++) {
			columnNames[i] = "column " + i;
		}
	}

	private SignalMatrix(int numRows, int numColumns,
			String[] rowNames, String[] columnNames, float[][] data) {
		super(numRows, numColumns, data);
		this.rowNames = rowNames;
		this.columnNames = columnNames;
	}

	public String getRowName(int row) {
		return rowNames[row];
	}

	public void setRowName(int row, String name) {
		rowNames[row] = name;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public void setColumnName(int column, String name) {
		columnNames[column] = name;
	}

	@Override
	public void transpose() {
		super.transpose();
		String[] y = rowNames;
		rowNames = columnNames;
		columnNames = y;
	}

	@Override
	public SignalMatrix clone() {
		return new SignalMatrix(numRows, numColumns, rowNames
				.clone(), columnNames.clone(), MatrixUtils
				.deepClone(data));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numRows;
		result = prime * result + numColumns;
		result = prime * result + Arrays.hashCode(rowNames);
		result = prime * result + Arrays.hashCode(columnNames);
		result = prime * result + Arrays.deepHashCode(data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SignalMatrix other = (SignalMatrix) obj;
		if (numRows != other.numRows) {
			return false;
		}
		if (numColumns != other.numColumns) {
			return false;
		}
		if (!Arrays.equals(rowNames, other.rowNames)) {
			return false;
		}
		if (!Arrays.equals(columnNames, other.columnNames)) {
			return false;
		}
		if (!Arrays.deepEquals(data, other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SignalMatrix [numRows=" + numRows + ", numColumns="
				+ numColumns + ", rowNames="
				+ Arrays.toString(rowNames) + ", columnNames="
				+ Arrays.toString(columnNames) + ", data="
				+ Arrays.deepToString(data) + "]";
	}

}
