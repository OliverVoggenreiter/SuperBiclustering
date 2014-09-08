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

package datatype.bicluster;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Iterator;

/**
 * A growing-on-demand binary vector.
 */
public interface BinaryVector extends Serializable, Cloneable,
Iterable<Integer> {

	/** Returns the number of bits set to true in this vector. */
	public int cardinality();

	public boolean get(int index);

	public void set(int index, boolean value);

	public void set(int index);

	/** Returns an iterator over the indices of set bits. */
	@Override
	public Iterator<Integer> iterator();

	public BitSet getBitSet();

	public void union(BinaryVector other);

	public void intersection(BinaryVector other);

	public void andNot(BinaryVector other);

	public BinaryVector clone();
}
