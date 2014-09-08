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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatype.bicluster.Bicluster;
import datatype.bicluster.BinaryVector;
import datatype.bicluster.BitSetBicluster;
import datatype.io.BiclusterIO;
import datatype.matrix.BinaryMatrix;

public class BiclusterUtils {

	private static final Logger logger = LoggerFactory
			.getLogger(BiclusterUtils.class);

	public static long getArea(Bicluster bicluster) {
		return bicluster.getNumberOfRows()
				* bicluster.getNumberOfColumns();
	}

	/**
	 * Returns a set of numbers representing each 'one' entry in the
	 * matrix.
	 */
	public static Set<Integer> getElementSet(Bicluster bicluster,
			BinaryMatrix matrix) {
		Set<Integer> elementSet = new HashSet<Integer>();

		// iterate over all elements of the bicluster, and add the
		// 'ones' to the set
		for (int row : bicluster.getRows()) {
			for (int column : bicluster.getColumns()) {
				if (matrix.get(row, column)) {
					int elementNumber =
							getElementNumber(row, column, matrix);
					elementSet.add(elementNumber);
				}
			}
		}
		return elementSet;
	}

	public static int getElementNumber(int row, int column,
			BinaryMatrix matrix) {
		return row * matrix.getNumColumns() + column;
	}

	public static Bicluster getLargest(
			Collection<? extends Bicluster> biclusters) {
		List<? extends Bicluster> sortedByArea =
				BiclusterUtils.sortByArea(biclusters);
		if (biclusters.size() > 0) {
			return sortedByArea.get(0);
		} else {
			return new BitSetBicluster(); // empty bicluster
		}
	}

	/**
	 * Represents a bicluster not by its own set of ones, but by the
	 * set of ones in its rows and columns that are NOT yet in the
	 * bicluster. This should give much better merging performance!
	 */
	public static Set<Integer> getAlternativeElementSet(
			Bicluster bicluster, BinaryMatrix matrix) {
		Set<Integer> elementSet = new HashSet<Integer>();

		// iterate over all elements of the bicluster, and add the
		// 'ones' to the set
		for (int row : bicluster.getRows()) {
			for (int column = 0; column < matrix.getNumColumns(); column++) {
				if (!bicluster.getColumns().get(column)) {
					if (matrix.get(row, column)) {
						int elementNumber =
								getElementNumber(row, column, matrix);
						elementSet.add(elementNumber);
					}
				}
			}
		}

		for (int column : bicluster.getColumns()) {
			for (int row = 0; row < matrix.getNumRows(); row++) {
				if (!bicluster.getRows().get(row)) {
					if (matrix.get(row, column)) {
						int elementNumber =
								getElementNumber(row, column, matrix);
						elementSet.add(elementNumber);
					}
				}
			}
		}

		return elementSet;
	}

	/**
	 * Represents a bicluster not by its own set of ones, but by the
	 * set of ones in its rows and columns that are NOT yet in the
	 * bicluster - but instead of using all these ones, it tries to
	 * select them wisely: It uses 1) 'ones' in the bicluster rows
	 * that are in a column which has minDensity over the rows of the
	 * bicluster, and 2) 'ones' in the bicluster columns that are in
	 * a row which has minDensity over the columns of the bicluster.
	 * The rationale behind this is that a merge can only be
	 * successful if these conditions are met (unless the two
	 * biclusters that are merged are denser than minDensity). Hence,
	 * no merge opportunities should be missed. Moreover, this
	 * restriction can be very effective because noise in the element
	 * set is reduced, which could significantly improve performance
	 * (merge success).
	 */
	public static Set<Integer> getNewAlternativeElementSet(
			Bicluster bicluster, BinaryMatrix matrix,
			double minDensity) {
		Set<Integer> elementSet = new HashSet<Integer>();

		// for each column not in the bicluster, compute density. If
		// it's sufficient, add its elements to the set:
		for (int column = 0; column < matrix.getNumColumns(); column++) {
			if (!bicluster.getColumns().get(column)) {

				int oneCount = 0;
				for (int row : bicluster.getRows()) {
					if (matrix.get(row, column)) {
						oneCount++;
					}
				}

				if (oneCount >= minDensity
						* bicluster.getNumberOfRows()) { // column is
					// sufficiently
					// dense
					for (int row : bicluster.getRows()) {
						if (matrix.get(row, column)) {
							int elementNumber =
									getElementNumber(row, column,
											matrix);
							elementSet.add(elementNumber);
						}
					}
				}

			}

		}

		// for each row not in the bicluster, compute density. If
		// it's sufficient, add its elements to the set:
		for (int row = 0; row < matrix.getNumRows(); row++) {
			if (!bicluster.getRows().get(row)) {

				int oneCount = 0;
				for (int column : bicluster.getColumns()) {
					if (matrix.get(row, column)) {
						oneCount++;
					}
				}

				if (oneCount >= minDensity
						* bicluster.getNumberOfColumns()) { // row is
					// sufficiently
					// dense
					for (int column : bicluster.getColumns()) {
						if (matrix.get(row, column)) {
							int elementNumber =
									getElementNumber(row, column,
											matrix);
							elementSet.add(elementNumber);
						}
					}
				}

			}

		}

		return elementSet;
	}

