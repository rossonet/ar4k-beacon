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
		// TODO metodo avvio
	}

	public static void setIdentLevelStatusFile(final int identLevelStatusFile) {
		Beaconctl.identLevelStatusFile = identLevelStatusFile;
	}

	public static void stopSavuctl() {
		Beaconctl.running = false;
	}

	public static final void writeStringToFile(final String text, final String fileName) throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(text);
		writer.close();
	}

}
