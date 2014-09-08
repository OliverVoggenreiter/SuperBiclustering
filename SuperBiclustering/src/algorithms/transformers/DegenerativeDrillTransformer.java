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
import map.MapController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algorithms.TransformerBlock;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class DegenerativeDrillTransformer extends TransformerBlock {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DegenerativeDrillTransformer.class);

	private int coreCount;
	private int minRows;
	private int minColumns;
	private float minDensity;
	private boolean useRandomChoice;

	public DegenerativeDrillTransformer(int coreCount, int minRows,
			int minColumns, float minDensity, boolean useRandomChoice) {
		this.coreCount = coreCount;
		this.minRows = minRows;
		this.minColumns = minColumns;
		this.minDensity = minDensity;
		this.useRandomChoice = useRandomChoice;
		LOGGER.info(String
				.format("Created Degenerative Drill Transformer - Min Rows:%1d - Min Columns:%1d - Min Density:%.2f",
						minRows, minColumns, minDensity));
	}

	@Override
	public String getName() {
		return String.format("Degenerative Drill Transformer");
	}

	@Override
	public String getShortName() {
		return String.format("DDT%1dx%1d-%.2f", minRows, minColumns,
				minDensity);
	}

	@Override
	public Collection<? extends Bicluster> findBlocks(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {
		LOGGER.info("######## Degenerative Drill ########");

		List<Bicluster> superBiclusters = new ArrayList<Bicluster>();

		MapController.setThreadCount(coreCount);

		List<KeyValuePair<Integer, Bicluster>> blocks =
				new ArrayList<KeyValuePair<Integer, Bicluster>>();
		int counter = 0;
		for (Bicluster bicluster : biclusters) {
			blocks.add(new KeyValuePair<Integer, Bicluster>(counter,
					bicluster));
			counter++;
		}
		List<KeyValuePair<Integer, Bicluster>> greedyBlocks =
				MapController.execute(
						new GreedyDegenerativeBlockTask(minDensity,
								matrix, useRandomChoice), blocks);

		for (KeyValuePair<Integer, Bicluster> greedyBlock : greedyBlocks) {
			if (greedyBlock.getValue().getNumberOfRows() >= minRows
					&& greedyBlock.getValue().getNumberOfColumns() >= minColumns) {
				superBiclusters.add(greedyBlock.getValue());
			}
		}

		LOGGER.info("####################################");

		return superBiclusters;
	}

}