	public static float getJaccardSimilarity(Bicluster bc1,
			Bicluster bc2, BinaryMatrix matrix) {

		Set<Integer> set1 = getElementSet(bc1, matrix);
		Set<Integer> set2 = getElementSet(bc2, matrix);

		Set<Integer> union = new HashSet<Integer>(set1);
		union.addAll(set2);

		Set<Integer> intersection = new HashSet<Integer>(set1);
		set1.retainAll(set2);

		return ((float) intersection.size()) / union.size();
	}

	/** Creates a new bicluster list sorted by area, largest first */
	public static <T extends Bicluster> List<T> sortByArea(
			Collection<T> biclusters) {
		ArrayList<T> sortedBiclusters = new ArrayList<T>(biclusters);
		Collections.sort(sortedBiclusters,
				new Comparator<Bicluster>() {

			@Override
			public int compare(Bicluster o1, Bicluster o2) {

				Long a1 = getArea(o1);
				Long a2 = getArea(o2);

				return a2.compareTo(a1);
			}

		});

		return sortedBiclusters;
	}

	public static Bicluster transpose(Bicluster bc) {
		Bicluster transposed = new BitSetBicluster();

		transposed.addRows(bc.getColumns());
		transposed.addColumns(bc.getRows());

		return transposed;
	}

	public static Set<Bicluster>
	transpose(Set<Bicluster> biclusters) {
		Set<Bicluster> biclustersTransposed =
				new HashSet<Bicluster>();
		for (Bicluster bc : biclusters) {
			biclustersTransposed.add(transpose(bc));
		}
		return biclustersTransposed;
	}

	public static <K, V> void addMultiMapValue(Map<K, Set<V>> map,
			K key, V value) {
		if (map.containsKey(key)) {
			map.get(key).add(value);
		} else {
			Set<V> lst = new HashSet<V>(1);
			lst.add(value);
			map.put(key, lst);
		}
	}

	public static <T extends Bicluster> boolean strictlyDominates(
			T big, T small) {

		if (big.getNumberOfRows() < small.getNumberOfRows()
				|| big.getNumberOfColumns() < small
				.getNumberOfColumns()) {
			return false;
		}

		for (int smallRow : small.getRows()) {
			if (!big.getRows().get(smallRow)) {
				return false;
			}
		}
		for (int smallColumn : small.getColumns()) {
			if (!big.getColumns().get(smallColumn)) {
				return false;
			}
		}

		return big.getNumberOfRows() > small.getNumberOfRows()
				|| big.getNumberOfColumns() > small
				.getNumberOfColumns();
	}

	/**
	 * Takes a collection of 'biclusters' and creates one bicluster
	 * that encompasses all of their rows and columns.
	 */
	public static Bicluster mergeBiclusters(
			Collection<Bicluster> biclusters) {
		Bicluster newBicluster = new BitSetBicluster();
		for (Bicluster bic : biclusters) {
			newBicluster.addRows(bic.getRows());
			newBicluster.addColumns(bic.getColumns());
		}
		return newBicluster;
	}

	/**
	 * Get the density as a float value between 0 to 1 for this
	 * 'bicluster' in the 'matrix'.
	 */
	public static float getDensity(Bicluster bicluster,
			BinaryMatrix matrix) {
		int ones = getOneCount(bicluster, matrix);
		long area = getArea(bicluster);
		if (area == 0) {
			return 0;
		} else {
			return ((float) ones) / getArea(bicluster);
		}
	}

	/**
	 * Get the density of the region defined by the row and columns.
	 */
	public static float getDensity(int row,
			Iterable<Integer> columns, BinaryMatrix matrix) {
		int ones = 0;
		int size = 0;
		for (int column : columns) {
			size++;
			if (matrix.get(row, column)) {
				ones++;
			}
		}
		return (float) ones / size;
	}

