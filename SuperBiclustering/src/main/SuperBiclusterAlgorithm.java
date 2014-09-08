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

package main;

import static org.kohsuke.args4j.ExampleMode.ALL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import util.FileFormatException;
import algorithms.GeneratorBlock;
import datatype.io.BiclusterIO;
import datatype.io.BinaryMatrixIO;
import datatype.matrix.BinaryMatrix;

public class SuperBiclusterAlgorithm {

	@Option(name = "-s", usage = "min size of biclusters")
	private int minSize = 10;

	@Option(name = "-o", usage = "filename for bicluster output")
	private String fileOut;

	@Option(name = "-d", usage = "min density of biclusters")
	private float minDensity = 0.9f;

	@Option(name = "-c", usage = "number of cores to use")
	private int coreCount = 1;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	private String algoName = "GSB";

	public static void main(String[] args)
			throws ClassNotFoundException, IOException,
			FileFormatException {
		new SuperBiclusterAlgorithm().run(args);
	}

	public void run(String[] args) throws ClassNotFoundException,
	IOException, FileFormatException {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			if (arguments.isEmpty()) {
				throw new CmdLineException(parser, "No input given.");
			}
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err
			.println("java -jar SuperBiclusterAlgorithm.jar [options...] arguments...");
			parser.printUsage(System.err);

			System.err.println();
			System.err
			.println("  Example: java SuperBiclusterAlgorithm"
					+ parser.printExample(ALL));

			return;
		}

		GeneratorBlock superBiclusterAlgo =
				new SuperBiclusteringFactory().setCoreCount(
						coreCount).setMinDensity(minDensity)
						.setMinSize(minSize).getBBKPlusGenerator(
								"BBKPlus");

		for (String matrixFile : arguments) {
			BinaryMatrix binMat;
			if (matrixFile.endsWith(".binMat.binary")) {
				binMat = BinaryMatrixIO.readBinaryFile(matrixFile);
			} else {
				binMat = BinaryMatrixIO.readTextFile(matrixFile);
			}
			BiclusterIO.writeBiclusters(algoName + ".biclusters",
					superBiclusterAlgo.createCandidates(binMat));
		}
	}

}
