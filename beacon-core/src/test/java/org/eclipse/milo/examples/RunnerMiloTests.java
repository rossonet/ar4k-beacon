package org.eclipse.milo.examples;

import org.eclipse.milo.examples.client.BrowseAsyncExample;
import org.eclipse.milo.examples.client.BrowseExample;
import org.eclipse.milo.examples.client.BrowseNodeExample;
import org.eclipse.milo.examples.client.EventSubscriptionExample;
import org.eclipse.milo.examples.client.MethodExample;
import org.eclipse.milo.examples.client.ReadExample;
import org.eclipse.milo.examples.client.SubscriptionExample;
import org.eclipse.milo.examples.client.WriteExample;
import org.eclipse.milo.examples.server.ExampleServer;
import org.junit.jupiter.api.Test;

public class RunnerMiloTests {

	@Test
	public void runBrowseAsyncExample() throws Exception {
		BrowseAsyncExample.main(new String[0]);
	}

	@Test
	public void runBrowseExample() throws Exception {
		BrowseExample.main(new String[0]);
	}

	@Test
	public void runBrowseNodeExample() throws Exception {
		BrowseNodeExample.main(new String[0]);
	}

	@Test
	public void runEventSubscriptionExample() throws Exception {
		EventSubscriptionExample.main(new String[0]);
	}

	@Test
	public void runMethodExample() throws Exception {
		MethodExample.main(new String[0]);
	}

	// @Test
	public void runMiloServerExample() throws Exception {
		ExampleServer.main(new String[0]);
	}

	@Test
	public void runReadExample() throws Exception {
		ReadExample.main(new String[0]);
	}

	@Test
	public void runSubscriptionExample() throws Exception {
		SubscriptionExample.main(new String[0]);
	}

	@Test
	public void runWriteExample() throws Exception {
		WriteExample.main(new String[0]);
	}

}
