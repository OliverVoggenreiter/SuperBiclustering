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
import java.util.Comparator;
import java.util.List;

import map.KeyValuePair;
import map.MapController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algorithms.TransformerBlock;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

/**
 * The BlockSplitterTransformer takes a set of less dense blocks as
 * input and attempts to slice them in order to form denser smaller
 * blocks and filter out less dense subregions of the input blocks.
 * This is useful for generating blocks for input to a hashing as it
 * will be more likely that each block can be assembled into its true
 * superbicluster.
 *
 * Splitting is done iteratively. First, the rows and columns are
 * ordered by density. Then a split is chosen based on the regions
 * with higher density and a limit to ensure that splitting does not
 * take too long. Subsequently, each of the newly generated subblocks
 * are sent through the same procedure until they meet the density
 * criteria and are not too small.
 *
 * Once all the splitting has been done, blocks are sent through a
 * greedy set cover in order to reduce unnecessary duplicates or high
 * coverage.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class BlockSplitterTransformer extends TransformerBlock {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BlockSplitterTransformer.class);

	private float minDensity;
	private int minSize;
	private int coreCount;

	public BlockSplitterTransformer(int coreCount, int minSize,
			float minDensity) {
		this.coreCount = coreCount;
		this.minSize = minSize;
		this.minDensity = minDensity;
		LOGGER.info(String
				.format("Created Block Splitter Transformer"));
	}

	@Override
	public String getName() {
		return String
				.format("Block Splitter Transformer - Min Size:%1d - Min Density:%.2f",
						minSize, minDensity);
	}

	@Override
	public String getShortName() {
		return String.format("BST%1dx%1d-%.2f", minSize, minSize,
				minDensity);
	}

	@Override
	public Collection<? extends Bicluster> findBlocks(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {
		LOGGER.info("######## Block Splitter Transformer ########");

		// SetCoverGroup superBlocks = new SetCoverGroup(matrix);
		MapController.setThreadCount(coreCount);

		List<KeyValuePair<Integer, Bicluster>> denseBlocks =
				MapController.execute(new SplitTask(matrix, minSize,
						minDensity), biclusters);

		Collections.sort(denseBlocks, new LevelComparator());

		// superBlocks.addAll(denseBlocks);

		LOGGER.info("############################################");

		// return superBlocks.getCoverGroup();
		// TODO: Do we really need/want set cover? Perhaps it is best
		// to leave all the blocks in for better sampling?
		List<Bicluster> blocks = new ArrayList<Bicluster>();
		for (KeyValuePair<Integer, Bicluster> hit : denseBlocks) {
			blocks.add(hit.getValue());
		}
		return blocks;
	}

	private static class LevelComparator implements
	Comparator<KeyValuePair<Integer, Bicluster>> {

		@Override
		public int compare(KeyValuePair<Integer, Bicluster> o1,
				KeyValuePair<Integer, Bicluster> o2) {
			return o1.getKey() - o2.getKey();
		}

	}
}