package net.rossonet.beacon.keycloak;

import java.io.IOException;

import net.rossonet.beacon.BeaconController;

public class KeycloakRemoteWrapper implements KeycloakWrapper {

	private BeaconController beaconController;

	@Override
	public void close() throws IOException {
		stopKeycloak();

	}

	@Override
	public void setController(final BeaconController beaconController) {
		this.beaconController = beaconController;

	}

	@Override
	public void startKeycloak() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopKeycloak() {
		// TODO Auto-generated method stub

	}
}
