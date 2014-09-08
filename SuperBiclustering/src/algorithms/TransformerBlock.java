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

import java.util.ArrayList;
import java.util.Collection;

import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public abstract class TransformerBlock implements Block {

	/**
	 * Method should take a collection of biclusters and transform
	 * them into a new set of biclusters. The input collection should
	 * NOT be altered; however, the generated collection may contain
	 * Bicluster instances that were part of the input collection.
	 */
	public Collection<? extends Bicluster> transformBiclusters(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {
		if (!TransformerBlockUtils.isLegalInput(biclusters, matrix)) {
			return new ArrayList<Bicluster>();
		}

		return findBlocks(biclusters, matrix);
	}

	protected abstract Collection<? extends Bicluster> findBlocks(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix);

}
