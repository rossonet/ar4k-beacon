package net.rossonet.beacon.web;

import java.io.IOException;

import net.rossonet.beacon.BeaconController;

public class BeaconWebAppLocalWrapper implements BeaconWebAppWrapper {

	private BeaconController beaconController;

	@Override
	public void close() throws IOException {
		stopWebApp();

	}

	@Override
	public void setController(final BeaconController beaconController) {
		this.beaconController = beaconController;

	}

	@Override
	public void startWebApp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopWebApp() {
		// TODO Auto-generated method stub

	}
}
