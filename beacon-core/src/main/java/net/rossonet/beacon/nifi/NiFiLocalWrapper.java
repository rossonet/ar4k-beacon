package net.rossonet.beacon.nifi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.rossonet.beacon.utils.LogHelper;

import com.github.hermannpencole.nifi.swagger.ApiClient;
import com.github.hermannpencole.nifi.swagger.Configuration;
import com.github.hermannpencole.nifi.swagger.client.AccessApi;

import net.rossonet.beacon.BeaconController;

public class NiFiLocalWrapper implements NiFiWrapper {

	/*
	 * I parametri di avvio sono visibili a questo link:
	 * https://nifi.apache.org/docs/nifi-docs/html/administration-guide.html
	 *
	 */

	private final class CallableManageLogo implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			while (!Files.exists(NIFI_LOGO_PATH)) {
				try {
					Thread.sleep(5000L);
					logger.info("wait for logo file...");
				} catch (final InterruptedException e) {
					logger.severe(LogHelper.stackTraceToString(e));
				}
			}
			try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("nifi.svg")) {
				Thread.sleep(30000L);
				Files.copy(is, NIFI_LOGO_PATH, StandardCopyOption.REPLACE_EXISTING);
				logger.info("wrote file " + NIFI_LOGO_PATH.toAbsolutePath().toString());
			} catch (final Exception e) {
				logger.severe(LogHelper.stackTraceToString(e));
			}
			return null;
		}
	}

	public static String DEFAULT_NIFI_DIRECTORY = "/opt/nifi/nifi-current";
	public static String DEFAULT_NIFI_STARTING_SCRIPT = "/opt/nifi/scripts/start.sh";
	public static String DEFAULT_NIFI_XML_ARCHIVE_DIRECTORY = DEFAULT_NIFI_DIRECTORY + "/conf/archive";
	public static String DEFAULT_NIFI_XML_FILE = DEFAULT_NIFI_DIRECTORY + "/conf/flow.xml.gz";
	public static Path NIFI_LOGO_PATH = Paths
			.get("/opt/nifi/nifi-current/work/jetty/nifi-web-ui-1.18.0.war/webapp/images/nifi-logo.svg");
	private static final Logger logger = Logger.getLogger(NiFiLocalWrapper.class.getName());
	private static final String NIFI_WEB_HTTP_PORT_PARAMETER = "NIFI_WEB_HTTP_PORT";

	private BeaconController beaconController;
	private Process nifiProcess;
	private final String pathNifiStartingScript;
	private AccessApi webClientService;

	private final File workingDirectory;

	public NiFiLocalWrapper() throws IOException {
		this(new File(DEFAULT_NIFI_DIRECTORY), DEFAULT_NIFI_STARTING_SCRIPT);
	}

	public NiFiLocalWrapper(final File workingDirectory, final String pathNifiStartingScript) throws IOException {
		this.workingDirectory = workingDirectory;
		this.pathNifiStartingScript = pathNifiStartingScript;
	}

	@Override
	public void close() throws IOException {
		stopNifi();

	}

	@Override
	public JSONObject getFlowXmlGzJSon() {
		// TODO ritorna l'attuale configurazione del Flow
		return null;
	}

	public Process getNifiProcess() {
		return nifiProcess;
	}

	public String getPathNifiStartingScript() {
		return pathNifiStartingScript;
	}

	@Override
	public AccessApi getWebClientApi() {
		return webClientService;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	@Override
	public void setController(final BeaconController beaconController) {
		this.beaconController = beaconController;

	}

	@Override
	public void startNifi() throws IOException {
		final ProcessBuilder nifiProcessBuilder = new ProcessBuilder(pathNifiStartingScript);
		nifiProcessBuilder.directory(workingDirectory);
		nifiProcessBuilder.environment().put(NIFI_WEB_HTTP_PORT_PARAMETER, "8080");
		// nifiProcessBuilder.environment().put("SINGLE_USER_CREDENTIALS_USERNAME",
		// "username");
		// nifiProcessBuilder.environment().put("SINGLE_USER_CREDENTIALS_USERNAME",
		// "password");
		nifiProcessBuilder.inheritIO();
		nifiProcess = nifiProcessBuilder.start();
		callLogoManager();
		createApiClient();
	}

	@Override
	public void stopNifi() {
		if (nifiProcess != null) {
			nifiProcess.destroy();
		}
	}

	@Override
	public void writeFlowXmlGzJson(final JSONObject decompressJson) {
		// TODO scrive l'attuale configurazione del Flow

	}

	private void callLogoManager() {
		final Callable<Void> callable = new CallableManageLogo();
		final FutureTask<Void> task = new FutureTask<>(callable);
		final Thread t = new Thread(task);
		t.start();
	}

	private void createApiClient() {
		webClientService = new AccessApi();
		final ApiClient defaultApiClient = Configuration.getDefaultApiClient();
		webClientService.setApiClient(defaultApiClient);
	}

}
