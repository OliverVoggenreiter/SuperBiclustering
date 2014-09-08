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

import java.io.Serializable;
import java.util.Iterator;

public interface Bicluster extends Serializable, Cloneable {

	public BinaryVector getRows();

	public BinaryVector getColumns();

	public Iterator<Integer> getRowIterator();

	public Iterator<Integer> getColumnIterator();

	public int getNumberOfRows();

	public int getNumberOfColumns();

	public boolean contains(int rowIndex, int columnIndex);

	public boolean containsRow(int rowIndex);

	public boolean containsColumn(int columnIndex);

	public void addRow(int rowIndex);

	public void addColumn(int columnIndex);

	public void addRows(BinaryVector rows);

	public void addColumns(BinaryVector columns);

	public Bicluster clone();

}
