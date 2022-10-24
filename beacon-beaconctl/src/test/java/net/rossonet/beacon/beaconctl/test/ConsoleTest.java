package net.rossonet.beacon.beaconctl.test;

import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import net.rossonet.beacon.beaconctl.Beaconctl;

@TestMethodOrder(OrderAnnotation.class)
public class ConsoleTest {
	private static final Logger logger = Logger.getLogger(ConsoleTest.class.getName());

	@Test
	@Order(1)
	public void checkApp() throws Exception {
		Beaconctl.runApp(new String[0]);
		logger.info("ok");
	}

	@AfterEach
	public void cleanAgentInstance() throws Exception {
		logger.info("test completed");
	}

}
