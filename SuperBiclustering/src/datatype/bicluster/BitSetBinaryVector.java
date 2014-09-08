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

import java.util.BitSet;
import java.util.Iterator;

/**
 * A BitSet implementation of a growing-on-demand binary vector.
 */
@SuppressWarnings("serial")
public class BitSetBinaryVector implements BinaryVector {

	private static class BitSetIterator implements Iterator<Integer> {

		private final BitSetBinaryVector binaryVector;
		private int index = 0;

		public BitSetIterator(BitSetBinaryVector binaryVector) {
			this.binaryVector = binaryVector;
		}

		@Override
		public boolean hasNext() {
			return (binaryVector.data.nextSetBit(index) != -1);
		}

		@Override
		public Integer next() {
			int nextSetBit = binaryVector.data.nextSetBit(index);
			index = nextSetBit + 1;
			return nextSetBit;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private BitSet data;

	public BitSetBinaryVector() {
		data = new BitSet();
	}

	public BitSetBinaryVector(int capacity) {
		data = new BitSet(capacity);
	}

	public BitSetBinaryVector(int capacity, boolean value) {
		data = new BitSet(capacity);
		if (value) {
			data.flip(0, capacity);
		}
	}

	// private constructor, used for efficient cloning
	private BitSetBinaryVector(BitSet data) {
		this.data = data;
	}

	@Override
	public int cardinality() {
		return data.cardinality();
	}

	@Override
	public boolean get(int index) {
		return data.get(index);
	}

	@Override
	public void set(int index, boolean value) {
		data.set(index, value);
	}

	@Override
	public void set(int index) {
		this.set(index, true);
	}

	@Override
	public Iterator<Integer> iterator() {
		return new BitSetIterator(this);
	}

	@Override
	public BitSet getBitSet() {
		return data;
	}

	@Override
	public void union(BinaryVector other) {
		data.or(other.getBitSet());
	}

	@Override
	public void intersection(BinaryVector other) {
		data.and(other.getBitSet());
	}

	@Override
	public void andNot(BinaryVector other) {
		data.andNot(other.getBitSet());
	}

	@Override
	public BitSetBinaryVector clone() {
		BitSet clonedData = (BitSet) data.clone();
		return new BitSetBinaryVector(clonedData);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
				prime * result
				+ ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitSetBinaryVector other = (BitSetBinaryVector) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BitSetBinaryVector [data=" + data + "]";
	}

}
