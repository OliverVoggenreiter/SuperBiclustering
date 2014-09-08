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

package map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.PrimeFinder;
import util.RandomHashFunction;

/**
 * MapController is a static class for executing MapAlgorithms on any
 * given input Collection. It is a simple way to use some of the
 * benefits of Map-Reduce without actually running a complex
 * maintenance service such as Hadoop. It can also be used in a
 * similar fashion to the typical "map" methods in other programming
 * languages such as python or perl. The MapController will accept an
 * algorithm and a set of data and will perform the algorithm on each
 * element of the data in parallel. In addition, input values are
 * grouped by their hash value so that potentially redundant work can
 * be avoided or the Map-Reduce paradigm can be implemented using the
 * KeyValuePair type.
 *
 * @author Oliver Voggenreiter
 * @date Wednesday, March 08, 2012
 */
public class MapController {

	private static final Logger logger = LoggerFactory
			.getLogger(MapController.class);

	private static int threadCount = 4;
	private static final MapWorkerFactory mapWorkerFactory =
			new MapWorkerFactory();
	private static ExecutorService threadPool = Executors
			.newFixedThreadPool(threadCount, mapWorkerFactory);

	// This parameter helps split the work into enough pieces so that
	// no single job is too large.
	private static final int partitionFactor = PrimeFinder
			.getNextPrime(1337);

	/**
	 * Takes a MapAlgorithm and a set of artibrarily ordered data. It
	 * subsequently partitions the input data into work packets which
	 * are given to a thread pool which handles the processing of the
	 * MapAlgorithm on each of the packets of data. Since the hash
	 * values of the input data are used to determine the
	 * partitioning, it is useful to use Key-Value pairs in order to
	 * guarantee certain arguments coming together that share the
	 * same key.
	 */
	public static <K, V> List<V> execute(
			MapAlgorithm<K, V> algorithm,
			Collection<? extends K> input) {
		logger.info("Input Size: " + input.size());
		Collection<List<K>> partitionedInput =
				partitionWork(input, threadCount);
		CompletionService<List<V>> pool =
				new ExecutorCompletionService<List<V>>(threadPool);

		for (List<K> partition : partitionedInput) {
			pool.submit(new MapTask<K, V>(algorithm, partition));
		}

		List<V> tuples = new ArrayList<V>();
		for (int i = 0; i < partitionedInput.size(); i++) {
			try {
				// TODO: Does adding many lists like this incur a
				// performance hit?
				// Perhaps we can return an iterable instead or some
				// special implementation
				// of a collection so that we do not have to resize
				// the tuples structure and
				// waste the allocation cost of each returned value.
				tuples.addAll(pool.take().get());
			} catch (InterruptedException e) {
				logger.error("Thread was interrupted!");
				e.printStackTrace();
			} catch (ExecutionException e) {
				logger.error("Exception during thread execution!");
				e.printStackTrace();
			}
		}

		logger.info("Output Size: " + tuples.size());

		return tuples;
	}

	/**
	 * Takes an arbitrarily ordered input and creates partitions of
	 * work from it using a hashing function to ensure items with the
	 * same hash come together for processing. This is especially
	 * useful when eliminating duplicates or when using a Key-Value
	 * system whereby two disjoint values may share the same Key and
	 * this is an indication that they should be treated together in
	 * subsequent processing (as in the Map-Reduce model).
	 */
	private static <T> Collection<List<T>> partitionWork(
			Collection<? extends T> input, int workerCount) {
		Map<Integer, List<T>> workPartitions =
				new HashMap<Integer, List<T>>();

		if (workerCount <= 0) {
			throw new IllegalArgumentException(
					"Must set more than 0 threads for MapController!");
		}
		if (input == null) {
			throw new NullPointerException(
					"Input to MapController was null!");
		}
		if (input.size() == 0) {
			throw new IllegalArgumentException(
					"Input to MapController was empty!");
		}

		RandomHashFunction jobHasher =
				new RandomHashFunction(workerCount * partitionFactor);

		for (T workItem : input) {
			int key = jobHasher.getHash(workItem);
			if (!workPartitions.containsKey(key)) {
				workPartitions.put(key, new ArrayList<T>());
			}
			workPartitions.get(key).add(workItem);
		}

		return workPartitions.values();
	}

	/**
	 * Used to change the number of threads used in the Thread Pool
	 * for the MapController. Only creates a new Fixed Size Thread
	 * Pool if the number of requested threads is actually different
	 * than previously set.
	 */
	public static void setThreadCount(int threadCount) {
		if (threadCount <= 0) {
			throw new IllegalArgumentException(
					"Cannot set less than 1 thread in MapController.");
		}
		if (MapController.threadCount != threadCount) {
			logger.info(String
					.format("Map Controller thread count changed! %1d -> %1d",
							MapController.threadCount, threadCount));
			MapController.threadCount = threadCount;
			threadPool =
					Executors.newFixedThreadPool(threadCount,
							mapWorkerFactory);
		}
	}

	public static int getThreadCount() {
		return threadCount;
	}

	/**
	 * Used to safely cleanup the thread pool after the garbage
	 * collector begins retrieving resources from the MapController.
	 */
	@Override
	public void finalize() {
		threadPool.shutdownNow();
	}
}
