package net.rossonet.beacon.nifi;

import java.io.IOException;

import org.json.JSONObject;

import com.github.hermannpencole.nifi.swagger.client.AccessApi;

import net.rossonet.beacon.BeaconWrapper;

public interface NiFiWrapper extends BeaconWrapper {

	JSONObject getFlowXmlGzJSon();

	AccessApi getWebClientApi();

	void startNifi() throws IOException;

	void stopNifi();

	void writeFlowXmlGzJson(JSONObject decompressJson);

}