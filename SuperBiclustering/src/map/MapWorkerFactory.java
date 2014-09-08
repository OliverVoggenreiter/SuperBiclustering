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

import java.util.concurrent.ThreadFactory;

/**
 * MapWorkerFactory allows to create "Daemon" type threads that
 * automatically relinquish their resources after the main thread of
 * a java application has terminated. This prevents the
 * non-terminating state that occurs when using non-daemon threads.
 *
 * @author "Oliver Voggenreiter"
 * @date Mar 5, 2013
 *
 */
public class MapWorkerFactory implements ThreadFactory {

	@Override
	public Thread newThread(Runnable runnable) {
		if (runnable == null)
			throw new NullPointerException(
					"Null Runnable passed to MapWorkerFactory!");
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	}

}
