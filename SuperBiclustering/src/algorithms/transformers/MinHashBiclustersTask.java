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

import map.KeyValuePair;
import map.MapAlgorithm;
import util.RandomHashFunction;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

/**
 * Calculates the min-hash values for each biclusters indices. This
 * is done using RandomHashFunction and a union of any of the three
 * types of indices.
 *
 * Internal Index: The row/col pairs of cells that are within the
 * biclusters.
 *
 * External Index: The row/col pairs of cells that are outside the
 * biclusters but share one row or column with the bicluster.
 *
 * Easy Index: The rows and columns of the bicluster.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class MinHashBiclustersTask
implements
MapAlgorithm<Bicluster, KeyValuePair<Integer, KeyValuePair<Bicluster, List<Integer>>>> {

	private List<RandomHashFunction> hashFunctions;
	private int bandCount;
	private BinaryMatrix MATRIX;

	private boolean useOverlapOnes = true;
	private boolean useCrossoverOnes = true;
	private boolean useRowsColumns = false;

	public MinHashBiclustersTask(
			List<RandomHashFunction> randomHashFunctions,
			int numberOfBands, BinaryMatrix matrix) {
		new MinHashBiclustersTask(randomHashFunctions,
				numberOfBands, matrix, true, true, false);
	}

	public MinHashBiclustersTask(
			List<RandomHashFunction> randomHashFunctions,
			int numberOfBands, BinaryMatrix matrix,
			boolean useOverlap, boolean useCrossover,
			boolean useRowsColumns) {
		this.hashFunctions = randomHashFunctions;
		this.bandCount = numberOfBands;
		this.MATRIX = matrix;
		this.useOverlapOnes = useOverlap;
		this.useCrossoverOnes = useCrossover;
		this.useRowsColumns = useRowsColumns;
	}

	@Override
	public
	List<KeyValuePair<Integer, KeyValuePair<Bicluster, List<Integer>>>>
	map(List<Bicluster> keyValuePairs) {
		List<KeyValuePair<Integer, KeyValuePair<Bicluster, List<Integer>>>> tuples =
				new ArrayList<KeyValuePair<Integer, KeyValuePair<Bicluster, List<Integer>>>>();

		for (Bicluster bicluster : keyValuePairs) {
			List<Integer> hashResults = new ArrayList<Integer>();
			List<Integer> indices = getIndices(bicluster);
			for (RandomHashFunction rhf : hashFunctions) {
				int lowestHash = Integer.MAX_VALUE;
				for (int index : indices) {
					if (rhf.getHash(index) < lowestHash) {
						lowestHash = rhf.getHash(index);
					}
				}
				hashResults.add(lowestHash);
			}

			List<KeyValuePair<Bicluster, List<Integer>>> bands =
					new ArrayList<KeyValuePair<Bicluster, List<Integer>>>();
			for (int i = 0; i < bandCount; i++) {
				bands.add(new KeyValuePair<Bicluster, List<Integer>>(
						bicluster, new ArrayList<Integer>()));
			}

			for (int i = 0; i < hashResults.size(); i++) {
				bands.get(i % this.bandCount).getValue().add(
						hashResults.get(i));
			}

			for (int i = 0; i < bandCount; i++) {
				tuples.add(new KeyValuePair<Integer, KeyValuePair<Bicluster, List<Integer>>>(
						i, bands.get(i)));
			}

		}
		return tuples;
	}

	private List<Integer> getIndices(Bicluster bicluster) {
		List<Integer> indices = new ArrayList<Integer>();
		if (useOverlapOnes)
			indices.addAll(getOverlapIndices(bicluster));
		if (useCrossoverOnes)
			indices.addAll(getCrossoverIndices(bicluster));
		if (useRowsColumns) {
			indices.addAll(getRowColumnIndices(bicluster));
		}
		return indices;
	}

	private List<Integer> getCrossoverIndices(Bicluster bicluster) {
		List<Integer> indices = new ArrayList<Integer>();

		for (int iRow = 0; iRow < MATRIX.getNumRows(); iRow++) {
			if (bicluster.containsRow(iRow))
				continue;
			int oneCount = 0;
			for (int column : bicluster.getColumns()) {
				if (MATRIX.get(iRow, column)) {
					oneCount++;
				}
			}
			if (oneCount > 0.8f * bicluster.getNumberOfColumns()) {
				for (int column : bicluster.getColumns()) {
					indices.add(column * MATRIX.getNumRows() + iRow);
				}
			}
		}
		for (int iColumn = 0; iColumn < MATRIX.getNumColumns(); iColumn++) {
			if (bicluster.containsColumn(iColumn))
				continue;
			int oneCount = 0;
			for (int row : bicluster.getRows()) {
				if (MATRIX.get(row, iColumn)) {
					oneCount++;
				}
			}
			if (oneCount > 0.8f * bicluster.getNumberOfRows()) {
				for (int row : bicluster.getRows()) {
					indices.add(iColumn * MATRIX.getNumRows() + row);
				}
			}
		}

		return indices;
	}

	private List<Integer> getOverlapIndices(Bicluster bicluster) {
		List<Integer> indices = new ArrayList<Integer>();

		for (int row : bicluster.getRows()) {
			for (int column : bicluster.getColumns()) {
				if (MATRIX.get(row, column)) {
					indices.add(column * MATRIX.getNumRows() + row);
				}
			}
		}

		return indices;
	}

	private List<Integer> getRowColumnIndices(Bicluster bicluster) {
		List<Integer> indices = new ArrayList<Integer>();

		for (int row : bicluster.getRows()) {
			indices.add(row);
		}
		for (int column : bicluster.getColumns()) {
			indices.add(MATRIX.getNumRows() + column);
		}

		return indices;
	}
}
