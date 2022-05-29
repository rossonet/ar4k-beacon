package org.ar4k.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ar4k.beacon.service.ConfigurationRefresh;
import org.json.JSONObject;

public class AppManagerImplementation implements AppManager {

	private static final Logger logger = Logger.getLogger(AppManagerImplementation.class.getName());

	private static String getFileChecksum(final MessageDigest digest, final File file) throws IOException {
		final FileInputStream fis = new FileInputStream(file);
		final byte[] byteArray = new byte[1024];
		int bytesCount = 0;
		while ((bytesCount = fis.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		}
		fis.close();
		final byte[] bytes = digest.digest();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	private String cacheDirectoryPath = "cache";
	private String cacheDirectoryPathEnviroment = "AR4K_CACHE_DIRECTORY_PATH";
	private final Timer checkConfigTimer = new Timer("config-refresh");
	private String configDirectoryPath = "configuration";
	private String configDirectoryPathEnviroment = "AR4K_CONFIG_DIRECTORY_PATH";

	private String configExtension = ".ar4k.conf";
	private String configExtensionEnviroment = "AR4K_CONFIG_EXTENSION";

	private final Map<String, String> configHashs = new HashMap<>();
	private final Map<String, JSONObject> configJsons = new HashMap<>();
	private String configVariableRegularExpressionEnd = "\\}\\!";
	private String configVariableRegularExpressionEnviromentEnd = "AR4K_CONFIG_VARIABLE_REGULAR_EXPRESSION_END";

	private String configVariableRegularExpressionEnviromentStart = "AR4K_CONFIG_VARIABLE_REGULAR_EXPRESSION_START";

	private String configVariableRegularExpressionStart = "\\!\\{";

	private Pattern patternVariableInConfiguration = null;

	private final Set<ConfigurationRefresh> services = new HashSet<>();

	private MessageDigest shaDigest = null;

	private String statusFilePath = "status.json";

	private String statusFilePathEnviroment = "AR4K_STATUS_FILE_PATH";

	private final Map<String, String> statusValues = new HashMap<>();
	private final TimerTask task = new TimerTask() {
		@Override
		public void run() {
			checkConfigs();
		}

	};

	public AppManagerImplementation() {
		readVariablesFromEnviroment();
		try {
			shaDigest = MessageDigest.getInstance("SHA-256");
		} catch (final NoSuchAlgorithmException e) {
			logger.severe("error in creation of MessageDigest " + e.getMessage());
		}
		checkConfigs();
		checkConfigTimer.schedule(task, Ar4kAgent.WHILE_DELAY, Ar4kAgent.WHILE_DELAY);
	}

	private void checkConfigs() {
		boolean configChanged = false;
		final Set<String> configFounds = new HashSet<>();
		final File configurationDirectory = new File(replaceHomeDirectory(configDirectoryPath));
		if (configurationDirectory.exists() && configurationDirectory.isDirectory()) {
			for (final File fileInside : configurationDirectory.listFiles()) {
				if (elaborateSingleConfigFile(configFounds, fileInside)) {
					configChanged = true;
				}
			}
			final Set<String> toDelete = new HashSet<>();
			for (final String i : configJsons.keySet()) {
				if (!configFounds.contains(i)) {
					toDelete.add(i);
				}
			}
			for (final String del : toDelete) {
				configJsons.remove(del);
			}
		}
		if (configChanged) {
			logger.info("new configuration found");
			reload();
		}
	}

	private String checkEnviroment(final String enviromentVariable) {
		return System.getenv(enviromentVariable);
	}

	@Override
	public void close() throws Exception {
		configHashs.clear();
		configJsons.clear();
		statusValues.clear();
	}

	private boolean elaborateSingleConfigFile(final Set<String> configFounds, final File fileInside) {
		boolean configChanged = false;
		if (!fileInside.isDirectory() && fileInside.toString().endsWith(configExtension)) {
			final String key = fileInside.getAbsolutePath().toString();
			String shaChecksum = "null-hash";
			configFounds.add(key);
			try {
				shaChecksum = getFileChecksum(shaDigest, fileInside);
			} catch (final IOException e) {
				logger.severe("error in hash alghoritm " + e.getMessage());
			}
			if (!configHashs.containsKey(key) || !configHashs.get(key).equals(shaChecksum)) {
				configHashs.put(key, shaChecksum);
				configChanged = true;
				try {
					final String data = new String(Files.readAllBytes(fileInside.toPath()), StandardCharsets.UTF_8);
					configJsons.put(key, new JSONObject(replaceEnviromentInString(data)));
				} catch (final IOException e) {
					logger.severe("error in read of file " + fileInside + " " + e.getMessage());
				}
			}
		}
		return configChanged;
	}

	@Override
	public String getCacheDirectoryPath() {
		return Paths.get(cacheDirectoryPath).toAbsolutePath().toString();
	}

	@Override
	public String getCacheDirectoryPathEnviroment() {
		return cacheDirectoryPathEnviroment;
	}

	@Override
	public String getConfigDirectoryPath() {
		return Paths.get(configDirectoryPath).toAbsolutePath().toString();
	}

	@Override
	public String getConfigDirectoryPathEnviroment() {
		return configDirectoryPathEnviroment;
	}

	@Override
	public String getConfigExtension() {
		return configExtension;
	}

	@Override
	public String getConfigExtensionEnviroment() {
		return configExtensionEnviroment;
	}

	public Map<String, String> getConfigHashs() {
		return configHashs;
	}

	@Override
	public Map<String, JSONObject> getConfigJsons() {
		return configJsons;
	}

	@Override
	public Collection<JSONObject> getConfigs() {
		return configJsons.values();
	}

	public String getConfigVariableRegularExpressionEnd() {
		return configVariableRegularExpressionEnd;
	}

	public String getConfigVariableRegularExpressionEnviromentEnd() {
		return configVariableRegularExpressionEnviromentEnd;
	}

	public String getConfigVariableRegularExpressionEnviromentStart() {
		return configVariableRegularExpressionEnviromentStart;
	}

	public String getConfigVariableRegularExpressionStart() {
		return configVariableRegularExpressionStart;
	}

	@Override
	public JSONObject getJsonStatus() {
		final JSONObject status = new JSONObject();
		status.put(STATUS_FIELD, OK_STATUS);
		for (final Entry<String, String> d : statusValues.entrySet()) {
			status.put(d.getKey(), d.getValue());
		}
		return status;
	}

	@Override
	public String getStatusFilePath() {
		return Paths.get(statusFilePath).toAbsolutePath().toString();
	}

	@Override
	public String getStatusFilePathEnviroment() {
		return statusFilePathEnviroment;
	}

	private void readVariablesFromEnviroment() {
		if (checkEnviroment(statusFilePathEnviroment) != null) {
			setStatusFilePath(checkEnviroment(statusFilePathEnviroment));
		}
		if (checkEnviroment(configDirectoryPathEnviroment) != null) {
			setConfigDirectoryPath(checkEnviroment(configDirectoryPathEnviroment));
		}
		if (checkEnviroment(cacheDirectoryPathEnviroment) != null) {
			setCacheDirectoryPath(checkEnviroment(cacheDirectoryPathEnviroment));
		}
		if (checkEnviroment(configExtensionEnviroment) != null) {
			setConfigExtension(checkEnviroment(configExtensionEnviroment));
		}
		if (checkEnviroment(configVariableRegularExpressionEnviromentStart) != null) {
			setConfigVariableRegularExpressionStart(checkEnviroment(configVariableRegularExpressionEnviromentStart));
		}
		if (checkEnviroment(configVariableRegularExpressionEnviromentEnd) != null) {
			setConfigVariableRegularExpressionEnd(checkEnviroment(configVariableRegularExpressionEnviromentEnd));
		}
	}

	@Override
	public void registerService(final ConfigurationRefresh service) {
		services.add(service);
	}

	@Override
	public void registerStatusValue(final String label, final String value) {
		statusValues.put(label, value);
	}

	private void reload() {
		for (final ConfigurationRefresh s : services) {
			s.reloadConfiguration();
			logger.info("reload called on\n" + s.toString());
		}
		logger.info("all services reloaded");
	}

	@Override
	public void removeService(final ConfigurationRefresh service) {
		services.remove(service);
	}

	private String replaceEnviromentInString(final String data) {
		final Map<String, String> replaceList = new HashMap<>();
		if (patternVariableInConfiguration == null) {
			patternVariableInConfiguration = Pattern
					.compile(configVariableRegularExpressionStart + ".*" + configVariableRegularExpressionEnd);
		}
		final Matcher matcher = patternVariableInConfiguration.matcher(data);
		while (matcher.find()) {
			final String placeHolder = matcher.group();
			final String variable = placeHolder.replaceAll(configVariableRegularExpressionStart, "")
					.replaceAll(configVariableRegularExpressionEnd, "");
			if (checkEnviroment(variable) != null) {
				replaceList.put(placeHolder, checkEnviroment(variable));
			}
		}
		String resultString = data;
		for (final Entry<String, String> toReplace : replaceList.entrySet()) {
			resultString = resultString.replace(toReplace.getKey(), toReplace.getValue());
		}
		return resultString;
	}

	private String replaceHomeDirectory(final String path) {
		return path.replace("~", System.getProperty("user.home"));
	}

	@Override
	public void setCacheDirectoryPath(final String cacheDirectoryPath) {
		this.cacheDirectoryPath = cacheDirectoryPath;
	}

	@Override
	public void setCacheDirectoryPathEnviroment(final String cacheDirectoryPathEnviroment) {
		this.cacheDirectoryPathEnviroment = cacheDirectoryPathEnviroment;
		readVariablesFromEnviroment();
	}

	@Override
	public void setConfigDirectoryPath(final String configDirectoryPath) {
		this.configDirectoryPath = configDirectoryPath;
		checkConfigs();
	}

	@Override
	public void setConfigDirectoryPathEnviroment(final String configDirectoryPathEnviroment) {
		this.configDirectoryPathEnviroment = configDirectoryPathEnviroment;
		readVariablesFromEnviroment();
	}

	@Override
	public void setConfigExtension(final String configExtension) {
		this.configExtension = configExtension;
		checkConfigs();
	}

	@Override
	public void setConfigExtensionEnviroment(final String configExtensionEnviroment) {
		this.configExtensionEnviroment = configExtensionEnviroment;
		readVariablesFromEnviroment();
	}

	public void setConfigVariableRegularExpressionEnd(final String configVariableRegularExpression) {
		this.configVariableRegularExpressionEnd = configVariableRegularExpression;
		checkConfigs();
	}

	public void setConfigVariableRegularExpressionEnviromentEnd(
			final String configVariableRegularExpressionEnviroment) {
		this.configVariableRegularExpressionEnviromentEnd = configVariableRegularExpressionEnviroment;
		readVariablesFromEnviroment();
	}

	public void setConfigVariableRegularExpressionEnviromentStart(
			final String configVariableRegularExpressionEnviroment) {
		this.configVariableRegularExpressionEnviromentStart = configVariableRegularExpressionEnviroment;
		readVariablesFromEnviroment();
	}

	public void setConfigVariableRegularExpressionStart(final String configVariableRegularExpression) {
		this.configVariableRegularExpressionStart = configVariableRegularExpression;
		checkConfigs();
	}

	@Override
	public void setStatusFilePath(final String statusFilePath) {
		this.statusFilePath = statusFilePath;
	}

	@Override
	public void setStatusFilePathEnviroment(final String statusFilePathEnviroment) {
		this.statusFilePathEnviroment = statusFilePathEnviroment;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("AppManagerImplementation [");
		if (cacheDirectoryPath != null) {
			builder.append("cacheDirectoryPath=");
			builder.append(Paths.get(cacheDirectoryPath).toAbsolutePath().toString());
			builder.append(", ");
		}
		if (cacheDirectoryPathEnviroment != null) {
			builder.append("cacheDirectoryPathEnviroment=");
			builder.append(cacheDirectoryPathEnviroment);
			builder.append(", ");
		}
		if (configDirectoryPath != null) {
			builder.append("configDirectoryPath=");
			builder.append(Paths.get(configDirectoryPath).toAbsolutePath().toString());
			builder.append(", ");
		}
		if (configDirectoryPathEnviroment != null) {
			builder.append("configDirectoryPathEnviroment=");
			builder.append(configDirectoryPathEnviroment);
			builder.append(", ");
		}
		if (configExtension != null) {
			builder.append("configExtension=");
			builder.append(configExtension);
			builder.append(", ");
		}
		if (configExtensionEnviroment != null) {
			builder.append("configExtensionEnviroment=");
			builder.append(configExtensionEnviroment);
			builder.append(", ");
		}
		if (configVariableRegularExpressionStart != null) {
			builder.append("configVariableRegularExpressionStart=");
			builder.append(configVariableRegularExpressionStart);
			builder.append(", ");
		}
		if (configVariableRegularExpressionEnd != null) {
			builder.append("configVariableRegularExpressionEnd=");
			builder.append(configVariableRegularExpressionEnd);
			builder.append(", ");
		}
		if (configVariableRegularExpressionEnviromentStart != null) {
			builder.append("configVariableRegularExpressionEnviromentStart=");
			builder.append(configVariableRegularExpressionEnviromentStart);
			builder.append(", ");
		}
		if (configVariableRegularExpressionEnviromentEnd != null) {
			builder.append("configVariableRegularExpressionEnviromentEnd=");
			builder.append(configVariableRegularExpressionEnviromentEnd);
			builder.append(", ");
		}
		if (statusFilePath != null) {
			builder.append("statusFilePath=");
			builder.append(Paths.get(statusFilePath).toAbsolutePath().toString());
			builder.append(", ");
		}
		if (statusFilePathEnviroment != null) {
			builder.append("statusFilePathEnviroment=");
			builder.append(statusFilePathEnviroment);
		}
		builder.append("]");
		return builder.toString();
	}

}
