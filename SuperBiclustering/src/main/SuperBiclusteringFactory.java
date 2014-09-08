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

import algorithms.GeneratorBlock;
import algorithms.generators.BiclusterGenerator;
import algorithms.generators.BipartiteBronKerboschPlusGenerator;
import algorithms.generators.RestrictedBiclusterGenerator;
import algorithms.generators.RestrictiveGenerator;
import algorithms.transformers.BlockSplitterTransformer;
import algorithms.transformers.DegenerativeDrillTransformer;
import algorithms.transformers.GreedyBlockSplitterTransformer;
import algorithms.transformers.GreedyExpanderTransformer;
import algorithms.transformers.IncreaseBlockDensityTransformer;
import algorithms.transformers.MinHashTransformer;
import algorithms.transformers.RemoveLowSignificanceTransformer;
import algorithms.transformers.SmallBiclusterFilter;

public class SuperBiclusteringFactory {

	private float minDensity = 0.9f;
	private int coreCount = 2;
	private int minSize = 10;

	public SuperBiclusteringFactory setMinDensity(float minDensity) {
		this.minDensity = minDensity;
		return this;
	}

	public SuperBiclusteringFactory setCoreCount(int coreCount) {
		this.coreCount = coreCount;
		return this;
	}

	public SuperBiclusteringFactory setMinSize(int outputSize) {
		this.minSize = outputSize;
		return this;
	}

	public GeneratorBlock getSuperBiclusterer(String name) {
		float looseness = 0.8f;
		CompositeScoringGenerator composite =
				new CompositeScoringGenerator(
						String.format(
								"SuperBiclustering-%1d-%1d-%.2f-%s.scores.tsv",
								coreCount, minSize, minDensity, name),
								new RestrictiveGenerator(
										(int) (minDensity * minSize)),
										new BlockSplitterTransformer(coreCount, 5,
												minDensity),
												new MinHashTransformer(coreCount,
														looseness * minDensity, 3, 3, false,
														false, true),
														new IncreaseBlockDensityTransformer(
																coreCount, 0.75f, 0.75f),
																new SmallBiclusterFilter(coreCount, minSize,
																		minSize),
																		new RemoveLowSignificanceTransformer(0.01f),
																		new GreedyExpanderTransformer(coreCount,
																				minDensity * looseness, 1),
																				new IncreaseBlockDensityTransformer(
																						coreCount, minDensity * looseness,
																						minDensity * looseness),
																						new RemoveLowSignificanceTransformer(0.01f)
						.negativeCoExpression(false));

		return composite;
	}

	public GeneratorBlock getMinHashBiclusterer(String name) {
		float looseness = 0.8f;
		CompositeScoringGenerator composite =
				new CompositeScoringGenerator(
						String.format(
								"SuperBiclustering-%1d-%1d-%.2f-%s.scores.tsv",
								coreCount, minSize, minDensity, name),
								new BiclusterGenerator(minSize, minSize),
								new MinHashTransformer(coreCount,
										looseness * minDensity, 3, 3, false,
										false, true),
										new IncreaseBlockDensityTransformer(
												coreCount, 0.75f, 0.75f),
												new SmallBiclusterFilter(coreCount, minSize,
														minSize),
														new RemoveLowSignificanceTransformer(0.01f));

		return composite;
	}

	public GeneratorBlock getGreedySplitterBiclusterer(String name) {
		float looseness = 0.9f;
		CompositeScoringGenerator composite =
				new CompositeScoringGenerator(
						String.format(
								"SuperBiclustering-%1d-%1d-%.2f-%s.scores.tsv",
								coreCount, minSize, minDensity, name),
								new RestrictiveGenerator(
										(int) (minDensity * minSize)),
										new GreedyBlockSplitterTransformer()
						.setCoreCount(coreCount),
						new MinHashTransformer(coreCount,
								looseness * minDensity, 3, 3, false,
								false, true),
								new GreedyExpanderTransformer(coreCount,
										minDensity * looseness, 1),
										new IncreaseBlockDensityTransformer(
												coreCount, 0.80f, 0.80f),
												new SmallBiclusterFilter(coreCount, minSize,
														minSize),
														new RemoveLowSignificanceTransformer(0.01f)
						.negativeCoExpression(false));

		return composite;
	}

	public GeneratorBlock getRestrictedBiclusterer(String name) {
		float looseness = 0.9f;
		CompositeScoringGenerator composite =
				new CompositeScoringGenerator(
						String.format(
								"SuperBiclustering-%1d-%1d-%.2f-%s.scores.tsv",
								coreCount, minSize, minDensity, name),
								new RestrictedBiclusterGenerator(minSize,
										minSize, minSize),
										new MinHashTransformer(coreCount,
												looseness * minDensity, 3, 3, false,
												false, true),
												new GreedyExpanderTransformer(coreCount,
														minDensity * looseness, 1),
														new IncreaseBlockDensityTransformer(
																coreCount, 0.80f, 0.80f),
																new SmallBiclusterFilter(coreCount, minSize,
																		minSize),
																		new RemoveLowSignificanceTransformer(0.01f)
						.negativeCoExpression(false));

		return composite;
	}

	public GeneratorBlock getBBKPlusGenerator(String name) {
		float looseness = 0.9f;
		CompositeScoringGenerator composite =
				new CompositeScoringGenerator(
						String.format(
								"SuperBiclustering-%1d-%1d-%.2f-%s.scores.tsv",
								coreCount, minSize, minDensity, name),
								new BipartiteBronKerboschPlusGenerator()
						.setMinSize(5).setCoreCount(
								coreCount),
								new MinHashTransformer(coreCount,
										looseness * minDensity, 3, 3, false,
										false, true),
										new RemoveLowSignificanceTransformer(0.01f)
						.negativeCoExpression(false),
						new GreedyExpanderTransformer(coreCount,
								minDensity * looseness, 1),
								new IncreaseBlockDensityTransformer(
										coreCount, 0.80f, 0.80f),
										new SmallBiclusterFilter(coreCount, minSize,
												minSize),
												new RemoveLowSignificanceTransformer(0.01f)
						.negativeCoExpression(false))
		.logScores(false);

		return composite;
	}

	public GeneratorBlock
	getDegenerativeDrillBiclusterer(String name) {
		float looseness = 0.9f;
		CompositeScoringGenerator composite =
				new CompositeScoringGenerator(
						String.format(
								"SuperBiclustering-%1d-%1d-%.2f-%s.scores.tsv",
								coreCount, minSize, minDensity, name),
								new RestrictiveGenerator(
										(int) (minDensity * minSize)),
										new DegenerativeDrillTransformer(coreCount,
												minSize, minSize, minDensity, true),
												new MinHashTransformer(coreCount,
														looseness * minDensity, 3, 3, false,
														false, true),
														new IncreaseBlockDensityTransformer(
																coreCount, 0.80f, 0.80f),
																new SmallBiclusterFilter(coreCount, minSize,
																		minSize),
																		new RemoveLowSignificanceTransformer(0.01f),
																		new GreedyExpanderTransformer(coreCount,
																				minDensity * looseness, 1),
																				new IncreaseBlockDensityTransformer(
																						coreCount, minDensity * looseness,
																						minDensity * looseness),
																						new RemoveLowSignificanceTransformer(0.01f)
						.negativeCoExpression(false));

		return composite;
	}
}