	/**
	 * Get the density of the region defined by the rows and column.
	 */
	public static float getDensity(Iterable<Integer> rows,
			int column, BinaryMatrix matrix) {
		int ones = 0;
		int size = 0;
		for (int row : rows) {
			size++;
			if (matrix.get(row, column)) {
				ones++;
			}
		}
		return (float) ones / size;
	}

	public static int getOneCount(Bicluster bicluster,
			BinaryMatrix matrix) {
		int ones = 0;
		for (int currentRow : bicluster.getRows()) {
			for (int currentColumn : bicluster.getColumns()) {
				if (matrix.get(currentRow, currentColumn)) {
					ones++;
				}
			}
		}
		return ones;
	}

	/**
	 * Checks whether the UNION of two biclusters' rows and columns
	 * produces a new bicluster with at least 'threshold' density on
	 * the BinaryMatrix 'matrix'
	 */
	public static boolean isUnionDense(Bicluster bic1,
			Bicluster bic2, BinaryMatrix matrix, float threshold) {
		BinaryVector rowsInCommon = bic1.getRows().clone();
		rowsInCommon.union(bic2.getRows());
		BinaryVector columnsInCommon = bic1.getColumns().clone();
		columnsInCommon.union(bic2.getColumns());

		int oneCount = 0;
		for (int currentRow : rowsInCommon) {
			for (int currentColumn : columnsInCommon) {
				if (matrix.get(currentRow, currentColumn))
					oneCount++;
			}
		}

		if (oneCount > (rowsInCommon.cardinality()
				* columnsInCommon.cardinality() * threshold)) {
			return true;
		}

		return false;
	}

	/**
	 * Checks whether bic1 is covered by bic2 by at least 'threshold'
	 * percent.
	 */
	public static boolean isCovered(Bicluster bic1, Bicluster bic2,
			float threshold) {
		return isCovered(bic1, bic2, threshold, false);
	}

	/**
	 * Checks whether bic1 is covered by bic2 by at least 'threshold'
	 * percent, if the twoWay flag is true, it will also require bic2
	 * be covered by bic1 by at least 'threshold' percent.
	 */
	public static boolean isCovered(Bicluster bic1, Bicluster bic2,
			float threshold, boolean twoWay) {
		BinaryVector rowsInCommon = bic1.getRows().clone();
		rowsInCommon.intersection(bic2.getRows());
		BinaryVector columnsInCommon = bic1.getColumns().clone();
		columnsInCommon.intersection(bic2.getColumns());

		float sizeOfOverlap =
				rowsInCommon.cardinality()
				* columnsInCommon.cardinality();
		float bic1Coverage =
				sizeOfOverlap / BiclusterUtils.getArea(bic1);
		float bic2Coverage =
				sizeOfOverlap / BiclusterUtils.getArea(bic2);

		if (twoWay && bic1Coverage >= threshold
				&& bic2Coverage >= threshold)
			return true;
		if (!twoWay && bic1Coverage >= threshold)
			return true;

		return false;
	}

	public static float coverage(Bicluster bic1, Bicluster bic2) {
		BinaryVector rowsInCommon = bic1.getRows().clone();
		rowsInCommon.intersection(bic2.getRows());
		BinaryVector columnsInCommon = bic1.getColumns().clone();
		columnsInCommon.intersection(bic2.getColumns());

		float sizeOfOverlap =
				rowsInCommon.cardinality()
				* columnsInCommon.cardinality();
		return sizeOfOverlap / BiclusterUtils.getArea(bic1);
	}

	public static Bicluster merge(Bicluster first, Bicluster second) {
		Bicluster mergedBicluster = new BitSetBicluster();
		mergedBicluster.addRows(first.getRows());
		mergedBicluster.addRows(second.getRows());
		mergedBicluster.addColumns(first.getColumns());
		mergedBicluster.addColumns(second.getColumns());

		return mergedBicluster;
	}

	public static boolean hasMinDimensions(Bicluster bicluster,
			int min_rows, int min_columns) {
		return (bicluster.getNumberOfRows() >= min_rows && bicluster
				.getNumberOfColumns() >= min_columns);
	}

