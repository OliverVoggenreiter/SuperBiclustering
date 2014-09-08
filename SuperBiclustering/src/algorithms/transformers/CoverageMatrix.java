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

package algorithms.transformers;

import java.util.Collection;

import datatype.bicluster.Bicluster;
import datatype.matrix.BasicFloatMatrix;
import datatype.matrix.BinaryMatrix;

@SuppressWarnings("serial")
public class CoverageMatrix extends BasicFloatMatrix {

	public CoverageMatrix(int numRows, int numColumns) {
		super(numRows, numColumns);
	}

	public CoverageMatrix(int numRows, int numColumns, float[][] data) {
		super(numRows, numColumns, data);
	}

	public CoverageMatrix(int numRows, int numColumns,
			Collection<? extends Bicluster> biclusters) {
		super(numRows, numColumns);
		addOnes(biclusters);
	}

	public void addOne(int row, int column) {
		data[row][column]++;
	}

	public void removeOne(int row, int column) {
		data[row][column]--;
	}

	public void removeOnes(Bicluster bicluster) {
		for (int row : bicluster.getRows()) {
			for (int column : bicluster.getColumns()) {
				removeOne(row, column);
			}
		}
	}

	public void addOnes(Bicluster bicluster) {
		for (int row : bicluster.getRows()) {
			for (int column : bicluster.getColumns()) {
				addOne(row, column);
			}
		}
	}

	public void addOnes(Collection<? extends Bicluster> biclusters) {
		for (Bicluster bicluster : biclusters) {
			addOnes(bicluster);
		}
	}

	public int computeSignificantOnesCount(Bicluster bicluster,
			BinaryMatrix matrix) {
		int significantOnes = 0;
		for (int row : bicluster.getRows()) {
			for (int column : bicluster.getColumns()) {
				if (this.get(row, column) == 1
						&& matrix.get(row, column)) {
					significantOnes++;
				}
			}
		}
		return significantOnes;
	}
}
