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
 * This Transformer takes a set of input blocks and greedily expands
 * each of them by simply adding rows and columns that are above the
 * minDensity threshold to that block. This is not done recursively!
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 12, 2013
 *
 */
public class GreedyExpanderTransformer extends TransformerBlock {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GreedyExpanderTransformer.class);
	private float minDensity;
	private int repeatCount;
	private int coreCount;

	public GreedyExpanderTransformer(int coreCount,
			float minDensity, int repeatCount) {
		this.coreCount = coreCount;
		this.minDensity = minDensity;
		this.repeatCount = repeatCount;
		LOGGER.info(String
				.format("Created Greedy Expander Transformer with min allowed density of %.1f%%.",
						this.minDensity * 100.0f));
	}

	@Override
	public String getName() {
		return String.format("Greedy Expander - Min Density:%.2f",
				minDensity);
	}

	@Override
	public String getShortName() {
		return String.format("GET%.2f", minDensity);
	}

	@Override
	protected Collection<? extends Bicluster> findBlocks(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {
		LOGGER.info("#### Greedy Expander Transformer ####");
		MapController.setThreadCount(coreCount);

		Collection<? extends Bicluster> superBiclusters =
				MapController.execute(
						new GreedyBiclusterExpanderTask(minDensity,
								repeatCount, matrix), biclusters);

		LOGGER.info("#####################################");
		return superBiclusters;
	}

}
