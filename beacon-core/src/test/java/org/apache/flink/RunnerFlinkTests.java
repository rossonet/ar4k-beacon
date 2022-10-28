package org.apache.flink;

import org.apache.flink.streaming.examples.join.WindowJoin;

public class RunnerFlinkTests {

	// verificare durata test
	// @Test
	public void runWindowJoinExample() throws Exception {
		WindowJoin.main(new String[0]);
	}

}
