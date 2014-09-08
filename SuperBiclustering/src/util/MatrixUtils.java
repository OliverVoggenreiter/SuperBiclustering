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

import java.lang.reflect.Array;

/**
 * Utilities for matrices represented as two-dimensional arrays.
 */
public class MatrixUtils {

	private MatrixUtils() {
	}

	public static double[][] deepClone(double[][] matrix) {
		int numRows = matrix.length;
		double[][] cloned = new double[numRows][];
		for (int iRow = 0; iRow < numRows; iRow++) {
			cloned[iRow] = matrix[iRow].clone();
		}
		return cloned;
	}

	public static float[][] deepClone(float[][] matrix) {
		int numRows = matrix.length;
		float[][] cloned = new float[numRows][];
		for (int iRow = 0; iRow < numRows; iRow++) {
			cloned[iRow] = matrix[iRow].clone();
		}
		return cloned;
	}

	public static int[][] deepClone(int[][] matrix) {
		int numRows = matrix.length;
		int[][] cloned = new int[numRows][];
		for (int iRow = 0; iRow < numRows; iRow++) {
			cloned[iRow] = matrix[iRow].clone();
		}
		return cloned;
	}

	// ///////////////////////////////////////////////////////////////////////

	/**
	 * Transposes the specified matrix of given type.
	 */
	public static <T> T[][] transpose(T[][] matrix, Class<T> clazz) {
		int rows = matrix.length;
		int cols = matrix[0] != null ? matrix[0].length : 0;
		@SuppressWarnings("unchecked")
		T[][] transposed =
		(T[][]) Array.newInstance(clazz, cols, rows);

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				transposed[j][i] = matrix[i][j];
			}
		}
		return transposed;
	}

	/**
	 * Transposes the specified matrix of type double.
	 */
	public static double[][] transpose(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0] != null ? matrix[0].length : 0;
		double[][] transposed = new double[cols][rows];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				transposed[j][i] = matrix[i][j];
			}
		}
		return transposed;
	}

	/**
	 * Transposes the specified matrix of type float.
	 */
	public static float[][] transpose(float[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0] != null ? matrix[0].length : 0;
		float[][] transposed = new float[cols][rows];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				transposed[j][i] = matrix[i][j];
			}
		}
		return transposed;
	}

	/**
	 * Transposes the specified matrix of type int.
	 */
	public static int[][] transpose(int[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0] != null ? matrix[0].length : 0;
		int[][] transposed = new int[cols][rows];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				transposed[j][i] = matrix[i][j];
			}
		}
		return transposed;
	}

	// ///////////////////////////////////////////////////////////////////////

	public static <T> boolean isSquare(T[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		return (rows == cols);
	}

	public static <T> boolean isSquare(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		return (rows == cols);
	}

	public static <T> boolean isSquare(float[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		return (rows == cols);
	}

	public static <T> boolean isSquare(int[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		return (rows == cols);
	}

	// ///////////////////////////////////////////////////////////////////////

	public static <T> boolean isLowerTriangular(T[][] matrix) {
		int rows = matrix.length;
		for (int i = 0; i < rows; i++) {
			if (matrix[i].length != i) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLowerTriangular(double[][] matrix) {
		int rows = matrix.length;
		for (int i = 0; i < rows; i++) {
			if (matrix[i].length != i) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLowerTriangular(float[][] matrix) {
		int rows = matrix.length;
		for (int i = 0; i < rows; i++) {
			if (matrix[i].length != i) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLowerTriangular(int[][] matrix) {
		int rows = matrix.length;
		for (int i = 0; i < rows; i++) {
			if (matrix[i].length != i) {
				return false;
			}
		}
		return true;
	}

}
