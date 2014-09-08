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
import algorithms.bronkerbosch.BronKerboschBipartiteV3;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class BiclusterGenerator extends GeneratorBlock {

	private static final Logger LOGGER = LoggerFactory.getLogger(BiclusterGenerator.class);
	private int minRow;
	private int minColumn;

	public BiclusterGenerator(int minRows, int minColumns) {
		this.minRow = minRows;
		this.minColumn = minColumns;
		LOGGER.info(String.format("Created Bicluster Generator with minimum row/col of %1d/%1d.", minRow, minColumn));
	}

	@Override
	public String getName() {
		return String.format("Bicluster Generator - Min Rows: %1d - Min Columns: %1d", minRow, minColumn);
	}

	@Override
	public String getShortName() {
		return String.format("BCG%1dx%1d", minRow, minColumn);
	}

	@Override
	protected Collection<? extends Bicluster> findBlocks(BinaryMatrix matrix) {
		BronKerboschBipartiteV3 bronKerboschBipartiteV3 =
				new BronKerboschBipartiteV3();
		bronKerboschBipartiteV3.setMinRows(minRow);
		bronKerboschBipartiteV3.setMinColumns(minColumn);
		return bronKerboschBipartiteV3.findBiclusters(matrix);
	}

}
