/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.examples.client;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.ServerStatusTypeNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.enumerated.ServerState;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.ServerStatusDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadNodeExample implements ClientExample {

	public static void main(final String[] args) throws Exception {
		final ReadNodeExample example = new ReadNodeExample();

		new ClientExampleRunner(example).run();
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean getTestResult() {
		// TODO verificare risultato test
		return true;
	}

	@Override
	public void run(final OpcUaClient client, final CompletableFuture<OpcUaClient> future) throws Exception {
		// synchronous connect
		client.connect().get();

		// Get a typed reference to the Server object: ServerNode
		final ServerTypeNode serverNode = (ServerTypeNode) client.getAddressSpace().getObjectNode(Identifiers.Server,
				Identifiers.ServerType);

		// Read properties of the Server object...
		final String[] serverArray = serverNode.getServerArray();
		final String[] namespaceArray = serverNode.getNamespaceArray();

		logger.info("ServerArray={}", Arrays.toString(serverArray));
		logger.info("NamespaceArray={}", Arrays.toString(namespaceArray));

		// Read the value of attribute the ServerStatus variable component
		final ServerStatusDataType serverStatus = serverNode.getServerStatus();

		logger.info("ServerStatus={}", serverStatus);

		// Get a typed reference to the ServerStatus variable
		// component and read value attributes individually
		final ServerStatusTypeNode serverStatusNode = serverNode.getServerStatusNode();
		final BuildInfo buildInfo = serverStatusNode.getBuildInfo();
		final DateTime startTime = serverStatusNode.getStartTime();
		final DateTime currentTime = serverStatusNode.getCurrentTime();
		final ServerState state = serverStatusNode.getState();

		logger.info("ServerStatus.BuildInfo={}", buildInfo);
		logger.info("ServerStatus.StartTime={}", startTime);
		logger.info("ServerStatus.CurrentTime={}", currentTime);
		logger.info("ServerStatus.State={}", state);

		future.complete(client);
	}

}
