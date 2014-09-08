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
import java.util.Collection;
import java.util.List;

import map.KeyValuePair;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;
import datatype.matrix.BitSetBinaryMatrix;

/**
 * SetCoverGroup is a data structure designed to create a set-cover
 * of biclusters on-the-fly. As new biclusters are added to the
 * group, the coverage of the binary matrix is updated in order to
 * determine if future biclusters will join the set or not. Finally,
 * all stored biclusters may be retrieved as a list.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class SetCoverGroup {
	List<Bicluster> coverGroup;
	BinaryMatrix leftToCover;
	BinaryMatrix matrix;

	public SetCoverGroup(BinaryMatrix matrix) {
		coverGroup = new ArrayList<Bicluster>();
		leftToCover =
				new BitSetBinaryMatrix(matrix.getNumRows(), matrix
						.getNumColumns());
		this.matrix = matrix;
	}

	/**
	 * Determines if all of the ones of the bicluster are covered by
	 * the set-cover group.
	 */
	public boolean covered(Bicluster bicluster) {
		for (int row : bicluster.getRows()) {
			for (int column : bicluster.getColumns()) {
				if (!leftToCover.get(row, column)
						&& matrix.get(row, column)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Adds the bicluster to the set-cover group if it is not already
	 * covered by the set-cover group.
	 */
	public boolean add(Bicluster bicluster) {
		if (!covered(bicluster)) {
			coverGroup.add(bicluster);
			setAll(bicluster);
			return true;
		}
		return false;
	}

	/**
	 * Adds all biclusters in the list to the set cover group in
	 * order if they are not already covered.
	 */
	public void addAll(
			List<KeyValuePair<Integer, Bicluster>> denseBlocks) {
		for (KeyValuePair<Integer, Bicluster> bicluster : denseBlocks) {
			this.add(bicluster.getValue());
		}
	}

	/**
	 * Adds all biclusters in the list to the set cover group in
	 * order if they are not already covered.
	 */
	public void addAll(Collection<Bicluster> denseBlocks) {
		for (Bicluster bicluster : denseBlocks) {
			this.add(bicluster);
		}
	}

	private void setAll(Bicluster bicluster) {
		for (int row : bicluster.getRows()) {
			for (int column : bicluster.getColumns()) {
				leftToCover.set(row, column);
			}
		}
	}

	/**
	 * Returns all biclusters in the set-cover group as a list.
	 */
	public List<Bicluster> getCoverGroup() {
		return coverGroup;
	}

	public int size() {
		return coverGroup.size();
	}
}
