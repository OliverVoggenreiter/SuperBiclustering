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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.FileFormatException;

public class AbstractMatrixIO {

	protected AbstractMatrixIO() {
	}

	@SuppressWarnings("unchecked")
	protected static <T> T readBinaryFile(String fileName,
			Class<T> clazz) throws IOException,
			ClassNotFoundException {
		T matrix = null;

		ObjectInputStream in = null;
		try {
			in =
					new ObjectInputStream(new FileInputStream(
							fileName));
			matrix = (T) in.readObject();
			in.close();
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return matrix;
	}

	protected static <T> void writeBinaryFile(T matrix,
			String fileName) throws IOException {

		ObjectOutputStream out = null;
		try {
			out =
					new ObjectOutputStream(new FileOutputStream(
							fileName));
			out.writeObject(matrix);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	// ///////////////////////////////////////////////////////////////////////

	public static class Dimension {
		private final int numRows;
		private final int numColumns;

		public Dimension(int numRows, int numColumns) {
			this.numRows = numRows;
			this.numColumns = numColumns;
		}

		public int getNumRows() {
			return numRows;
		}

		public int getNumColumns() {
			return numColumns;
		}
	}

	/**
	 * Checks if the text file with the given <code>fileName</code>
	 * contains the same number of columns in each row and returns
	 * the corresponding <code>Dimension</code> object.
	 */
	protected static Dimension checkDimension(String fileName,
			String separator) throws IOException,
			FileFormatException {
		int numRows = 0;
		int numColumns = 0;
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				// handle special behavior of split with empty string
				// as pattern
				int length =
						(separator.equals("")) ? line.length()
								: line.split(separator).length;

						if (numRows == 0) {
							numColumns = length;
						} else if (length != numColumns) {
							throw new FileFormatException(
									"File must contain the same number of columns in each row.");
						}
						numRows++;
			}
			reader.close();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return new Dimension(numRows, numColumns);
	}

}
