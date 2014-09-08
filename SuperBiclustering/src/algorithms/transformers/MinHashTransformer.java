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

package algorithms.transformers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import map.KeyValuePair;
import map.MapController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.BiclusterUtils;
import util.RandomHashFunction;
import algorithms.TransformerBlock;
import datatype.bicluster.Bicluster;
import datatype.matrix.BinaryMatrix;

/**
 * The Min-Hash Transformer takes a set of blocks and attempts to
 * find possible merge sets of these blocks. This is done using the
 * usual paradigm of min-hashing by using one or more of three types
 * of identifying indices of the biclusters. The first is the
 * internal index, which simply uses each one's position inside the
 * bicluster as an index; second is the external index which looks at
 * only ones that occur outside the bicluster but are along its
 * row/col axes (i.e. the one must either contain a row or a column
 * in common with the bicluster); finally there is the simple
 * indexing, which simply uses the rows and columns of the bicluster
 * as indices for hashing.
 *
 * In order to limit the computational effort of the transformer, an
 * artificial limit is placed on the number of input biclusters. If
 * there are more biclusters than this threshold, a random sample of
 * the input is taken.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 11, 2013
 *
 */
public class MinHashTransformer extends TransformerBlock {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MinHashTransformer.class);

	private int bandCount;
	private int hashesPerBand;
	private int coreCount;
	private float minDensity;
	private boolean useOverlap;
	private boolean useCrossover;
	private boolean useRowsColumns;

	private final static int MAX_BICLUSTER_SAMPLES = 100000000;

	public MinHashTransformer(int coreCount, float minDensity,
			int numberOfBands, int hashesPerBand,
			boolean useOverlap, boolean useCrossover,
			boolean useRowsColumns) {
		this.coreCount = coreCount;
		this.minDensity = minDensity;
		this.bandCount = numberOfBands;
		this.hashesPerBand = hashesPerBand;
		this.useOverlap = useOverlap;
		this.useCrossover = useCrossover;
		this.useRowsColumns = useRowsColumns;
		LOGGER.info(String
				.format("Created Min-Hashing Transformer with %1d bands with %1d hashes each.",
						this.bandCount, this.hashesPerBand));
	}

	@Override
	public String getName() {
		return String
				.format("Min-Hash Transformer - Cores:%1d - Min Density:%.2f - Bands:%1d - Hashes/Band:%1d",
						coreCount, minDensity, bandCount,
						hashesPerBand);
	}

	@Override
	public String getShortName() {
		return String.format("MHT%.2f-%1d-%1d", minDensity,
				bandCount, hashesPerBand);
	}

	@Override
	public Collection<Bicluster> findBlocks(
			Collection<? extends Bicluster> biclusters,
			BinaryMatrix matrix) {
		LOGGER.info("###### Min-Hash Transformer ######");

		long time = System.currentTimeMillis();

		Collection<? extends Bicluster> biclusterPool =
				sampleBiclusters(biclusters);

		LOGGER.debug("Generating Random Hashing Functions....");
		List<RandomHashFunction> randomHashFunctions =
				getRandomHashFunctions(bandCount * hashesPerBand,
						matrix.getNumRows() * matrix.getNumColumns());

		MapController.setThreadCount(coreCount);

		LOGGER.debug("Calculating Hashes for each bicluster....");
		List<KeyValuePair<Integer, KeyValuePair<Bicluster, List<Integer>>>> tuples =
				MapController.execute(new MinHashBiclustersTask(
						randomHashFunctions, bandCount, matrix,
						useOverlap, useCrossover, useRowsColumns),
						biclusterPool);
		LOGGER.debug("Reorganizing Hashbands for group extraction...");
		List<KeyValuePair<Integer, Map<Set<Integer>, List<Bicluster>>>> bandHashTables =
				MapController.execute(
						new MinHasherBiclusterReduceTask(), tuples);

		LOGGER.debug("Calculating groups of biclusters....");
		List<List<Bicluster>> tuples2 =
				MapController.execute(
						new MinHasherSimilarBiclusterExtractTask(),
						bandHashTables);
		LOGGER.debug("Merging groups of biclusters....");
		List<Bicluster> superBiclusters =
				MapController.execute(
						new MinHasherSimilarBiclusterMergeTask(
								minDensity, matrix), tuples2);

		reportBiggestBiclusters(superBiclusters, matrix);

		time = System.currentTimeMillis() - time;
		LOGGER.debug("Number of SuperBiclusters: "
				+ superBiclusters.size());
		LOGGER.debug("MH-RUNTIME: " + time + " ms");
		LOGGER.info("##################################");
		return superBiclusters;
	}

	private void reportBiggestBiclusters(List<Bicluster> biclusters,
			BinaryMatrix matrix) {
		List<Bicluster> sortedBiclusters =
				BiclusterUtils.sortByArea(biclusters);
		LOGGER.debug("Biggest SuperBicluster Sets:");
		for (int i = 0; i < 5 && i < sortedBiclusters.size(); i++) {
			LOGGER.debug("SB"
					+ i
					+ ": "
					+ sortedBiclusters.get(i).getNumberOfRows()
					+ " rows by "
					+ sortedBiclusters.get(i).getNumberOfColumns()
					+ " columns with "
					+ BiclusterUtils.getDensity(sortedBiclusters
							.get(i), matrix) + " % density!");
		}
	}

	private Collection<? extends Bicluster> sampleBiclusters(
			Collection<? extends Bicluster> biclusters) {
		Collection<Bicluster> sampledBiclusters =
				new ArrayList<Bicluster>();
		float keep_ratio = 1.1f;
		if (biclusters.size() > MAX_BICLUSTER_SAMPLES) {
			LOGGER.warn(String
					.format("Too many biclusters as input (%1d), taking random sample...",
							biclusters.size()));
			keep_ratio =
					(float) MAX_BICLUSTER_SAMPLES
					/ biclusters.size();
		} else {
			return biclusters;
		}
		Random rand = new Random(696);
		for (Bicluster bicluster : biclusters) {
			if (rand.nextFloat() < keep_ratio) {
				sampledBiclusters.add(bicluster);
			}
		}
		LOGGER.debug(String.format(
				"Continuing with %1d random biclusters",
				sampledBiclusters.size()));
		return sampledBiclusters;
	}

	private List<RandomHashFunction> getRandomHashFunctions(
			int numberOfHashFunctions, int targetBinSize) {
		List<RandomHashFunction> hashFunctions =
				new ArrayList<RandomHashFunction>();

		for (int i = 0; i < numberOfHashFunctions; i++) {
			hashFunctions.add(new RandomHashFunction(i,
					targetBinSize));
		}

		return hashFunctions;
	}

}
