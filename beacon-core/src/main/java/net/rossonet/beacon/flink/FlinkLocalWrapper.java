package net.rossonet.beacon.flink;

import java.io.IOException;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import net.rossonet.beacon.BeaconController;

public class FlinkLocalWrapper implements FlinkWrapper {

	private BeaconController beaconController;
	private final StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment
			.getExecutionEnvironment();

	@Override
	public void close() throws IOException {
		stopFlink();

	}

	public StreamExecutionEnvironment getExecutionEnvironment() {
		return executionEnvironment;
	}

	@Override
	public void setController(final BeaconController beaconController) {
		this.beaconController = beaconController;

	}

	@Override
	public void startFlink() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopFlink() {
		executionEnvironment.clearJobListeners();
	}

}
