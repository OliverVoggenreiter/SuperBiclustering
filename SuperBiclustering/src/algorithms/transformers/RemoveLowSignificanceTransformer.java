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
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.BiclusterUtils;
import algorithms.TransformerBlock;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;
import datatype.matrix.BitSetBinaryMatrix;

public class RemoveLowSignificanceTransformer extends
TransformerBlock {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RemoveLowSignificanceTransformer.class);

	private float minSignificance;
	private boolean negCoExp = false;

	public RemoveLowSignificanceTransformer(
			float minSignificanceTreshold) {
		this.minSignificance = minSignificanceTreshold;
	}

	@Override
	public String getName() {
		return String
				.format("Remove Low Significance Biclusters Transformer - Min Significance:%.2f",
						minSignificance);
	}

	@Override
	public String getShortName() {
		return String.format("RLST%.2f", minSignificance);
	}

	@Override
	public Collection<? extends Bicluster> findBlocks(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {
		LOGGER.info("#### Remove Low Significance ####");
		LOGGER.info(String.format("Starting with %d biclusters.",
				biclusters.size()));

		List<Bicluster> superBiclusters = new ArrayList<Bicluster>();

		// Generate the sorted list of biclusters from smallest to
		// largest.
		List<? extends Bicluster> sortedBiclusters =
				BiclusterUtils.sortByArea(biclusters);
		Collections.reverse(sortedBiclusters);

		if (negCoExp) {
			matrix = getNormalMatrix(matrix);
		}

		CoverageMatrix coverage =
				new CoverageMatrix(matrix.getNumRows(), matrix
						.getNumColumns(), biclusters);

		for (Bicluster bicluster : sortedBiclusters) {
			int significantOnes =
					coverage.computeSignificantOnesCount(bicluster,
							matrix);

			if (significantOnes >= minSignificance
					* BiclusterUtils.getArea(bicluster)) {
				superBiclusters.add(bicluster);
			} else {
				coverage.removeOnes(bicluster);
			}
		}

		LOGGER.info(String.format("Finished with %d biclusters.",
				superBiclusters.size()));
		LOGGER.info("#################################");

		return superBiclusters;
	}

	public RemoveLowSignificanceTransformer negativeCoExpression(
			boolean negCoExp) {
		this.negCoExp = negCoExp;
		return this;
	}

	private BinaryMatrix getNormalMatrix(BinaryMatrix matrix) {
		BinaryMatrix normMatrix =
				new BitSetBinaryMatrix(matrix.getNumRows() / 2,
						matrix.getNumColumns() / 2);

		for (int i = 0; i < normMatrix.getNumRows(); i++) {
			for (int j = 0; j < normMatrix.getNumColumns(); j++) {
				if (matrix.get(i, j)) {
					normMatrix.set(i, j);
				}
				if (matrix.get(i, j + (matrix.getNumColumns() / 2))) {
					normMatrix.set(i, j);
				}
			}
		}

		return normMatrix;
	}
}
