package net.rossonet.beacon.nifi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.rossonet.utils.LogHelper;

import com.github.hermannpencole.nifi.swagger.ApiClient;
import com.github.hermannpencole.nifi.swagger.Configuration;
import com.github.hermannpencole.nifi.swagger.client.AccessApi;

import net.rossonet.beacon.BeaconController;
import net.rossonet.beacon.utils.SynchronizeHelper;

public class NiFiLocalWrapper implements NiFiWrapper {

	private final class CallablePostBoot implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			while (!Files.exists(NIFI_LOGO_PATH)) {
				try {
					Thread.sleep(5000L);
					logger.info("wait for completed statrtup of NiFi");
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

	/*
	 * I parametri di avvio sono visibili a questo link:
	 * https://nifi.apache.org/docs/nifi-docs/html/administration-guide.html
	 *
	 */

	public static String DEFAULT_NIFI_XML_ARCHIVE_DIRECTORY = DEFAULT_NIFI_DIRECTORY + "/conf/archive";

	public static String DEFAULT_NIFI_XML_FILE = DEFAULT_NIFI_DIRECTORY + "/conf/flow.xml.gz";
	public static Path NIFI_LOGO_PATH = Paths
			.get("/opt/nifi/nifi-current/work/jetty/nifi-web-ui-1.18.0.war/webapp/images/nifi-logo.svg");
	private static final String DEFAULT_NIFI_PORT = "8080";
	private static final String DEFAULT_SECRET_KEY_ARCHIVE = "p3er4Lt6YmlI";
	private static final Logger logger = Logger.getLogger(NiFiLocalWrapper.class.getName());
	private static final String NIFI_API_PATH = "/nifi-api";
	private static final String NIFI_SENSITIVE_PROPS_KEY_PARAMETER = "NIFI_SENSITIVE_PROPS_KEY";

	private static final String NIFI_STORAGE_CONTENT_DIRECTORY = "/nifi";

	private static final String NIFI_WEB_HTTP_PORT_PARAMETER = "NIFI_WEB_HTTP_PORT";

	private static String getHostName() {
		String hostName = "127.0.0.1";
		try {
			final InetAddress inetAddress = InetAddress.getLocalHost();
			hostName = inetAddress.getHostName();
		} catch (final UnknownHostException e) {
			logger.severe(LogHelper.stackTraceToString(e));
		}
		return hostName;
	}

	private BeaconController beaconController;
	private Process nifiProcess;
	private final String pathNifiStartingScript;

	private final String secretKeyArchive;

	private final TimerTask synchronizeConfigTask = new TimerTask() {

		@Override
		public void run() {
			try {
				synchronizeNiFiArchiveToBeaconStorage();
			} catch (final Exception a) {
				logger.severe(LogHelper.stackTraceToString(a));
			}

		}
	};

	private final Timer timer = new Timer();

	private AccessApi webClientService;

	private final File workingDirectory;

	public NiFiLocalWrapper() throws IOException {
		this(new File(DEFAULT_NIFI_DIRECTORY), DEFAULT_NIFI_STARTING_SCRIPT, DEFAULT_SECRET_KEY_ARCHIVE);
	}

	public NiFiLocalWrapper(final File workingDirectory, final String pathNifiStartingScript,
			final String secretKeyArchive) throws IOException {
		this.workingDirectory = workingDirectory;
		this.pathNifiStartingScript = pathNifiStartingScript;
		this.secretKeyArchive = secretKeyArchive;
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
		nifiProcessBuilder.environment().put(NIFI_WEB_HTTP_PORT_PARAMETER, DEFAULT_NIFI_PORT);
		nifiProcessBuilder.environment().put(NIFI_SENSITIVE_PROPS_KEY_PARAMETER, secretKeyArchive);
		synchronizeBeaconStorageToNiFiArchive();
		nifiProcessBuilder.inheritIO();
		nifiProcess = nifiProcessBuilder.start();
		callLogoManager();
		createApiClient();
		timer.schedule(synchronizeConfigTask, 60000, 30000);
	}

	@Override
	public void stopNifi() {
		synchronizeNiFiArchiveToBeaconStorage();
		if (nifiProcess != null) {
			nifiProcess.destroy();
		}
	}

	@Override
	public void writeFlowXmlGzJson(final JSONObject decompressJson) {
		// TODO scrive l'attuale configurazione del Flow

	}

	private void callLogoManager() {
		final Callable<Void> callable = new CallablePostBoot();
		final FutureTask<Void> task = new FutureTask<>(callable);
		final Thread t = new Thread(task);
		t.setName("nifi-post");
		t.start();
	}

	private void createApiClient() {
		webClientService = new AccessApi();
		final ApiClient defaultApiClient = Configuration.getDefaultApiClient();
		webClientService.setApiClient(
				defaultApiClient.setBasePath("http://" + getHostName() + ":" + DEFAULT_NIFI_PORT + NIFI_API_PATH));
	}

	private void synchronizeBeaconStorageToNiFiArchive() {
		final Path targetPath = Paths.get(DEFAULT_NIFI_XML_FILE);
		final Path sourcePath = Paths.get(BeaconController.DEFAULT_STORAGE_DIRECTORY + NIFI_STORAGE_CONTENT_DIRECTORY);
		try {
			final String report = SynchronizeHelper.copyLastFileToXmlFile(sourcePath, targetPath);
			logger.info(report);
		} catch (final Exception e) {
			logger.severe(LogHelper.stackTraceToString(e));
		}
	}

	private void synchronizeNiFiArchiveToBeaconStorage() {
		final Path sourcePath = Paths.get(DEFAULT_NIFI_XML_ARCHIVE_DIRECTORY);
		final Path targetPath = Paths.get(BeaconController.DEFAULT_STORAGE_DIRECTORY + NIFI_STORAGE_CONTENT_DIRECTORY);
		try {
			String report = SynchronizeHelper.synchronizeDirectories(sourcePath, targetPath);
			report = report + " " + SynchronizeHelper.deleteOldFilesInDirectory(targetPath);
			logger.info(report);
		} catch (final Exception e) {
			logger.severe(LogHelper.stackTraceToString(e));
		}
	}

}
