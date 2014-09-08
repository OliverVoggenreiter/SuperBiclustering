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
import java.util.Map;
import java.util.Set;

import map.KeyValuePair;
import map.MapAlgorithm;
import datatype.bicluster.Bicluster;

/**
 * A simple datatype converter MapAlgorithm, it simply strips the
 * lists of biclusters generated in the previous min-hashing step
 * from the corresponding data structure.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class MinHasherSimilarBiclusterExtractTask
implements
MapAlgorithm<KeyValuePair<Integer, Map<Set<Integer>, List<Bicluster>>>, List<Bicluster>> {

	@Override
	public
	List<List<Bicluster>>
	map(List<KeyValuePair<Integer, Map<Set<Integer>, List<Bicluster>>>> keyValuePairs) {
		List<List<Bicluster>> tuples =
				new ArrayList<List<Bicluster>>();

		for (KeyValuePair<Integer, Map<Set<Integer>, List<Bicluster>>> keyValuePair : keyValuePairs) {
			for (List<Bicluster> bics : keyValuePair.getValue()
					.values()) {
				tuples.add(bics);
			}
		}

		return tuples;
	}

}
