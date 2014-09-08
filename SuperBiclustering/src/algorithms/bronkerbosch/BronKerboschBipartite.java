/* Bimax 2 - A biclustering algorithm based on the Bron-Kerbosch
 * Maximal Clique Enumeration Algorithm.
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

package algorithms.bronkerbosch;

import java.util.BitSet;
import java.util.List;

import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public interface BronKerboschBipartite {

	public abstract List<Bicluster> findBiclusters(
			BinaryMatrix connectivityMatrix);

	public abstract List<Bicluster> findBiclusters(
			BinaryMatrix connectivityMatrix, BitSet requiredRows,
			BitSet requiredCols);

	public abstract void setMinRows(int minRows);

	public abstract void setMinColumns(int minColumns);

	public abstract void setMaxBiclusters(int maxBiclusters);

}