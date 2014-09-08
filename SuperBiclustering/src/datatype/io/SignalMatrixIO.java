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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.FileFormatException;
import datatype.matrix.SignalMatrix;

public class SignalMatrixIO extends AbstractMatrixIO {

	private static Logger logger = LoggerFactory
			.getLogger(SignalMatrixIO.class);

	private static final String SEPARATOR = ",";

	public static interface FloatDecoder {
		public float decode(String value);
	}

	public static class DefaultFloatDecoder implements FloatDecoder {
		@Override
		public float decode(String value) {
			return Float.valueOf(value);
		}
	}

	private SignalMatrixIO() {
	}

	/**
	 * Reads a SignalMatrix from a binary (i.e. using the Java
	 * serialization mechanism) file.
	 */
	public static SignalMatrix readBinaryFile(String fileName)
			throws IOException, ClassNotFoundException {
		logger.debug("Reading binary file " + fileName + "...");
		return AbstractMatrixIO.readBinaryFile(fileName,
				SignalMatrix.class);
	}

	/**
	 * Writes a SignalMatrix to a binary (i.e. using the Java
	 * serialization mechanism) file.
	 */
	public static void writeBinaryFile(SignalMatrix matrix,
			String fileName) throws IOException {
		logger.debug("Writing binary file " + fileName + "...");
		AbstractMatrixIO.writeBinaryFile(matrix, fileName);
	}

	public static SignalMatrix readTextFile(String fileName)
			throws IOException, FileFormatException {
		return readTextFile(fileName, new DefaultFloatDecoder());
	}

	public static SignalMatrix readTextFile(String fileName,
			FloatDecoder decoder) throws IOException,
			FileFormatException {
		logger.debug("Reading text file " + fileName + "...");

		Dimension dimension =
				AbstractMatrixIO.checkDimension(fileName, SEPARATOR);
		int numRows = dimension.getNumRows() - 1;
		int numColumns = dimension.getNumColumns() - 1;
		logger.debug("numRows=" + numRows + ", numColumns="
				+ numColumns);

		SignalMatrix matrix = new SignalMatrix(numRows, numColumns);

		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));

			// read header line
			line = reader.readLine();
			// logger.trace("line=\"" + line + "\"");
			String[] columnNames = line.split(SEPARATOR);
			for (int iColumn = 0; iColumn < numColumns; iColumn++) {
				matrix.setColumnName(iColumn,
						columnNames[iColumn + 1]);
			}

			// read data lines
			for (int iRow = 0; iRow < numRows; iRow++) {
				line = reader.readLine();
				// logger.trace("line=\"" + line + "\"");
				String[] columns = line.split(SEPARATOR);
				matrix.setRowName(iRow, columns[0]);

				for (int iColumn = 0; iColumn < numColumns; iColumn++) {
					float value =
							decoder.decode(columns[iColumn + 1]);
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

	public static void writeTextFile(SignalMatrix matrix,
			String fileName) throws IOException {
		logger.debug("Writing text file " + fileName + "...");

		PrintWriter writer = null;
		try {
			writer =
					new PrintWriter(new BufferedWriter(
							new FileWriter(fileName)));
			int numRows = matrix.getNumRows();
			int numColumns = matrix.getNumColumns();

			// write header row, containing column names
			writer.print("");
			if (numColumns > 0) {
				writer.print(SEPARATOR);
			}
			for (int iColumn = 0; iColumn < numColumns; iColumn++) {
				writer.print(matrix.getColumnName(iColumn));
				if (iColumn < numColumns - 1) {
					writer.print(SEPARATOR);
				}
			}
			writer.println();

			// write data rows
			for (int iRow = 0; iRow < numRows; iRow++) {
				writer.print(matrix.getRowName(iRow));
				if (numColumns > 0) {
					writer.print(SEPARATOR);
				}
				for (int iColumn = 0; iColumn < numColumns; iColumn++) {
					float data = matrix.get(iRow, iColumn);
					writer.print(data);
					if (iColumn < numColumns - 1) {
						writer.print(SEPARATOR);
					}
				}
				writer.println();
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

}
