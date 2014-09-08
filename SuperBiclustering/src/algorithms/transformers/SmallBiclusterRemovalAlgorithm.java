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
import util.BiclusterUtils;
import datatype.bicluster.Bicluster;

/**
 * Takes a block and compares its size against a threshold. If it is
 * smaller, it is dropped, otherwise it is passed on to the output.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class SmallBiclusterRemovalAlgorithm implements
MapAlgorithm<Bicluster, Bicluster> {

	private int minRows;
	private int minColumns;
	private int minArea;

	public SmallBiclusterRemovalAlgorithm(int min_rows,
			int min_columns, int minArea) {
		this.minRows = min_rows;
		this.minColumns = min_columns;
		this.minArea = minArea;
	}

	@Override
	public List<Bicluster> map(List<Bicluster> biclusters) {
		List<Bicluster> bigBiclusters = new ArrayList<Bicluster>();

		for (Bicluster bicluster : biclusters) {
			if (BiclusterUtils.hasMinDimensions(bicluster, minRows,
					minColumns)
					&& BiclusterUtils.getArea(bicluster) >= minArea) {
				bigBiclusters.add(bicluster);
			}
		}

		return bigBiclusters;
	}

}
