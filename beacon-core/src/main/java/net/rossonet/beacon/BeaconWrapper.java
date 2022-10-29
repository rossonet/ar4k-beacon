package net.rossonet.beacon;

import java.io.Closeable;

public interface BeaconWrapper extends Closeable {

	void setController(BeaconController beaconController);

}
