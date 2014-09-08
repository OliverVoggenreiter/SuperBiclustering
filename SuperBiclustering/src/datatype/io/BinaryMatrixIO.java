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

package datatype.io;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.FileFormatException;
import datatype.matrix.BinaryMatrix;
import datatype.matrix.BinaryMatrixFactory;
import datatype.matrix.BitSetBinaryMatrixFactory;

public class BinaryMatrixIO extends AbstractMatrixIO {

	private static Logger logger = LoggerFactory
			.getLogger(BinaryMatrixIO.class);

	private BinaryMatrixIO() {
	}

	/**
	 * Reads a BinaryMatrix from a binary (i.e. using the Java
	 * serialization mechanism) file.
	 */
	public static BinaryMatrix readBinaryFile(String fileName)
			throws IOException, ClassNotFoundException {
		logger.debug("Reading binary file " + fileName + "...");
		return AbstractMatrixIO.readBinaryFile(fileName,
				BinaryMatrix.class);
	}

	/**
	 * Writes a BinaryMatrix to a binary (i.e. using the Java
	 * serialization mechanism) file.
	 */
	public static void writeBinaryFile(BinaryMatrix matrix,
			String fileName) throws IOException {
		logger.debug("Writing binary file " + fileName + "...");
		AbstractMatrixIO.writeBinaryFile(matrix, fileName);
	}

	/**
	 * Reads a BinaryMatrix from a text file. See @see
	 * {@link BinaryMatrixIO#writeTextFile(BinaryMatrix, String)} for
	 * the format definition.
	 */
	public static BinaryMatrix readTextFile(String fileName)
			throws IOException, FileFormatException {
		return readTextFile(fileName,
				new BitSetBinaryMatrixFactory()); // use default
		// factory
	}

	/**
	 * Reads a BinaryMatrix from a text file. See @see
	 * {@link BinaryMatrixIO#writeTextFile(BinaryMatrix, String)} for
	 * the format definition.
	 */
	public static BinaryMatrix readTextFile(String fileName,
			BinaryMatrixFactory binaryMatrixFactory)
					throws IOException, FileFormatException {
		logger.debug("Reading text file " + fileName + "...");

		Dimension dimension =
				AbstractMatrixIO.checkDimension(fileName, "");
		int numRows = dimension.getNumRows();
		int numColumns = dimension.getNumColumns();
		logger.debug("numRows=" + numRows + ", numColumns="
				+ numColumns);
		BinaryMatrix matrix =
				binaryMatrixFactory.createBinaryMatrix(numRows,
						numColumns);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			for (int iRow = 0; iRow < numRows; iRow++) {
				String line = reader.readLine();
				logger.trace("line=\"" + line + "\"");
				for (int iColumn = 0; iColumn < numColumns; iColumn++) {
					boolean value =
							line.charAt(iColumn) == '1' ? true
									: false;
					matrix.set(iRow, iColumn, value);
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return matrix;
	}

	/**
	 * Writes a BinaryMatrix to a text file. One line is written per
	 * row. For each element of a row, either "1" or "0" is written,
	 * with no separator between elements.
	 */
	public static void writeTextFile(BinaryMatrix matrix,
			String fileName) throws IOException {
		logger.debug("Writing text file " + fileName + "...");

		PrintWriter writer = null;
		try {
			writer =
					new PrintWriter(new BufferedWriter(
							new FileWriter(fileName)));
			int numRows = matrix.getNumRows();
			int numColumns = matrix.getNumColumns();
			logger.debug("numRows=" + numRows + ", numColumns="
					+ numColumns);

			for (int iRow = 0; iRow < numRows; iRow++) {
				for (int iColumn = 0; iColumn < numColumns; iColumn++) {
					boolean value = matrix.get(iRow, iColumn);
					writer.print((value ? '1' : '0'));
				}
				writer.println();
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Writes the BinaryMatrix into an image file. Possible formats:
	 * gif, png.
	 */
	public static void writeImageFile(BinaryMatrix binaryMatrix,
			String fileName, String imageFormat) throws IOException {
		logger.debug("Writing matrix image to " + fileName + " ...");

		BufferedImage image =
				new BufferedImage(binaryMatrix.getNumColumns(),
						binaryMatrix.getNumRows(),
						BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();

		// visit each point of the matrix and paint it:
		for (int i = 0; i < binaryMatrix.getNumRows(); i++) {
			for (int j = 0; j < binaryMatrix.getNumColumns(); j++) {
				if (binaryMatrix.get(i, j)) {
					g2.setColor(Color.BLACK);
				} else {
					g2.setColor(Color.WHITE);
				}
				g2.drawLine(j, i, j, i);
			}
		}

		OutputStream file = new FileOutputStream(fileName);
		ImageIO.write(image, imageFormat, file);
		logger.debug("Finished writing matrix image to " + fileName);
	}

}