	public static List<Bicluster> readBiclustersFromTextFile(
			String file) {

		List<Bicluster> biclusters = new ArrayList<Bicluster>();
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			int biclusterCount = 0;
			if ((line = br.readLine()) != null) {
				biclusterCount = Integer.parseInt(line);
			}
			for (int i = 0; i < biclusterCount; i++) {
				String[] rows = br.readLine().split(",");
				String[] columns = br.readLine().split(",");
				Bicluster bicluster = new BitSetBicluster();
				for (String row : rows) {
					bicluster.addRow(Integer.parseInt(row));
				}
				for (String column : columns) {
					bicluster.addColumn(Integer.parseInt(column));
				}
				biclusters.add(bicluster);
			}
			br.close();
		} catch (FileNotFoundException exception) {
			System.out
			.println("Could not find file for reading biclusters!");
			exception.printStackTrace();
		} catch (NumberFormatException exception) {
			System.out
			.println("Incorrect number in file. Should only contain integers!");
			exception.printStackTrace();
		} catch (IOException exception) {
			System.out.println("Could not read from file.");
			exception.printStackTrace();
		}

		return biclusters;
	}

	public static boolean canReachDensity(float minDensity,
			int oneCount, long minArea) {

		if (oneCount < minDensity * minArea) {
			logger.trace("fail - can't reach density");
			return false;
		}
		return true;
	}

	public static void exportFIMI(BinaryMatrix matrix,
			String filename) throws IOException {
		BufferedWriter bw =
				new BufferedWriter(new FileWriter(filename));

		for (int row = 0; row < matrix.getNumRows(); row++) {
			StringBuilder sb = new StringBuilder();
			for (int column = 0; column < matrix.getNumColumns(); column++) {
				if (matrix.get(row, column)) {
					sb.append(column);
					sb.append(" ");
				}
			}
			bw.write(sb.toString() + "\n");
		}

		bw.flush();
		bw.close();

	}

	public static void exportTransposedFIMI(BinaryMatrix matrix,
			String filename) throws IOException {
		BufferedWriter bw =
				new BufferedWriter(new FileWriter(filename));

		for (int column = 0; column < matrix.getNumColumns(); column++) {
			StringBuilder sb = new StringBuilder();
			for (int row = 0; row < matrix.getNumRows(); row++) {
				if (matrix.get(row, column)) {
					sb.append(row);
					sb.append(" ");
				}
			}
			bw.write(sb.toString() + "\n");
		}

		bw.flush();
		bw.close();

	}

	public static List<Bicluster> readFIMIBiclusters(
			String filename, BinaryMatrix matrix) throws IOException {
		BufferedReader br =
				new BufferedReader(new FileReader(filename));

		List<Bicluster> biclusters = new ArrayList<Bicluster>();
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			}
			if (line.length() == 0)
				continue;
			Bicluster bicluster = new BitSetBicluster();
			String[] parts = line.split(" ");
			boolean skipFirst = true;
			for (String part : parts) {
				if (skipFirst) {
					skipFirst = false;
					continue;
				}
				if (part.length() > 0)
					bicluster.addColumn(Integer.parseInt(part));
			}

			int minOnes =
					(int) (bicluster.getNumberOfColumns() * 0.80f);

			for (int i = 0; i < matrix.getNumRows(); i++) {
				int ones = 0;
				for (int column : bicluster.getColumns()) {
					if (matrix.get(i, column))
						ones++;
				}
				if (ones > minOnes) {
					bicluster.addRow(i);
				}
			}

			biclusters.add(bicluster);
		}

		br.close();

		if (!new File(filename + ".biclusters").isFile())
			BiclusterIO.writeBiclusters(filename + ".biclusters",
					biclusters);

		return biclusters;
	}

	public static List<Bicluster> readTransposedFIMIBiclusters(
			String filename, BinaryMatrix matrix) throws IOException {
		BufferedReader br =
				new BufferedReader(new FileReader(filename));

		List<Bicluster> biclusters = new ArrayList<Bicluster>();
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			}
			if (line.length() == 0)
				continue;
			Bicluster bicluster = new BitSetBicluster();
			String[] parts = line.split(" ");
			boolean skipFirst = true;
			for (String part : parts) {
				if (skipFirst) {
					skipFirst = false;
					continue;
				}
				if (part.length() > 0)
					bicluster.addRow(Integer.parseInt(part));
			}

			int minOnes =
					(int) (bicluster.getNumberOfRows() * 0.50f);

			for (int i = 0; i < matrix.getNumColumns(); i++) {
				int ones = 0;
				for (int row : bicluster.getRows()) {
					if (matrix.get(row, i))
						ones++;
				}
				if (ones > minOnes) {
					bicluster.addColumn(i);
				}
			}

			biclusters.add(bicluster);
		}

		br.close();

		if (!new File(filename + ".biclusters").isFile())
			BiclusterIO.writeBiclusters(filename + ".biclusters",
					biclusters);

		return biclusters;
	}

}
