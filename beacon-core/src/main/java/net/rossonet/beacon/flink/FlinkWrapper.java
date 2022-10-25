package net.rossonet.beacon.flink;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkWrapper {

	private final StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment
			.getExecutionEnvironment();

	public StreamExecutionEnvironment getExecutionEnvironment() {
		return executionEnvironment;
	}

	public void stop() {
		executionEnvironment.clearJobListeners();
	}

}
