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

import org.rossonet.beacon.utils.LogHelper;

public class NiFiWrapper {

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

	private static final Logger logger = Logger.getLogger(NiFiWrapper.class.getName());
	public static Path NIFI_LOGO_PATH = Paths
			.get("/opt/nifi/nifi-current/work/jetty/nifi-web-ui-1.18.0.war/webapp/images/nifi-logo.svg");
	public static String DEFAULT_NIFI_DIRECTORY = "/opt/nifi/nifi-current";
	public static String DEFAULT_NIFI_STARTING_SCRIPT = "/opt/nifi/scripts/start.sh";

	private final Process nifiProcess;

	public NiFiWrapper() throws IOException {
		this(new File(DEFAULT_NIFI_DIRECTORY), DEFAULT_NIFI_STARTING_SCRIPT);
	}

	public NiFiWrapper(final File workingDirectory, final String pathNifiStartingScript) throws IOException {
		final ProcessBuilder nifiProcessBuilder = new ProcessBuilder(pathNifiStartingScript);
		nifiProcessBuilder.directory(workingDirectory);
		nifiProcessBuilder.environment().put("NIFI_WEB_HTTP_PORT", "8080");
		// nifiProcessBuilder.environment().put("SINGLE_USER_CREDENTIALS_USERNAME",
		// "username");
		// nifiProcessBuilder.environment().put("SINGLE_USER_CREDENTIALS_USERNAME",
		// "password");
		nifiProcessBuilder.inheritIO();
		nifiProcess = nifiProcessBuilder.start();
		callLogoManager();

	}

	private void callLogoManager() {
		final Callable<Void> callable = new CallableManageLogo();
		final FutureTask<Void> task = new FutureTask<>(callable);
		final Thread t = new Thread(task);
		t.start();
	}

	public void stopNifi() {
		if (nifiProcess != null) {
			nifiProcess.destroy();
		}
	}

}
