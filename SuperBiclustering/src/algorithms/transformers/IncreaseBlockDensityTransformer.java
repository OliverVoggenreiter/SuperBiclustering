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

import java.util.Collection;

import map.MapController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algorithms.TransformerBlock;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

/**
 * Takes a set of biclusters and attempts to greedily increase their
 * density by recursively removing the worst row or column until the
 * density requirement is met.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class IncreaseBlockDensityTransformer extends
TransformerBlock {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(IncreaseBlockDensityTransformer.class);
	private float minRowDensity;
	private float minColumnDensity;
	private int coreCount;

	public IncreaseBlockDensityTransformer(int cores,
			float minRowDensity, float minColumnDensity) {
		this.minRowDensity = minRowDensity;
		this.minColumnDensity = minColumnDensity;
		this.coreCount = cores;
		LOGGER.info(String
				.format("Created Increase Block Density Transformer with min-allowed density of %.1f%%.",
						this.minRowDensity * 100.0f));
	}

	@Override
	public String getName() {
		return "Increase Block Density Transformer";
	}

	@Override
	public String getShortName() {
		return String.format("IBDT%.2f-%.2f", minRowDensity,
				minColumnDensity);
	}

	@Override
	public Collection<? extends Bicluster> findBlocks(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {
		LOGGER.info("######## Increase Block Density Transformer ########");

		MapController.setThreadCount(coreCount);

		Collection<? extends Bicluster> superbiclusters =
				MapController.execute(new IncreaseDensityAlgorithm(
						minRowDensity, minColumnDensity, matrix),
						biclusters);

		LOGGER.info("####################################################");

		return superbiclusters;
	}

}
