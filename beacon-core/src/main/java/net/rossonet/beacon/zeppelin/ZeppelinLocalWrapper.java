package net.rossonet.beacon.zeppelin;

import java.io.IOException;

import net.rossonet.beacon.BeaconController;

public class ZeppelinLocalWrapper implements ZeppelinWrapper {

	private BeaconController beaconController;

	@Override
	public void close() throws IOException {
		stopZeppelin();

	}

	@Override
	public void setController(final BeaconController beaconController) {
		this.beaconController = beaconController;

	}

	@Override
	public void startZeppelin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopZeppelin() {
		// TODO Auto-generated method stub

	}
}
