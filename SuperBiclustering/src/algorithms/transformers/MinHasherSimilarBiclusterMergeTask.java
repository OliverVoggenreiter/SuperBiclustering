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
import java.util.Iterator;
import java.util.List;

import map.MapAlgorithm;
import util.BiclusterUtils;
import algorithms.generators.HashSetBicluster;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

/**
 * This MapAlgorithm takes many lists of biclusters as input and
 * simply merges all biclusters in each list into one
 * super-bicluster. If the density of that super-bicluster meets the
 * minimum, it is passed on to the output.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class MinHasherSimilarBiclusterMergeTask implements
MapAlgorithm<List<Bicluster>, Bicluster> {

	private float minDensity;
	private BinaryMatrix matrix;

	public MinHasherSimilarBiclusterMergeTask(float minDensity,
			BinaryMatrix matrix) {
		this.minDensity = minDensity;
		this.matrix = matrix;
	}

	@Override
	public List<Bicluster> map(List<List<Bicluster>> keyValuePairs) {
		List<Bicluster> tuples = new ArrayList<Bicluster>();

		for (List<Bicluster> biclusterSet : keyValuePairs) {
			Bicluster superBicluster = new HashSetBicluster();
			for (Bicluster bicluster : biclusterSet) {
				Iterator<Integer> rowIterator =
						bicluster.getRowIterator();
				while (rowIterator.hasNext()) {
					superBicluster.addRow(rowIterator.next());
				}
				Iterator<Integer> columnIterator =
						bicluster.getColumnIterator();
				while (columnIterator.hasNext()) {
					superBicluster.addColumn(columnIterator.next());
				}
			}
			if (BiclusterUtils.getDensity(superBicluster, matrix) >= minDensity) {
				tuples.add(superBicluster);
			}
		}
		return tuples;
	}
}
