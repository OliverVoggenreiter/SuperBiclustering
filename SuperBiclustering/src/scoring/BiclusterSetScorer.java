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

package scoring;

import java.util.Collection;
import java.util.Iterator;

import util.BiclusterUtil;
import util.MatrixUtil;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class BiclusterSetScorer {

	private static final String SEPARATOR = "\t";

	public static String getSeparator() {
		return SEPARATOR;
	}

	public static String getHeader() {
		StringBuilder output = new StringBuilder();
		output.append("Ones Covered %");
		output.append(SEPARATOR);
		output.append("Ones Covered");
		output.append(SEPARATOR);
		output.append("Zeros Covered %");
		output.append(SEPARATOR);
		output.append("Zeros Covered");
		output.append(SEPARATOR);
		output.append("Average One Coverage");
		output.append(SEPARATOR);
		output.append("Average Coverage");
		output.append(SEPARATOR);

		output.append("Average Size");
		output.append(SEPARATOR);
		output.append("Largest Size");
		output.append(SEPARATOR);
		output.append("Smallest Size");
		output.append(SEPARATOR);

		output.append("Average Density %");
		output.append(SEPARATOR);
		output.append("Highest Density %");
		output.append(SEPARATOR);
		output.append("Lowest Density %");
		output.append(SEPARATOR);

		output.append("Bicluster Count\n");

		return output.toString();
	}

	public static String calculateScores(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		StringBuilder output = new StringBuilder();
		output.append(String.format("%.3f", getOnesCoveredPercent(
				biclusterSet, matrix)));
		output.append(SEPARATOR);
		output.append(getOnesCovered(biclusterSet, matrix));
		output.append(SEPARATOR);
		output.append(String.format("%.3f", getZerosCoveredPercent(
				biclusterSet, matrix)));
		output.append(SEPARATOR);
		output.append(getZerosCovered(biclusterSet, matrix));
		output.append(SEPARATOR);
		output.append(String.format("%.3f", getAverageOneCoverage(
				biclusterSet, matrix)));
		output.append(SEPARATOR);
		output.append(String.format("%.3f", getAverageCoverage(
				biclusterSet, matrix)));
		output.append(SEPARATOR);

		output.append(String.format("%.3f",
				getAverageBiclusterSize(biclusterSet)));
		output.append(SEPARATOR);
		output.append(getLargestBiclusterSize(biclusterSet));
		output.append(SEPARATOR);
		output.append(getSmallestBiclusterSize(biclusterSet));
		output.append(SEPARATOR);

		output.append(String.format("%.3f",
				getAverageBiclusterDensity(biclusterSet, matrix)));
		output.append(SEPARATOR);
		output.append(String.format("%.3f",
				getHighestBiclusterDensity(biclusterSet, matrix)));
		output.append(SEPARATOR);
		output.append(String.format("%.3f",
				getLowestBiclusterDensity(biclusterSet, matrix)));
		output.append(SEPARATOR);

		output.append(getBiclusterCount(biclusterSet));
		output.append("\n");

		return output.toString();
	}

	// Percent of ones covered
	public static double getOnesCoveredPercent(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		return (double) getOnesCovered(biclusterSet, matrix)
				/ MatrixUtil.countOnesInMatrix(matrix) * 100.0;
	}

	public static long getOnesCovered(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		BinaryMatrix matrixCopy = MatrixUtil.cloneMatrix(matrix);
		long oneCount = 0;

		for (Bicluster bicluster : biclusterSet) {
			for (int iRow : bicluster.getRows()) {
				for (int iColumn : bicluster.getColumns()) {
					if (matrixCopy.get(iRow, iColumn)) {
						matrixCopy.set(iRow, iColumn, false);
						oneCount++;
					}
				}
			}
		}
		return oneCount;
	}

	public static long getOnesInMatrix(BinaryMatrix matrix) {
		return MatrixUtil.countOnesInMatrix(matrix);
	}

	// Percent of zeros covered
	public static double getZerosCoveredPercent(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		return (double) getZerosCovered(biclusterSet, matrix)
				/ MatrixUtil.countZerosInMatrix(matrix) * 100.0;
	}

	public static long getZerosCovered(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		BinaryMatrix matrixCopy = MatrixUtil.cloneMatrix(matrix);
		long zeroCount = 0;

		for (Bicluster bicluster : biclusterSet) {
			for (int iRow : bicluster.getRows()) {
				for (int iColumn : bicluster.getColumns()) {
					if (!matrixCopy.get(iRow, iColumn)) {
						matrixCopy.set(iRow, iColumn);
						zeroCount++;
					}
				}
			}
		}
		return zeroCount;
	}

	public static long getZerosInMatrix(BinaryMatrix matrix) {
		return MatrixUtil.countZerosInMatrix(matrix);
	}

	// Average coverage of ones
	public static double getAverageOneCoverage(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		return (double) BiclusterUtil.countBiclusterSetOnes(
				biclusterSet, matrix)
				/ getOnesCovered(biclusterSet, matrix);
	}

	// Average coverage
	public static double getAverageCoverage(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		return (double) BiclusterUtil
				.getTotalOfBiclusterAreas(biclusterSet)
				/ BiclusterUtil.getTotalBiclusterSetArea(
						biclusterSet, matrix);
	}

	// Average bicluster size
	public static double getAverageBiclusterSize(
			Collection<? extends Bicluster> biclusterSet) {
		long totalSize =
				BiclusterUtil.getTotalOfBiclusterAreas(biclusterSet);
		return (double) totalSize / biclusterSet.size();
	}

	// Largest bicluster size
	public static long getLargestBiclusterSize(
			Collection<? extends Bicluster> biclusterSet) {
		long size = -1;
		for (Bicluster bicluster : biclusterSet) {
			if (BiclusterUtil.getBiclusterArea(bicluster) > size) {
				size = BiclusterUtil.getBiclusterArea(bicluster);
			}
		}
		return size;
	}

	// Smallest bicluster size
	public static long getSmallestBiclusterSize(
			Collection<? extends Bicluster> biclusterSet) {
		long size = Long.MAX_VALUE;
		for (Bicluster bicluster : biclusterSet) {
			if (BiclusterUtil.getBiclusterArea(bicluster) < size) {
				size = BiclusterUtil.getBiclusterArea(bicluster);
			}
		}
		return size;
	}

	// Average density
	public static double getAverageBiclusterDensity(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		double totalDensities = 0;
		for (Bicluster bicluster : biclusterSet) {
			totalDensities +=
					BiclusterUtil.getBiclusterDensity(bicluster,
							matrix);
		}
		return totalDensities / biclusterSet.size() * 100.0;
	}

	// Highest density
	public static double getHighestBiclusterDensity(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		double highestDensity = -1;
		Iterator<? extends Bicluster> setIterator =
				biclusterSet.iterator();
		while (highestDensity < 1 && setIterator.hasNext()) {
			double density =
					BiclusterUtil.getBiclusterDensity(setIterator
							.next(), matrix);
			if (density > highestDensity) {
				highestDensity = density;
			}
		}
		return highestDensity * 100.0;
	}

	// Lowest density
	public static double getLowestBiclusterDensity(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		double lowestDensity = Double.MAX_VALUE;
		Iterator<? extends Bicluster> setIterator =
				biclusterSet.iterator();
		while (lowestDensity > 0 && setIterator.hasNext()) {
			double density =
					BiclusterUtil.getBiclusterDensity(setIterator
							.next(), matrix);
			if (density < lowestDensity) {
				lowestDensity = density;
			}
		}
		return lowestDensity * 100.0;
	}

	// Number of biclusters
	public static long getBiclusterCount(
			Collection<? extends Bicluster> biclusterSet) {
		return biclusterSet.size();
	}

	public static String calculateExtendedScores(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix,
			Collection<? extends Bicluster> realBiclusters) {
		return "";
	}


}
