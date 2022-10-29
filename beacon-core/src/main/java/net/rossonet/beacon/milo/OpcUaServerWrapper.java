package net.rossonet.beacon.milo;

import net.rossonet.beacon.BeaconController;

public class OpcUaServerWrapper {

	private final BeaconController beaconController;
	private final OpcUaServerParameters opcUaServerParameters;
	private final OpcUaServerStorage opcUaServerStorage;

	public OpcUaServerWrapper(final BeaconController beaconController,
			final OpcUaServerParameters opcUaServerParameters, final OpcUaServerStorage opcUaServerStorage) {
		this.beaconController = beaconController;
		this.opcUaServerParameters = opcUaServerParameters;
		this.opcUaServerStorage = opcUaServerStorage;
	}

	public void startOpcServer() {
		// TODO Auto-generated method stub

	}

	public void stopOpcServer() {
		// TODO Auto-generated method stub

	}

}
