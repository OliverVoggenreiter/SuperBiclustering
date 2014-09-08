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

import java.util.Collection;
import java.util.Iterator;

public class ArrayUtils {

	private ArrayUtils() {
	}

	/**
	 * Concatenates 2 arrays. Returns a newly allocated copy, i.e.
	 * changes to the result do not write back to a nor b.
	 */
	public static <T> T[] concat(T[] a, T[] b) {
		final int aLen = a.length;
		final int bLen = b.length;

		@SuppressWarnings("unchecked")
		final T[] result =
		(T[]) java.lang.reflect.Array.newInstance(a
				.getClass().getComponentType(), aLen + bLen);

		System.arraycopy(a, 0, result, 0, aLen);
		System.arraycopy(b, 0, result, aLen, bLen);
		return result;
	}

	/**
	 * Concatenates 2 arrays. Returns a newly allocated copy, i.e.
	 * changes to the result do not write back to a nor b.
	 */
	public static double[] concat(double[] a, double[] b) {
		final int aLen = a.length;
		final int bLen = b.length;
		double[] result = new double[aLen + bLen];

		System.arraycopy(a, 0, result, 0, aLen);
		System.arraycopy(b, 0, result, aLen, bLen);
		return result;
	}

	/**
	 * Concatenates 2 arrays. Returns a newly allocated copy, i.e.
	 * changes to the result do not write back to a nor b.
	 */
	public static float[] concat(float[] a, float[] b) {
		final int aLen = a.length;
		final int bLen = b.length;
		float[] result = new float[aLen + bLen];

		System.arraycopy(a, 0, result, 0, aLen);
		System.arraycopy(b, 0, result, aLen, bLen);
		return result;
	}

	/**
	 * Concatenates 2 arrays. Returns a newly allocated copy, i.e.
	 * changes to the result do not write back to a nor b.
	 */
	public static int[] concat(int[] a, int[] b) {
		final int aLen = a.length;
		final int bLen = b.length;
		int[] result = new int[aLen + bLen];

		System.arraycopy(a, 0, result, 0, aLen);
		System.arraycopy(b, 0, result, aLen, bLen);
		return result;
	}

	// ///////////////////////////////////////////////////////////////////////

	/**
	 * Converts a collection e.q List or Set of Double objects to a
	 * double array<br>
	 * Note: By convention an empty collection will return a double[]
	 * with length zero.
	 */
	public static double[] toDoubleArray(Collection<Double> col) {
		double[] array = new double[col.size()];
		Iterator<Double> iterator = col.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			array[i++] = iterator.next();
		}
		return array;
	}

	/**
	 * Converts a collection e.q List or Set of Float objects to a
	 * float array<br>
	 * Note: By convention an empty collection will return a float[]
	 * with length zero.
	 */
	public static float[] toFloatArray(Collection<Float> col) {
		float[] array = new float[col.size()];
		Iterator<Float> iterator = col.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			array[i++] = iterator.next();
		}
		return array;
	}

	/**
	 * Converts a collection e.q List or Set of Integer objects to an
	 * int array<br>
	 * Note: By convention an empty collection will return an int[]
	 * with length zero.
	 */
	public static int[] toIntArray(Collection<Integer> col) {
		int[] array = new int[col.size()];
		Iterator<Integer> iterator = col.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			array[i++] = iterator.next();
		}
		return array;
	}

	// ///////////////////////////////////////////////////////////////////////

	public static float[] toFloat(double[] data) {
		float[] result = new float[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (float) data[i];
		}
		return result;
	}

	public static double[] toDouble(float[] data) {
		double[] result = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = data[i];
		}
		return result;
	}

	// ///////////////////////////////////////////////////////////////////////

	public static int[] getSequence(int length) {
		return getSequence(0, length - 1);
	}

	public static int[] getSequence(int from, int to) {
		assert (from <= to);
		int[] array = new int[to - from + 1];
		for (int i = from; i <= to; i++) {
			array[i - from] = i;
		}
		return array;
	}

	// ///////////////////////////////////////////////////////////////////////

	public static float[] scale(float[] data, float factor) {
		int numI = data.length;
		float[] result = new float[numI];
		for (int i = 0; i < numI; i++) {
			result[i] = factor * data[i];
		}
		return result;
	}

	public static float[][] scale(float[][] data, float factor) {
		int numI = data.length;
		int numJ = data[0].length;
		float[][] result = new float[numI][numJ];
		for (int i = 0; i < numI; i++) {
			for (int j = 0; j < numJ; j++) {
				result[i][j] = factor * data[i][j];
			}
		}
		return result;
	}

}
