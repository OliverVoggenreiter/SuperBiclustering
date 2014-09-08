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

package util;

import java.util.Random;

/**
 * RandomHashFunction provides a way to create randomly hashing
 * functions with a seed. This is mainly used in min-hashing and
 * provides a simple way to reproducibly create random hash
 * functions.
 *
 * @author Oliver Voggenreiter
 * @date Friday, March 10, 2012
 *
 */
public class RandomHashFunction {

	// Used if no seed is provided (for reproducibility).
	private static final int randSeed = 696;

	// Max binCount is 2,147,483,647 (Integer.MAX_VALUE).
	private final int binCount;
	private final long a;
	private final long b;

	/**
	 * Default constructor that should only be used for convenience,
	 * however it will always use the same seed for the ranomd number
	 * generation.
	 */
	public RandomHashFunction(int targetSize) {
		this(randSeed, targetSize);
	}

	/**
	 * Initializes a random hash function that uses a specified seed
	 * for random number generation.
	 */
	public RandomHashFunction(int seed, int targetSize) {
		if (targetSize <= 1)
			throw new IllegalArgumentException();
		Random rand = new Random(seed);
		this.binCount = PrimeFinder.getNextPrime(targetSize);
		this.a = rand.nextInt(this.binCount);
		this.b = rand.nextInt(this.binCount);
	}

	/**
	 * Maps the hash of this 'key' to a fixed position in the
	 * possible bins defined by the RandomHashFunction itself.
	 */
	public <T> int getHash(T key) {
		return (int) ((a * Math.abs(key.hashCode()) + b) % binCount);
	}

	/**
	 * Gives the possible space of hashes (between 0 and the returned
	 * value);
	 */
	public int getNumberOfBins() {
		return this.binCount;
	}
}
