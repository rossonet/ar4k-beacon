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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import net.rossonet.beacon.BeaconController;
import net.rossonet.beacon.flink.FlinkLocalWrapper;
import net.rossonet.beacon.keycloak.KeycloakRemoteWrapper;
import net.rossonet.beacon.milo.OpcUaServerParameters;
import net.rossonet.beacon.milo.OpcUaServerStorage;
import net.rossonet.beacon.nifi.NiFiLocalWrapper;
import net.rossonet.beacon.web.BeaconWebAppLocalWrapper;
import net.rossonet.beacon.zeppelin.ZeppelinLocalWrapper;

/**
 * Classe main per avvio
 *
 * @author Andrea Ambrosini
 */
public class Beaconctl {

	private static final Logger logger = Logger.getLogger(Beaconctl.class.getName());
	private static final int SYSTEM_EXIT_GENERAL_ERROR = 300;

	public static void main(final String[] args) {
		try {
			final Beaconctl beaconctl = new Beaconctl(args);
			beaconctl.runApp();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(SYSTEM_EXIT_GENERAL_ERROR);
		}
	}

	private final String[] commanLineArgs;
	// TODO completare esecuzione fuori da container Rossonet
	private boolean isInRossonetContainer = true;

	private final OpcUaServerParameters opcUAParameters = OpcUaServerParameters.getDockerRossonetParameters();

	private int parallelThreads = Runtime.getRuntime().availableProcessors();
	private final Map<String, Object> storageMap = new HashMap<>();

	private boolean wipeContentAtEnd = true;

	public Beaconctl(final String[] args) {
		this.commanLineArgs = args;
	}

	public String[] getCommanLineArgs() {
		return commanLineArgs;
	}

	public OpcUaServerParameters getOpcUAParameters() {
		return opcUAParameters;
	}

	public int getParallelThreads() {
		return parallelThreads;
	}

	public boolean isInRossonetContainer() {
		return isInRossonetContainer;
	}

	public boolean isWipeContentAtEnd() {
		return wipeContentAtEnd;
	}

	public void runApp() throws InstantiationException, IllegalAccessException {
		Thread.currentThread().setName("beacon-main");
		BeaconController beaconController = null;
		if (isInRossonetContainer) {
			logger.info("starting beaconctl");
			beaconController = new BeaconController(Executors.newFixedThreadPool(parallelThreads), opcUAParameters,
					new OpcUaServerStorage() {
						@Override
						public Map<String, Object> getMap() {
							return storageMap;
						}
					}, KeycloakRemoteWrapper.class, NiFiLocalWrapper.class, ZeppelinLocalWrapper.class,
					FlinkLocalWrapper.class, BeaconWebAppLocalWrapper.class);
		}
		beaconController.start();
		logger.info("starting process completed. Now running");
		beaconController.waitTermination();
		if (wipeContentAtEnd) {
			beaconController.wipe();
		}
	}

	private void setInRossonetContainer(final boolean isInRossonetContainer) {
		this.isInRossonetContainer = isInRossonetContainer;
	}

	private void setParallelThreads(final int parallelThreads) {
		this.parallelThreads = parallelThreads;
	}

	private void setWipeContentAtEnd(final boolean wipeContentAtEnd) {
		this.wipeContentAtEnd = wipeContentAtEnd;
	}

}
