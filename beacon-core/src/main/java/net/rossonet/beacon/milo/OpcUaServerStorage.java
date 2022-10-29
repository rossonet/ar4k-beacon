package net.rossonet.beacon.milo;

import java.io.Serializable;
import java.util.Map;

public interface OpcUaServerStorage {

	public Map<String, Serializable> getMap();

}
