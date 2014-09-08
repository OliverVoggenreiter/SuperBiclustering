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
import java.util.List;

import map.MapController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algorithms.TransformerBlock;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class GreedyBlockSplitterTransformer extends TransformerBlock {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GreedyBlockSplitterTransformer.class);

	private int coreCount = 1;
	private float minDensity = 0.9f;
	private int minSize = 10;

	public GreedyBlockSplitterTransformer setCoreCount(int cores) {
		this.coreCount = cores;
		return this;
	}

	public GreedyBlockSplitterTransformer
	setMinDensity(float density) {
		this.minDensity = density;
		return this;
	}

	public GreedyBlockSplitterTransformer setMinSize(int size) {
		this.minSize = size;
		return this;
	}

	@Override
	public String getName() {
		return String.format("Greedy Block Splitter Transformer");
	}

	@Override
	public String getShortName() {
		return String.format("GBST");
	}

	@Override
	protected Collection<? extends Bicluster> findBlocks(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {

		LOGGER.info("##### Greedy Block Splitter Transformer ####");

		MapController.setThreadCount(coreCount);

		GreedySplitTask task = new GreedySplitTask();
		task.setMinDensity(minDensity);
		task.setMinSize(minSize);
		task.setMatrix(matrix);

		List<Bicluster> denseBiclusters =
				MapController.execute(task, biclusters);

		LOGGER.info("############################################");

		return denseBiclusters;
	}

}
