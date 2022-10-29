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
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowseNodeExample implements ClientExample {

	public static void main(final String[] args) throws Exception {
		final BrowseNodeExample example = new BrowseNodeExample();

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

		// start browsing at root folder
		browseNode("", client, Identifiers.RootFolder);

		future.complete(client);
	}

	private void browseNode(final String indent, final OpcUaClient client, final NodeId browseRoot) {
		try {
			final List<? extends UaNode> nodes = client.getAddressSpace().browseNodes(browseRoot);

			for (final UaNode node : nodes) {
				logger.info("{} Node={}", indent, node.getBrowseName().getName());

				// recursively browse to children
				browseNode(indent + "  ", client, node.getNodeId());
			}
		} catch (final UaException e) {
			logger.error("Browsing nodeId={} failed: {}", browseRoot, e.getMessage(), e);
		}
	}

}
