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

import map.MapController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algorithms.GeneratorBlock;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class BipartiteBronKerboschPlusGenerator extends GeneratorBlock {

	private static final Logger LOGGER = LoggerFactory.getLogger(BipartiteBronKerboschPlusGenerator.class);

	private int coreCount = 1;
	private int minSize = 5;

	public BipartiteBronKerboschPlusGenerator setCoreCount(int count) {
		this.coreCount = count;
		return this;
	}

	public BipartiteBronKerboschPlusGenerator setMinSize(int size) {
		this.minSize = size;
		return this;
	}

	@Override
	public String getName() {
		return "BBKP";
	}

	@Override
	public String getShortName() {
		return "Bipartite Bron Kerbosch Plus";
	}

	@Override
	protected Collection<? extends Bicluster> findBlocks(BinaryMatrix matrix) {

		LOGGER.debug("######## BronKerbosch Plus Generator #######");

		BBKTaskFactory.setMatrix(matrix);
		BBKTaskFactory.setMinSize(minSize);
		Collection<Integer> bbkTasks = BBKTaskFactory.getTasks();

		MapController.setThreadCount(coreCount);

		LOGGER.debug("Tasks created... starting work...");

		Collection<? extends Bicluster> superBiclusters = MapController.execute(new BBKPWorker(), bbkTasks);

		LOGGER.debug("############################################");

		return superBiclusters;

	}

}
