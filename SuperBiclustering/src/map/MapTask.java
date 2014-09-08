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

package map;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * MapTask allows for MapAlgorithms and Lists of input to be prepared
 * so that it may be run at any time afterwards by a thread.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 5, 2013
 *
 * @param <K>
 *            - The type of the List that is given as input to the
 *            MapAlgorithm. (e.g. List< K >)
 * @param <V>
 *            - The type of the output returned by the given
 *            MapAlgorithm.
 */
public class MapTask<K, V> implements Callable<List<V>> {

	private final MapAlgorithm<K, V> algorithm;
	private final List<K> input;

	public MapTask(MapAlgorithm<K, V> mapAlgorithm, List<K> mapInput) {

		if (mapAlgorithm == null) {
			throw new NullPointerException(
					"Null passed as MapAlgorithm!");
		}
		if (mapInput == null) {
			throw new NullPointerException("Null passed as Input");
		}
		if (mapInput.size() == 0) {
			throw new IllegalArgumentException(
					"Empty input passed to MapTask!");
		}

		this.algorithm = mapAlgorithm;
		this.input = mapInput;
	}

	@Override
	public List<V> call() throws Exception {
		return this.algorithm.map(this.input);
	}

}
