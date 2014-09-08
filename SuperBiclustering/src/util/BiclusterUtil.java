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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;
import datatype.matrix.BitSetBinaryMatrix;
import datatype.matrix.SignalMatrix;

public class BiclusterUtil {

	public static void saveGeneLists(
			Collection<? extends Bicluster> biclusters,
			String filename, SignalMatrix matrix) throws IOException {
		int index = 0;
		for (Bicluster bicluster : biclusters) {
			BufferedWriter bw =
					new BufferedWriter(new FileWriter(String.format(
							"%s%d.txt", filename, index)));

			for (int row : bicluster.getRows()) {
				if (row > matrix.getNumRows())
					row -= matrix.getNumRows();
				bw.write(String.format("%s\n", matrix
						.getRowName(row)));
			}

			bw.close();
			index++;
		}
	}

	public static void saveGeneList(
			List<? extends Bicluster> biclusters, int index,
			String filename, SignalMatrix matrix) throws IOException {
		Bicluster bicluster = biclusters.get(index);
		BufferedWriter bw =
				new BufferedWriter(new FileWriter(String.format(
						"%s%d.txt", filename, index)));

		for (int row : bicluster.getRows()) {
			if (row > matrix.getNumRows())
				row -= matrix.getNumRows();
			bw.write(String.format("%s\n", matrix.getRowName(row)));
		}

		bw.close();
	}

	public static long getBiclusterArea(Bicluster bicluster) {
		return bicluster.getNumberOfRows()
				* bicluster.getNumberOfColumns();
	}

	public static long getTotalOfBiclusterAreas(
			Collection<? extends Bicluster> biclusterSet) {
		long area = 0;
		for (Bicluster bicluster : biclusterSet) {
			area += getBiclusterArea(bicluster);
		}
		return area;
	}

	public static long getTotalBiclusterSetArea(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		BinaryMatrix dummyMatrix =
				new BitSetBinaryMatrix(matrix.getNumRows(), matrix
						.getNumColumns());
		for (Bicluster bicluster : biclusterSet) {
			for (int row : bicluster.getRows()) {
				for (int column : bicluster.getColumns()) {
					if (!dummyMatrix.get(row, column)) {
						dummyMatrix.set(row, column);
					}
				}
			}
		}
		return MatrixUtil.countOnesInMatrix(dummyMatrix);
	}

	public static long countBiclusterSetOnes(
			Collection<? extends Bicluster> biclusterSet,
			BinaryMatrix matrix) {
		long oneCount = 0;
		for (Bicluster bicluster : biclusterSet) {
			oneCount +=
					BiclusterUtil.countBiclusterOnes(bicluster,
							matrix);
		}
		return oneCount;
	}

	public static long countBiclusterOnes(Bicluster bicluster,
			BinaryMatrix matrix) {
		long oneCount = 0;

		for (int row : bicluster.getRows()) {
			for (int column : bicluster.getColumns()) {
				if (matrix.get(row, column)) {
					oneCount++;
				}
			}
		}
		return oneCount;
	}

	public static double getBiclusterDensity(Bicluster bicluster,
			BinaryMatrix matrix) {
		return (double) countBiclusterOnes(bicluster, matrix)
				/ getBiclusterArea(bicluster);
	}

}
