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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomUtil {

	public static Integer[] getRandomUniqueIndices(int low,
			int high, int size) {
		Random rand = new Random();
		Set<Integer> randomIndices = new HashSet<Integer>();
		while (randomIndices.size() < size) {
			randomIndices.add(rand.nextInt(high - low) + low);
		}
		return randomIndices.toArray(new Integer[0]);
	}

}