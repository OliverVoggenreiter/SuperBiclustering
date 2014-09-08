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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatype.matrix.BinaryMatrix;

public class GeneratorBlockUtils {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GeneratorBlockUtils.class);

	public static boolean isLegalInput(BinaryMatrix matrix) {

		if (matrix == null) {
			LOGGER.warn("Null Matrix passed to Generator!");
			return false;
		}
		if (matrix.getDensity() == 0) {
			LOGGER.warn("Empty Matrix passed to Generator!");
			return false;
		}

		return true;
	}

}
