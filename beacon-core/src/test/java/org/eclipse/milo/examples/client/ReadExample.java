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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.ServerState;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class ReadExample implements ClientExample {

	public static void main(final String[] args) throws Exception {
		final ReadExample example = new ReadExample();

		new ClientExampleRunner(example, false).run();
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

		// synchronous read request via VariableNode
		final UaVariableNode node = client.getAddressSpace().getVariableNode(Identifiers.Server_ServerStatus_StartTime);
		final DataValue value = node.readValue();

		logger.info("StartTime={}", value.getValue().getValue());

		// asynchronous read request
		readServerStateAndTime(client).thenAccept(values -> {
			final DataValue v0 = values.get(0);
			final DataValue v1 = values.get(1);

			logger.info("State={}", ServerState.from((Integer) v0.getValue().getValue()));
			logger.info("CurrentTime={}", v1.getValue().getValue());

			future.complete(client);
		});
	}

	private CompletableFuture<List<DataValue>> readServerStateAndTime(final OpcUaClient client) {
		final List<NodeId> nodeIds = ImmutableList.of(Identifiers.Server_ServerStatus_State,
				Identifiers.Server_ServerStatus_CurrentTime);

		return client.readValues(0.0, TimestampsToReturn.Both, nodeIds);
	}

}
