package net.rossonet.beacon.web;

import net.rossonet.beacon.BeaconWrapper;

public interface BeaconWebAppWrapper extends BeaconWrapper {

	void startWebApp();

	void stopWebApp();

}