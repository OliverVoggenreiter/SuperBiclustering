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

import java.util.ArrayList;
import java.util.List;

import map.MapAlgorithm;
import datatype.bicluster.Bicluster;
import datatype.bicluster.BinaryVector;
import datatype.bicluster.BitSetBicluster;
import datatype.matrix.BinaryMatrix;

/**
 * MapAlgorithm that takes a block and increases its density by
 * systematically removing the lowest density row or column
 * recursively until the minimum density threshold is reached or
 * there are no more rows/columns left to remove.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class IncreaseDensityAlgorithm implements
MapAlgorithm<Bicluster, Bicluster> {

	private float minRowDensity;
	private float minColumnDensity;
	private BinaryMatrix matrix;

	public IncreaseDensityAlgorithm(float minRowDensity,
			float minColumnDensity, BinaryMatrix matrix) {
		this.minRowDensity = minRowDensity;
		this.minColumnDensity = minColumnDensity;
		this.matrix = matrix;
	}

	@Override
	public List<Bicluster> map(List<Bicluster> keyValuePairs) {
		List<Bicluster> tuples = new ArrayList<Bicluster>();

		for (Bicluster bicluster : keyValuePairs) {
			BinaryVector rows = bicluster.getRows().clone();
			BinaryVector columns = bicluster.getColumns().clone();
			boolean somethingRemoved = true;
			boolean rowTime = true;
			while (somethingRemoved) {
				double[] worst_row_density =
						new double[rows.cardinality()];
				double[] worst_column_density =
						new double[columns.cardinality()];

				int row_index = 0;
				for (int row : rows) {
					int column_index = 0;
					for (int column : columns) {
						if (matrix.get(row, column)) {
							worst_row_density[row_index]++;
							worst_column_density[column_index]++;
						}
						column_index++;
					}
					row_index++;
				}

				somethingRemoved = false;

				if (rowTime) {
					rowTime = !rowTime;
					somethingRemoved |=
							remove(rows, columns.cardinality(),
									worst_row_density, minRowDensity);
					if (!somethingRemoved) {
						somethingRemoved |=
								remove(columns, rows.cardinality(),
										worst_column_density,
										minColumnDensity);
					}
				} else {
					rowTime = !rowTime;
					somethingRemoved |=
							remove(columns, rows.cardinality(),
									worst_column_density,
									minColumnDensity);
					if (!somethingRemoved) {
						somethingRemoved |=
								remove(rows, columns.cardinality(),
										worst_row_density,
										minRowDensity);
					}
				}
			}
			if (rows.cardinality() > 0 && columns.cardinality() > 0) {
				Bicluster newSuperBicluster = new BitSetBicluster();
				newSuperBicluster.addRows(rows);
				newSuperBicluster.addColumns(columns);
				tuples.add(newSuperBicluster);
			}
		}

		return tuples;
	}

	private boolean remove(BinaryVector vector, int opSize,
			double[] worst_vector_density, float minDensity) {
		int index = 0;
		boolean somethingRemoved = false;
		for (int element : vector) {
			if (worst_vector_density[index] / opSize < minDensity) {
				somethingRemoved = true;
				vector.set(element, false);
			}
			index++;
		}
		return somethingRemoved;
	}

}
