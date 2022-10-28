package org.eclipse.milo.examples;

import static org.junit.Assert.assertTrue;

import org.eclipse.milo.examples.client.BrowseAsyncExample;
import org.eclipse.milo.examples.client.BrowseExample;
import org.eclipse.milo.examples.client.BrowseNodeExample;
import org.eclipse.milo.examples.client.ClientExample;
import org.eclipse.milo.examples.client.ClientExampleRunner;
import org.eclipse.milo.examples.client.EventSubscriptionExample;
import org.eclipse.milo.examples.client.MethodExample;
import org.eclipse.milo.examples.client.ReadExample;
import org.eclipse.milo.examples.client.SubscriptionExample;
import org.eclipse.milo.examples.client.WriteExample;
import org.eclipse.milo.examples.server.ExampleServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunnerMiloTests {

	private static final Logger logger = LoggerFactory.getLogger(RunnerMiloTests.class);

	public static boolean running = true;
	private static ExampleServer server = null;

	public static void main(String[] args) throws Exception {
		logger.info("starting OPCUA server");
		runMiloServerExample();
		while (running) {
			Thread.sleep(2000);
		}
		stopMiloServerExample();
		logger.info("OPCUA server shutdown completed!");
	}

	private static void runMiloServerExample() throws Exception {
		server = new ExampleServer();
		server.startup();
	}

	private static void stopMiloServerExample() throws Exception {
		if (server != null) {
			server.shutdown();
		}
	}

	@Test
	public void runBrowseAsyncExample() throws Exception {
		// BrowseAsyncExample.main(new String[0]);
		final BrowseAsyncExample example = new BrowseAsyncExample();
		runMiloTest(example);
	}

	@Test
	public void runBrowseExample() throws Exception {
		// BrowseExample.main(new String[0]);
		final BrowseExample example = new BrowseExample();
		runMiloTest(example);
	}

	@Test
	public void runBrowseNodeExample() throws Exception {
		// BrowseNodeExample.main(new String[0]);
		final BrowseNodeExample example = new BrowseNodeExample();
		runMiloTest(example);
	}

	@Test
	public void runEventSubscriptionExample() throws Exception {
		// EventSubscriptionExample.main(new String[0]);
		final EventSubscriptionExample example = new EventSubscriptionExample();
		runMiloTest(example);
	}

	@Test
	public void runMethodExample() throws Exception {
		// MethodExample.main(new String[0]);
		final MethodExample example = new MethodExample();
		runMiloTest(example);
	}

	private void runMiloTest(final ClientExample example) throws Exception {
		final ClientExampleRunner client = new ClientExampleRunner(example);
		client.run();
		assertTrue(client.getTestResult());
	}

	@Test
	public void runReadExample() throws Exception {
		// ReadExample.main(new String[0]);
		final ReadExample example = new ReadExample();
		runMiloTest(example);
	}

	@Test
	public void runSubscriptionExample() throws Exception {
		// SubscriptionExample.main(new String[0]);
		final SubscriptionExample example = new SubscriptionExample();
		runMiloTest(example);
	}

	@Test
	public void runWriteExample() throws Exception {
		// WriteExample.main(new String[0]);
		final WriteExample example = new WriteExample();
		runMiloTest(example);
	}

}
