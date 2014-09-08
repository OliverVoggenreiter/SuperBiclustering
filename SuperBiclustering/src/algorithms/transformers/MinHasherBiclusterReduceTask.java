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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import map.KeyValuePair;
import map.MapAlgorithm;
import datatype.bicluster.Bicluster;

/**
 * Takes a set of input blocks with their associated min-hashes and
 * maps these together by those same min-hashes. It subsequently
 * output the group data structure.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class MinHasherBiclusterReduceTask
implements
MapAlgorithm<KeyValuePair<Integer, KeyValuePair<Bicluster, List<Integer>>>, KeyValuePair<Integer, Map<Set<Integer>, List<Bicluster>>>> {

	@Override
	public
	List<KeyValuePair<Integer, Map<Set<Integer>, List<Bicluster>>>>
	map(List<KeyValuePair<Integer, KeyValuePair<Bicluster, List<Integer>>>> keyValuePairs) {
		List<KeyValuePair<Integer, Map<Set<Integer>, List<Bicluster>>>> tuples =
				new ArrayList<KeyValuePair<Integer, Map<Set<Integer>, List<Bicluster>>>>();

		Map<Integer, List<KeyValuePair<Bicluster, List<Integer>>>> bandMap =
				new HashMap<Integer, List<KeyValuePair<Bicluster, List<Integer>>>>();
		for (KeyValuePair<Integer, KeyValuePair<Bicluster, List<Integer>>> keyValuePair : keyValuePairs) {
			if (!bandMap.containsKey(keyValuePair.getKey())) {
				List<KeyValuePair<Bicluster, List<Integer>>> rowsInBand =
						new ArrayList<KeyValuePair<Bicluster, List<Integer>>>();
				bandMap.put(keyValuePair.getKey(), rowsInBand);
			}
			bandMap.get(keyValuePair.getKey()).add(
					keyValuePair.getValue());
		}

		for (Entry<Integer, List<KeyValuePair<Bicluster, List<Integer>>>> entry : bandMap
				.entrySet()) {
			Map<Set<Integer>, List<Bicluster>> bandHash =
					new HashMap<Set<Integer>, List<Bicluster>>();
			for (KeyValuePair<Bicluster, List<Integer>> keyValuePair : entry
					.getValue()) {
				Set<Integer> fingerprint = new HashSet<Integer>();
				for (Integer hash : keyValuePair.getValue()) {
					fingerprint.add(hash);
				}
				if (!bandHash.containsKey(fingerprint)) {
					bandHash.put(fingerprint,
							new ArrayList<Bicluster>());
				}
				bandHash.get(fingerprint).add(keyValuePair.getKey());
			}
			tuples.add(new KeyValuePair<Integer, Map<Set<Integer>, List<Bicluster>>>(
					entry.getKey(), bandHash));
		}

		return tuples;
	}

}
