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

package datatype.bicluster;

import java.util.Iterator;

public class ArrayBicluster implements Bicluster {

	private static final long serialVersionUID = 12345L;

	private int[] rows;
	private int currentRow = 0;
	private int[] columns;
	private int currentColumn = 0;

	public ArrayBicluster(int rowSize, int columnSize) {
		rows = new int[rowSize];
		columns = new int[columnSize];
	}

	@Override
	public BinaryVector getRows() {
		BinaryVector vector = new BitSetBinaryVector();
		for (int i = 0; i < rows.length; i++) {
			vector.set(rows[i], true);
		}
		return vector;
	}

	@Override
	public BinaryVector getColumns() {
		BinaryVector vector = new BitSetBinaryVector();
		for (int i = 0; i < columns.length; i++) {
			vector.set(columns[i], true);
		}
		return vector;
	}

	@Override
	public Iterator<Integer> getRowIterator() {
		return new IntArrayIterator(rows);
	}

	@Override
	public Iterator<Integer> getColumnIterator() {
		return new IntArrayIterator(columns);
	}

	private class IntArrayIterator implements Iterator<Integer> {

		private int[] arr;
		private int index = 0;

		public IntArrayIterator(int[] array) {
			this.arr = array;
		}

		@Override
		public boolean hasNext() {
			return index < arr.length;
		}

		@Override
		public Integer next() {
			index++;
			return arr[index - 1];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"Cannot remove element from int[] iterator.");
		}

	}

	@Override
	public int getNumberOfRows() {
		return currentRow;
	}

	@Override
	public int getNumberOfColumns() {
		return currentColumn;
	}

	@Override
	public boolean contains(int rowIndex, int columnIndex) {
		return containsRow(rowIndex) && containsColumn(columnIndex);
	}

	@Override
	public boolean containsRow(int rowIndex) {
		for (int i = 0; i < rows.length; i++) {
			if (rows[i] == rowIndex)
				return true;
		}
		return false;
	}

	@Override
	public boolean containsColumn(int columnIndex) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == columnIndex)
				return true;
		}
		return false;
	}

	@Override
	public void addRow(int rowIndex) {
		if (currentRow < rows.length) {
			rows[currentRow] = rowIndex;
			currentRow++;
		} else {
			throw new ArrayIndexOutOfBoundsException(
					"Cannot add more rows to fixed size bicluster!");
		}
	}

	@Override
	public void addColumn(int columnIndex) {
		if (currentColumn < columns.length) {
			columns[currentColumn] = columnIndex;
			currentColumn++;
		} else {
			throw new ArrayIndexOutOfBoundsException(
					"Cannot add more columns to fixed size bicluster!");
		}
	}

	@Override
	public void addRows(BinaryVector rows) {
		for (Integer row : rows) {
			addRow(row);
		}
	}

	@Override
	public void addColumns(BinaryVector columns) {
		for (Integer column : columns) {
			addColumn(column);
		}
	}

	@Override
	public Bicluster clone() {
		Bicluster clone = null;
		try {
			clone = (ArrayBicluster) super.clone();
		} catch (CloneNotSupportedException e) {
			System.err
			.println("Something went wrong during ArrayBicluster cloning!");
			e.printStackTrace();
		}
		return clone;
	}

}
