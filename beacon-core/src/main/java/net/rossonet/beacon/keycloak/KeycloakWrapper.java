package net.rossonet.beacon.keycloak;

import net.rossonet.beacon.BeaconWrapper;

public interface KeycloakWrapper extends BeaconWrapper {

	void startKeycloak();

	void stopKeycloak();

}