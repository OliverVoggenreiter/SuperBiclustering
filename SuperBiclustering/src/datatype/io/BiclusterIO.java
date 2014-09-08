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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.DataFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datatype.bicluster.Bicluster;

/**
 * BiclusterIO provides a utility for saving biclusters in a binary
 * form as per the serialization interface from the standard java
 * library.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 6, 2013
 *
 */
public class BiclusterIO {

	private static final Logger logger = LoggerFactory
			.getLogger(BiclusterIO.class);

	/**
	 * Takes a collection of biclusters and saves them to the file
	 * 'filename' using built-in java serialization.
	 */
	public static void writeBiclusters(String filename,
			Collection<? extends Bicluster> biclusters) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(biclusters);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			logger.error(String.format("Could not find file: %s",
					filename));
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(String.format(
					"Failed to create OutputStream for file: %s",
					filename));
			e.printStackTrace();
		}
		logger.info(String.format(
				"Successfully saved %1d biclusters to %s",
				biclusters.size(), filename));
	}

	/**
	 * Reads a file and rebuilds the Collection of biclusters from
	 * that file. Will throw a DataFormatException if there is no
	 * collection of biclusters in the file to read.
	 */
	public static List<Bicluster> readBiclusters(String filename) {
		List<Bicluster> biclusters = new ArrayList<Bicluster>();
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Object input = ois.readObject();
			if (input instanceof Collection<?>) {
				for (Object item : (Collection<?>) input) {
					if (item instanceof Bicluster) {
						biclusters.add((Bicluster) item);
					} else if (item == null) {
						// do nothing
					} else {
						logger.info("Look what came out... "
								+ item.getClass().toString());
						ois.close();
						throw new DataFormatException(
								"Object is not of type Bicluster");
					}
				}
			} else {
				ois.close();
				throw new DataFormatException(
						"Object is not of type Collection<?>");
			}
			ois.close();
		} catch (FileNotFoundException e) {
			logger.error(String.format(
					"Could not find input file: %s", filename));
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(String.format(
					"Could not open file for reading: %s", filename));
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error(String
					.format("Could not find class definition!"));
			e.printStackTrace();
		} catch (DataFormatException e) {
			logger.error(String
					.format("One or more objects in the stream are of an incorrect type."));
			e.printStackTrace();
		}
		logger.info(String
				.format("Successfully retrieved %1d biclusters from file: %1s",
						biclusters.size(), filename));
		return biclusters;
	}
}
