package org.ar4k.agent;

import java.util.Collection;
import java.util.Map;

import org.ar4k.beacon.service.ConfigurationRefresh;
import org.json.JSONObject;

public interface AppManager extends AutoCloseable {

	public static final String KO_STATUS = "ko";
	public static final String OK_STATUS = "ok";
	public static final String STATUS_FIELD = "status";

	String getCacheDirectoryPath();

	String getCacheDirectoryPathEnviroment();

	String getConfigDirectoryPath();

	String getConfigDirectoryPathEnviroment();

	String getConfigExtension();

	String getConfigExtensionEnviroment();

	Map<String, JSONObject> getConfigJsons();

	Collection<JSONObject> getConfigs();

	JSONObject getJsonStatus();

	String getStatusFilePath();

	String getStatusFilePathEnviroment();

	void registerService(ConfigurationRefresh service);

	void registerStatusValue(String label, String value);

	void removeService(ConfigurationRefresh service);

	void setCacheDirectoryPath(String cacheDirectoryPath);

	void setCacheDirectoryPathEnviroment(String cacheDirectoryPathEnviroment);

	void setConfigDirectoryPath(String configDirectoryPath);

	void setConfigDirectoryPathEnviroment(String configDirectoryPathEnviroment);

	void setConfigExtension(String configExtension);

	void setConfigExtensionEnviroment(String configExtensionEnviroment);

	void setStatusFilePath(String statusFilePath);

	void setStatusFilePathEnviroment(String statusFilePathEnviroment);

}
