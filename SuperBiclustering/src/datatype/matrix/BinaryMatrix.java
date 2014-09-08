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

import java.io.Serializable;

import datatype.bicluster.BinaryVector;

/**
 * A fixed-size binary matrix.
 */
public interface BinaryMatrix extends Cloneable, Serializable {

	public int getNumRows();

	public int getNumColumns();

	public boolean get(int rowIndex, int columnIndex);

	public void set(int rowIndex, int columnIndex, boolean value);

	public void set(int rowIndex, int columnIndex);

	public float getDensity();

	public BinaryMatrix getSubMatrix(BinaryVector rows,
			BinaryVector columns);

	public BinaryMatrix getSubRows(BinaryVector rows);

	public BinaryMatrix getSubColumns(BinaryVector columns);

	public void transpose();

	public BinaryMatrix clone();

}
