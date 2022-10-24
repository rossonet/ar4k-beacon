package net.rossonet.savumerkki.savuctl;

import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import net.rossonet.beacon.beaconctl.Beaconctl;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class ConsoleTest {
	private static final Logger logger = Logger.getLogger(ConsoleTest.class.getName());

	@Test
	@Order(1)
	public void checkApp() throws Exception {
		Beaconctl.runApp();
		logger.info("ok");
	}

	@AfterEach
	public void cleanAgentInstance() throws Exception {
		logger.info("test completed");
	}

}
