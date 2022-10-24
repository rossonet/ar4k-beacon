/*
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */
package net.rossonet.beacon.beaconctl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import org.rossonet.beacon.utils.LogHelper;

import net.rossonet.beacon.nifi.NiFiWrapper;

/**
 * Classe main per avvio
 *
 * @author Andrea Ambrosini
 */
public class Beaconctl {

	private static int identLevelStatusFile = 2;

	private static final Logger logger = Logger.getLogger(Beaconctl.class.getName());
	private static boolean running = true;

	public static final long WHILE_DELAY = 60 * 1000L;

	public static int getIdentLevelStatusFile() {
		return identLevelStatusFile;
	}

	public static boolean isRunning() {
		return running;
	}

	public static void main(final String[] args) {
		runApp(args);
	}

	public static void runApp(final String[] args) {
		Thread.currentThread().setName("beacon-main");
		logger.info("starting beaconctl");
		logger.info("starting Apache NiFi");
		NiFiWrapper nifi = null;
		try {
			nifi = new NiFiWrapper();
		} catch (final IOException e1) {
			logger.severe("exception starting Apache NiFi\n" + LogHelper.stackTraceToString(e1));
		}
		while (running) {
			try {
				Thread.sleep(WHILE_DELAY);
			} catch (final InterruptedException e) {
				logger.severe("exception in main thread\n" + LogHelper.stackTraceToString(e));
			}
		}
		nifi.stopNifi();
		// TODO metodo avvio

	}

	public static void setIdentLevelStatusFile(final int identLevelStatusFile) {
		Beaconctl.identLevelStatusFile = identLevelStatusFile;
	}

	public static void stopBeaconctl() {
		Beaconctl.running = false;
	}

	public static final void writeStringToFile(final String text, final String fileName) throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(text);
		writer.close();
	}

}
