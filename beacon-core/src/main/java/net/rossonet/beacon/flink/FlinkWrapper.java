package net.rossonet.beacon.flink;

import net.rossonet.beacon.BeaconWrapper;

public interface FlinkWrapper extends BeaconWrapper {

	void startFlink();

	void stopFlink();

}