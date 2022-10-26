package org.apache.flink;

import org.apache.flink.streaming.examples.join.WindowJoin;
import org.junit.jupiter.api.Test;

public class RunnerFlinkTests {

	@Test
	public void runWindowJoinExample() throws Exception {
		WindowJoin.main(new String[0]);
	}

}
