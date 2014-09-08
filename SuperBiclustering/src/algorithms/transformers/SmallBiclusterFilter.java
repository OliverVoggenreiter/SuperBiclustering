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
 * Removes biclusters that are do not match the minimum number of
 * rows and columns.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class SmallBiclusterFilter extends TransformerBlock {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SmallBiclusterFilter.class);

	private int minRows;
	private int minColumns;
	private int coreCount;
	private int minArea;

	public SmallBiclusterFilter(int cores, int minRows,
			int minColumns, int minArea) {
		this.coreCount = cores;
		this.minRows = minRows;
		this.minColumns = minColumns;
		this.minArea = minArea;
	}

	public SmallBiclusterFilter(int cores, int minRows,
			int minColumns) {
		this(cores, minRows, minColumns, minRows * minColumns);
	}

	@Override
	public String getName() {
		return String
				.format("Small Bicluster Filter - Min Rows:%1d - Min Columns:%1d",
						minRows, minColumns);
	}

	@Override
	public String getShortName() {
		return String.format("SBF%1dx%1d", minRows, minColumns);
	}

	@Override
	public Collection<? extends Bicluster> findBlocks(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {

		LOGGER.info("####### Small Bicluster Filter #######");
		LOGGER.info("Biclusters before filtering: "
				+ biclusters.size());

		MapController.setThreadCount(coreCount);

		Collection<? extends Bicluster> superBiclusters =
				MapController.execute(
						new SmallBiclusterRemovalAlgorithm(minRows,
								minColumns, minArea), biclusters);

		LOGGER.info("Biclusters after filtering: "
				+ superBiclusters.size());
		LOGGER.info("######################################");

		return superBiclusters;
	}

}
