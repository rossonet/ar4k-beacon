package net.rossonet.beacon.milo;

import net.rossonet.beacon.BeaconController;

public class OpcUaServerWrapper {

	private final BeaconController beaconController;
	private final OpcUaServerParameters opcUaServerParameters;

	public OpcUaServerWrapper(final BeaconController beaconController,
			final OpcUaServerParameters opcUaServerParameters) {
		this.beaconController = beaconController;
		this.opcUaServerParameters = opcUaServerParameters;
	}

	public void startOpcServer() {
		// TODO Auto-generated method stub

	}

	public void stopOpcServer() {
		// TODO Auto-generated method stub

	}

}
