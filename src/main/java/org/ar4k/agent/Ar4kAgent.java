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
package org.ar4k.agent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe main per avvio Agente Ar4k
 *
 * @author Andrea Ambrosini
 */
public class Ar4kAgent {
	private static AppManager appManager = null;
	private static int identLevelStatusFile = 2;
	static boolean running = true;

	public static final long WHILE_DELAY = 60 * 1000L;

	public static AppManager getAppManager() {
		return appManager;
	}

	public static int getIdentLevelStatusFile() {
		return identLevelStatusFile;
	}

	public static boolean isRunning() {
		return running;
	}

	public static void main(final String[] args) {
		runApp();
	}

	private static void reset() throws Exception {
		if (appManager != null) {
			appManager.close();
			appManager = null;
		}
	}

	public static void runApp() {
		appManager = new AppManagerImplementation();
		System.out.println("agent started");
		System.out.println(appManager.toString());
		while (running) {
			try {
				writeStringToFile(appManager.getJsonStatus().toString(identLevelStatusFile),
						appManager.getStatusFilePath());
				Thread.sleep(WHILE_DELAY);
			} catch (final Exception e) {
				System.out.println("agent stopped");
				e.printStackTrace();
				System.exit(100);
			}
		}
		try {
			reset();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void setIdentLevelStatusFile(final int identLevelStatusFile) {
		Ar4kAgent.identLevelStatusFile = identLevelStatusFile;
	}

	public static void stopAgent() {
		Ar4kAgent.running = false;
	}

	public static final void writeStringToFile(final String text, final String fileName) throws IOException {
		final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(text);
		writer.close();
	}
}
