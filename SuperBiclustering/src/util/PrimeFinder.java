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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PrimeFinder is a simple implementation of the Sieve of
 * Erastosthenes whereby primes are found by testing a number against
 * all previous numbers for divisibility. In this case, since we only
 * want to generate one prime, we make do with just exploring against
 * all previous numbers until a divisor is found or primality is
 * established.
 *
 * @author Oliver Voggenreiter
 * @date Friday, March 10, 2012
 *
 */
public class PrimeFinder {

	private static final Logger logger = LoggerFactory
			.getLogger(PrimeFinder.class);

	/**
	 * Generate the next highest prime number equal to or higher than
	 * 'query'.
	 */
	public static int getNextPrime(int query) {
		if (query < 0)
			throw new IllegalArgumentException(
					"Cannot ask for next prime of a negative value!");

		while (!isPrime(query)) {
			query++;
		}

		return query;
	}

	/**
	 * Checks whether the given number is prime. 0, 1, and negative
	 * numbers are assumed to be non-prime and return false.
	 */
	public static boolean isPrime(int query) {
		if (query <= 1)
			return false;

		double maxPossible = Math.floor(Math.sqrt(query));

		for (int i = 2; i <= maxPossible; i++) {
			if (query % i == 0) {
				return false;
			}
		}

		return true;
	}

	// As an example only!
	public static void main(String[] args) {
		logger.info("next prime: " + PrimeFinder.getNextPrime(1337));
	}
}
