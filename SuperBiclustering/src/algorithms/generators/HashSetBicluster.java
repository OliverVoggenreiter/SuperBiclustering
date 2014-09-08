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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import datatype.bicluster.Bicluster;
import datatype.bicluster.BinaryVector;
import datatype.bicluster.BitSetBinaryVector;

public class HashSetBicluster implements Bicluster {

	private static final long serialVersionUID = 1L;

	Set<Integer> rows;
	Set<Integer> columns;

	public HashSetBicluster() {
		this.rows = new HashSet<Integer>();
		this.columns = new HashSet<Integer>();
	}

	@Override
	public BinaryVector getRows() {
		BinaryVector vector = new BitSetBinaryVector();
		for (int i : rows) {
			vector.set(i, true);
		}
		return vector;
	}

	@Override
	public BinaryVector getColumns() {
		BinaryVector vector = new BitSetBinaryVector();
		for (int i : columns) {
			vector.set(i, true);
		}
		return vector;
	}

	@Override
	public Iterator<Integer> getRowIterator() {
		return rows.iterator();
	}

	@Override
	public Iterator<Integer> getColumnIterator() {
		return columns.iterator();
	}

	@Override
	public int getNumberOfRows() {
		return rows.size();
	}

	@Override
	public int getNumberOfColumns() {
		return columns.size();
	}

	@Override
	public boolean contains(int rowIndex, int columnIndex) {
		return containsRow(rowIndex) && containsColumn(columnIndex);
	}

	@Override
	public boolean containsRow(int rowIndex) {
		return rows.contains(rowIndex);
	}

	@Override
	public boolean containsColumn(int columnIndex) {
		return columns.contains(columnIndex);
	}

	@Override
	public void addRow(int rowIndex) {
		rows.add(rowIndex);
	}

	@Override
	public void addColumn(int columnIndex) {
		columns.add(columnIndex);
	}

	@Override
	public void addRows(BinaryVector rows) {
		for (int row : rows) {
			addRow(row);
		}
	}

	@Override
	public void addColumns(BinaryVector columns) {
		for (int column : columns) {
			addColumn(column);
		}
	}

	@Override
	public Bicluster clone() {
		Bicluster clone = null;
		try {
			clone = (HashSetBicluster) super.clone();
		} catch (CloneNotSupportedException e) {
			System.err
			.println("Something went wrong during ArrayBicluster cloning!");
			e.printStackTrace();
		}
		return clone;
	}

}
