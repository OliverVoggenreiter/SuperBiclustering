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

package algorithms;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class TransformerBlockUtils {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TransformerBlockUtils.class);

	public static boolean isLegalInput(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {

		if (!GeneratorBlockUtils.isLegalInput(matrix)) {
			return false;
		}

		if (biclusters == null) {
			LOGGER.warn("Null biclusters passed to transformer!");
			return false;
		}

		if (biclusters.size() == 0) {
			LOGGER.warn("Zero biclusters passed to transformer!");
			return false;
		}

		return true;
	}

}
