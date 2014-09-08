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

package map;

/**
 * Provides a generic implementation of a Key-Value Pair. This can be
 * used for any application where a tuple may be required. Provides
 * only the basic creation and getter functionality. This
 * implementation does not allow you to change the key or value after
 * creation.
 *
 * IMPORTANT: The hashCode for an instance of KeyValuePair is
 * actually the hashCode of the key of that instance.
 *
 * @author Oliver Voggenreiter
 * @date Wednesday, March 07, 2012
 *
 */
public class KeyValuePair<K, V> {
	private K key;
	private V value;

	public KeyValuePair(K key, V value) {
		this.setKey(key);
		this.setValue(value);
	}

	public K getKey() {
		return key;
	}

	private void setKey(K key) {
		if (key == null)
			throw new NullPointerException(
					"Cannot use null as key in KeyValuePair!");
		this.key = key;
	}

	public V getValue() {
		return value;
	}

	private void setValue(V value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object other) {

		if (this == other)
			return true;
		if (!(other instanceof KeyValuePair<?, ?>))
			return false;

		KeyValuePair<?, ?> that = (KeyValuePair<?, ?>) other;

		if (!this.key.equals(that.key))
			return false;
		if (!this.value.equals(that.value))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", key, value);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

}