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

package algorithms.transformers;

/**
 * This is a reimplementation of Java's sort using a special
 * reference array for the value of each int being compared.
 * Unfortunately there is no better way to do this than to copy and
 * alter the library code thanks to Java's need for Objects in custom
 * comparators and the autoboxing feature.
 *
 * @author "Oliver Voggenreiter"
 * @date Feb 27, 2013
 *
 */
public class RefArrays {

	/**
	 * Sorts the specified range of the specified array of ints into
	 * ascending numerical order. The range to be sorted extends from
	 * index <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>
	 * , exclusive. (If <tt>fromIndex==toIndex</tt>, the range to be
	 * sorted is empty.)
	 * <p>
	 *
	 * The sorting algorithm is a tuned quicksort, adapted from Jon
	 * L. Bentley and M. Douglas McIlroy's
	 * "Engineering a Sort Function", Software-Practice and
	 * Experience, Vol. 23(11) P. 1249-1265 (November 1993). This
	 * algorithm offers n*log(n) performance on many data sets that
	 * cause other quicksorts to degrade to quadratic performance.
	 *
	 * @param a
	 *            the array to be sorted
	 * @param fromIndex
	 *            the index of the first element (inclusive) to be
	 *            sorted
	 * @param toIndex
	 *            the index of the last element (exclusive) to be
	 *            sorted
	 * @throws IllegalArgumentException
	 *             if <tt>fromIndex &gt; toIndex</tt>
	 * @throws ArrayIndexOutOfBoundsException
	 *             if <tt>fromIndex &lt; 0</tt> or
	 *             <tt>toIndex &gt; a.length</tt>
	 */
	public static void sort(int[] a, int fromIndex, int toIndex,
			int[] values) {
		rangeCheck(a.length, fromIndex, toIndex);
		sort1(a, fromIndex, toIndex - fromIndex, values);
	}

	/**
	 * Check that fromIndex and toIndex are in range, and throw an
	 * appropriate exception if they aren't.
	 */
	private static void rangeCheck(int arrayLen, int fromIndex,
			int toIndex) {
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex("
					+ fromIndex + ") > toIndex(" + toIndex + ")");
		if (fromIndex < 0)
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		if (toIndex > arrayLen)
			throw new ArrayIndexOutOfBoundsException(toIndex);
	}

	/**
	 * Sorts the specified sub-array of integers into ascending
	 * order.
	 */
	private static void
	sort1(int x[], int off, int len, int[] values) {
		// Insertion sort on smallest arrays
		if (len < 7) {
			for (int i = off; i < len + off; i++)
				for (int j = i; j > off
						&& values[x[j - 1]] > values[x[j]]; j--)
					swap(x, j, j - 1);
			return;
		}
		// Choose a partition element, v
		int m = off + (len >> 1); // Small arrays, middle element
		if (len > 7) {
			int l = off;
			int n = off + len - 1;
			if (len > 40) { // Big arrays, pseudomedian of 9
				int s = len / 8;
				l = med3(x, l, l + s, l + 2 * s, values);
				m = med3(x, m - s, m, m + s, values);
				n = med3(x, n - 2 * s, n - s, n, values);
			}
			m = med3(x, l, m, n, values); // Mid-size, med of 3
		}
		int v = values[x[m]];

		// Establish Invariant: v* (<v)* (>v)* v*
		int a = off, b = a, c = off + len - 1, d = c;
		while (true) {
			while (b <= c && values[x[b]] <= v) {
				if (values[x[b]] == v)
					swap(x, a++, b);
				b++;
			}
			while (c >= b && values[x[c]] >= v) {
				if (values[x[c]] == v)
					swap(x, c, d--);
				c--;
			}
			if (b > c)
				break;
			swap(x, b++, c--);
		}

		// Swap partition elements back to middle
		int s, n = off + len;
		s = Math.min(a - off, b - a);
		vecswap(x, off, b - s, s);
		s = Math.min(d - c, n - d - 1);
		vecswap(x, b, n - s, s);

		// Recursively sort non-partition-elements
		if ((s = b - a) > 1)
			sort1(x, off, s, values);
		if ((s = d - c) > 1)
			sort1(x, n - s, s, values);
	}

	/**
	 * Swaps x[a] with x[b].
	 */
	private static void swap(int x[], int a, int b) {
		int t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

	/**
	 * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
	 */
	private static void vecswap(int x[], int a, int b, int n) {
		for (int i = 0; i < n; i++, a++, b++)
			swap(x, a, b);
	}

	/**
	 * Returns the index of the median of the three indexed integers.
	 */
	private static int med3(int x[], int a, int b, int c,
			int[] values) {
		return (values[x[a]] < values[x[b]] ? (values[x[b]] < values[x[c]] ? b
				: values[x[a]] < values[x[c]] ? c : a)
				: (values[x[b]] > values[x[c]] ? b
						: values[x[a]] > values[x[c]] ? c : a));
	}

}
