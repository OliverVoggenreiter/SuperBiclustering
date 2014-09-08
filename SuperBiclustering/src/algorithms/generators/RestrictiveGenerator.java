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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algorithms.GeneratorBlock;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

/**
 * The Restrictive Generator can create sets of rows/columns as blocks whereby it is guaranteed that the degeneracy of each row/column in
 * such a block meets the minimum threshold of degeneracy. This is an excellent way in order to reduce the problem space of biclustering and
 * provide a set of blocks whose union still contains all biclusters greater in size than the minimum degeneracy requirement while
 * eliminating many noisy ones in the matrix. In addtion, since many blocks are created, the problem is automatically split into independent
 * chunks for processing. It inadvertantly also provides coverage of true bicluster ones in the order of the number of biclusters that each
 * one is participating in.
 *
 * This class is a wrapper for the actual algorithm in order to meet the requirements of the GeneratorBlock interface. For the logic of the
 * algorithm, please see RestrictiveMatrixDegeneracy.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 7, 2013
 *
 */
public class RestrictiveGenerator extends GeneratorBlock {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestrictiveGenerator.class);

	private int minDegeneracy;

	public RestrictiveGenerator(int minDegeneracy) {
		this.minDegeneracy = minDegeneracy;
	}

	@Override
	public String getName() {
		return String.format("RestrictiveGenerator-MinOnes%1d", minDegeneracy);
	}

	@Override
	public String getShortName() {
		return String.format("RG%1d", minDegeneracy);
	}

	@Override
	protected Collection<? extends Bicluster> findBlocks(BinaryMatrix matrix) {
		LOGGER.debug("######## Restrictive Generator ########");

		Collection<? extends Bicluster> superBiclusters = RestrictiveMatrixDegeneracy.computeDegenerateBlocks(matrix, minDegeneracy);

		LOGGER.info(String.format("Created %1d Degenerate Blocks.", superBiclusters.size()));

		LOGGER.debug("#######################################");

		return superBiclusters;
	}

}
