package net.rossonet.beacon;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import org.rossonet.utils.LogHelper;

import com.github.hermannpencole.nifi.swagger.ApiException;
import com.github.hermannpencole.nifi.swagger.client.model.AccessStatusEntity;

import net.rossonet.beacon.keycloak.KeycloakWrapper;
import net.rossonet.beacon.milo.OpcUaServerParameters;
import net.rossonet.beacon.milo.OpcUaServerStorage;
import net.rossonet.beacon.milo.OpcUaServerWrapper;
import net.rossonet.beacon.nifi.NiFiWrapper;
import net.rossonet.beacon.web.BeaconWebAppWrapper;

public class BeaconController implements AutoCloseable {
	public static final String DEFAULT_STORAGE_DIRECTORY = "/beacon-data";
	public static final long WHILE_DELAY = 60 * 1000L;
	private static final Logger logger = Logger.getLogger(BeaconController.class.getName());
	private final ExecutorService executorService;

	private final KeycloakWrapper keycloakWrapper;

	private boolean niFiCheckOk = false;

	private final NiFiWrapper nifiWrapper;

	private final OpcUaServerWrapper opcUaServer;

	private boolean running = false;

	private final BeaconWebAppWrapper wepAppWrapper;

	public BeaconController(final ExecutorService executorService, final OpcUaServerParameters opcUaServerParameters,
			final OpcUaServerStorage opcUaServerStorage, final Class<? extends KeycloakWrapper> keycloakWrapperClass,
			final Class<? extends NiFiWrapper> niFiWrapperClass,
			final Class<? extends BeaconWebAppWrapper> wepAppWrapperClass)
			throws InstantiationException, IllegalAccessException {
		this.executorService = executorService;
		opcUaServer = new OpcUaServerWrapper(this, opcUaServerParameters, opcUaServerStorage);
		// TODO completare wrapper Keycloak
		keycloakWrapper = keycloakWrapperClass.newInstance();
		keycloakWrapper.setController(this);
		nifiWrapper = niFiWrapperClass.newInstance();
		nifiWrapper.setController(this);
		// TODO completare web app Beacon
		wepAppWrapper = wepAppWrapperClass.newInstance();
		wepAppWrapper.setController(this);
		startOpcUaServer();
	}

	@Override
	public void close() throws Exception {
		stop();
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public KeycloakWrapper getKeycloakWrapper() {
		return keycloakWrapper;
	}

	public NiFiWrapper getNifiWrapper() {
		return nifiWrapper;
	}

	public OpcUaServerWrapper getOpcUaServer() {
		return opcUaServer;
	}

	public BeaconWebAppWrapper getWepAppWrapper() {
		return wepAppWrapper;
	}

	public boolean isRunning() {
		return running;
	}

	public void start() {
		startKeycloak();
		startNifi();
		startWebApp();
		running = true;
	}

	public void stop() {
		running = false;
		try {
			if (opcUaServer != null) {
				opcUaServer.stopOpcServer();
			}
		} catch (final Exception e) {
			logger.severe("exception stopping opcUaServer\n" + LogHelper.stackTraceToString(e));
		}
		try {
			if (wepAppWrapper != null) {
				wepAppWrapper.stopWebApp();
			}
		} catch (final Exception e) {
			logger.severe("exception stopping wepAppWrapper\n" + LogHelper.stackTraceToString(e));
		}

		try {
			if (nifiWrapper != null) {
				nifiWrapper.stopNifi();
			}
		} catch (final Exception e) {
			logger.severe("exception stopping nifiWrapper\n" + LogHelper.stackTraceToString(e));
		}

		try {
			if (keycloakWrapper != null) {
				keycloakWrapper.stopKeycloak();
			}
		} catch (final Exception e) {
			logger.severe("exception stopping keycloakWrapper\n" + LogHelper.stackTraceToString(e));
		}
	}

	public void waitTermination() {
		while (running) {
			try {
				Thread.sleep(WHILE_DELAY);
				tryNifiClient();
			} catch (final InterruptedException e) {
				logger.severe("interrupted wait thread\n" + LogHelper.stackTraceToString(e));
			}

		}

	}

	public void wipe() {
		// TODO implementare distruzione file locali

	}

	private void fireNifiError(final Exception e) {
		// TODO verificare come comportarsi in casi di mancata risposta di NiFi
		logger.severe("exception trying NiFi API client\n" + LogHelper.stackTraceToString(e, 15));

	}

	private void fireNifiRestore(final AccessStatusEntity accessStatusEntity) {
		logger.info("AccessStatus to NiFi " + accessStatusEntity.toString());
	}

	private void startKeycloak() {

		executorService.submit(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.currentThread().setName("keycloak-wrapper");
					logger.info("starting RedHat Keycloak");
					keycloakWrapper.startKeycloak();
				} catch (final Exception e) {
					logger.severe("exception in RedHat Keycloak\n" + LogHelper.stackTraceToString(e));
				}

			}
		});

	}

	private void startNifi() {

		executorService.submit(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.currentThread().setName("nifi-wrapper");
					logger.info("starting Apache NiFi");
					nifiWrapper.startNifi();
				} catch (final Exception e) {
					logger.severe("exception in Apache NiFi\n" + LogHelper.stackTraceToString(e));
				}

			}
		});

	}

	private void startOpcUaServer() {
		opcUaServer.startOpcServer();

	}

	private void startWebApp() {

		executorService.submit(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.currentThread().setName("webapp-wrapper");
					logger.info("starting Beacon Webapp");
					wepAppWrapper.startWebApp();
				} catch (final Exception e) {
					logger.severe("exception in Beacon Webapp\n" + LogHelper.stackTraceToString(e));
				}

			}
		});

	}

	private void tryNifiClient() {
		try {
			final AccessStatusEntity result = nifiWrapper.getWebClientApi().getAccessStatus();
			if (!niFiCheckOk) {
				fireNifiRestore(result);
			}
			niFiCheckOk = true;

		} catch (final ApiException a) {
			logger.severe("trying NiFi Client API access\n" + LogHelper.stackTraceToString(a, 4));
			for (final Entry<String, List<String>> header : a.getResponseHeaders().entrySet()) {
				logger.severe(" - " + header.getKey() + " => " + String.join(", ", header.getValue()));
			}

		} catch (final Exception e) {
			if (niFiCheckOk) {
				fireNifiError(e);
			} else {
				logger.severe("trying NiFi Client API access\n" + LogHelper.stackTraceToString(e, 2));
			}
			niFiCheckOk = false;

		}
	}

}
