package net.rossonet.beacon.nifi;

import java.io.File;
import java.io.IOException;

public class NiFiWrapper {

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
	}

	public void stopNifi() {
		if (nifiProcess != null) {
			nifiProcess.destroy();
		}
	}

}
