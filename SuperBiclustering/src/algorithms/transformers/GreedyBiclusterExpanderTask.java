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

import java.util.List;

import map.MapAlgorithm;
import util.BiclusterUtils;
import datatype.bicluster.Bicluster;
import datatype.bicluster.BinaryVector;
import datatype.bicluster.BitSetBinaryVector;
import datatype.matrix.BinaryMatrix;

/**
 * The GreedyBiclusterExpanderTask takes a block and searches for
 * rows in the matrix that are not part of the bicluster that contain
 * at least minDensity columns compared to the biclusters already
 * present rows. Subsequently it does the same with columns. This is
 * not recursively called, in order to minimize accidental explosions
 * in bicluster size.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 12, 2013
 *
 */
public class GreedyBiclusterExpanderTask implements
MapAlgorithm<Bicluster, Bicluster> {

	private float minDensity;
	private int iterationCount;
	private BinaryMatrix matrix;

	public GreedyBiclusterExpanderTask(float minDensity,
			int iterationCount, BinaryMatrix matrix) {
		this.minDensity = minDensity;
		this.iterationCount = iterationCount;
		this.matrix = matrix;
	}

	@Override
	public List<Bicluster> map(List<Bicluster> biclusters) {

		for (Bicluster bicluster : biclusters) {
			for (int i = 0; i < iterationCount; i++) {
				addBestRowsAndColumns(bicluster, matrix);
			}
		}

		return biclusters;
	}

	private void addBestRowsAndColumns(Bicluster bicluster,
			BinaryMatrix matrix) {
		BinaryVector newRows = new BitSetBinaryVector();
		for (int i = 0; i < matrix.getNumRows(); i++) {
			if (!bicluster.containsRow(i)
					&& BiclusterUtils.getDensity(i, bicluster
							.getColumns(), matrix) >= minDensity) {
				newRows.set(i);
			}
		}

		BinaryVector newColumns = new BitSetBinaryVector();
		for (int i = 0; i < matrix.getNumColumns(); i++) {
			if (!bicluster.containsColumn(i)
					&& BiclusterUtils.getDensity(
							bicluster.getRows(), i, matrix) >= minDensity) {
				newColumns.set(i);
			}
		}

		bicluster.addRows(newRows);
		bicluster.addColumns(newColumns);

	}

}
