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

package algorithms.generators;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algorithms.GeneratorBlock;
import algorithms.bronkerbosch.RestrictedBronKerboschBipartiteV3;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class RestrictedBiclusterGenerator extends GeneratorBlock {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RestrictedBiclusterGenerator.class);

	private int maxLevel;
	private int minRows;
	private int minCols;

	public RestrictedBiclusterGenerator(int maxLevel, int minRows,
			int minColumns) {
		this.maxLevel = maxLevel;
		this.minRows = minRows;
		this.minCols = minColumns;
	}

	@Override
	public String getName() {
		return String.format("Restricted Bicluster Generator");
	}

	@Override
	public String getShortName() {
		return String.format("RBG");
	}

	@Override
	protected Collection<? extends Bicluster> findBlocks(
			BinaryMatrix matrix) {
		LOGGER.debug("### Restricted Bicluster Generator ###");
		RestrictedBronKerboschBipartiteV3 restrictedBronKerboschBipartiteV3 =
				new RestrictedBronKerboschBipartiteV3(maxLevel);
		restrictedBronKerboschBipartiteV3.setMinRows(minRows);
		restrictedBronKerboschBipartiteV3.setMinColumns(minCols);
		List<Bicluster> biclusters =
				restrictedBronKerboschBipartiteV3
				.findBiclusters(matrix);
		LOGGER.debug(String.format("Found %1d biclusters.",
				biclusters.size()));
		LOGGER.debug("######################################");
		return biclusters;
	}

}
