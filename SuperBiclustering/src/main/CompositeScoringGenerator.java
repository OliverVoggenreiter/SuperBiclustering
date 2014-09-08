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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import scoring.BiclusterSetScorer;
import algorithms.Block;
import algorithms.GeneratorBlock;
import algorithms.TransformerBlock;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

public class CompositeScoringGenerator extends GeneratorBlock {

	String fileName;
	GeneratorBlock generatorBlock;
	TransformerBlock[] transformerBlocks;
	private boolean enableLog = true;

	public CompositeScoringGenerator(String filename,
			GeneratorBlock generatorBlock,
			TransformerBlock... transformerBlocks) {
		this.fileName = filename;
		this.generatorBlock = generatorBlock;
		this.transformerBlocks = transformerBlocks;
	}

	public CompositeScoringGenerator logScores(boolean enableLog) {
		this.enableLog = enableLog;
		return this;
	}

	@Override
	public String getName() {
		String name = "CompositeScoring[" + generatorBlock.getName();
		for (Block b : transformerBlocks) {
			name += "|" + b.getName();
		}
		name += "]";
		return name;
	}

	@Override
	public String getShortName() {
		String name =
				"CompositeScoring[" + generatorBlock.getShortName();
		for (Block b : transformerBlocks) {
			name += "|" + b.getShortName();
		}
		name += "]";
		return name;
	}

	@Override
	protected Collection<? extends Bicluster> findBlocks(
			BinaryMatrix matrix) {
		Collection<? extends Bicluster> biclusters = null;
		if (enableLog) {

			try {
				BufferedWriter bw =
						new BufferedWriter(new FileWriter(fileName));
				bw.write("Algorithm Part"
						+ BiclusterSetScorer.getSeparator()
						+ BiclusterSetScorer.getHeader());
				biclusters = generatorBlock.createCandidates(matrix);
				bw.write(generatorBlock.getShortName()
						+ BiclusterSetScorer.getSeparator()
						+ BiclusterSetScorer.calculateScores(
								biclusters, matrix));
				bw.flush();

				for (TransformerBlock t : transformerBlocks) {
					biclusters =
							t.transformBiclusters(biclusters, matrix);
					bw.write(t.getShortName()
							+ BiclusterSetScorer.getSeparator()
							+ BiclusterSetScorer.calculateScores(
									biclusters, matrix));
					bw.flush();
				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			biclusters = generatorBlock.createCandidates(matrix);
			for (TransformerBlock t : transformerBlocks) {
				biclusters =
						t.transformBiclusters(biclusters, matrix);
			}
		}

		return biclusters;
	}

}
