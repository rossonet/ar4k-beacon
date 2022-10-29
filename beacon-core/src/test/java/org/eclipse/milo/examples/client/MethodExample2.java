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

import org.eclipse.milo.opcua.sdk.client.DataTypeTreeBuilder;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.methods.UaMethod;
import org.eclipse.milo.opcua.sdk.client.nodes.UaObjectNode;
import org.eclipse.milo.opcua.sdk.core.DataTypeTree;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodExample2 implements ClientExample {

	public static void main(final String[] args) throws Exception {
		final MethodExample2 example = new MethodExample2();

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

		final UaObjectNode objectNode = client.getAddressSpace().getObjectNode(NodeId.parse("ns=2;s=HelloWorld"));

		final UaMethod sqrtMethod = objectNode.getMethod("sqrt(x)");

		logArguments(client, sqrtMethod);

		final Variant[] inputs = { new Variant(16.0) };
		final Variant[] outputs = sqrtMethod.call(inputs);

		logger.info("Input values: " + Arrays.toString(inputs));
		logger.info("Output values: " + Arrays.toString(outputs));

		future.complete(client);
	}

	private void logArguments(final Argument[] arguments, final DataTypeTree dataTypeTree, final boolean input) {
		if (arguments.length == 0) {
			logger.info("{} arguments: none", input ? "Input" : "Output");
		} else {
			logger.info("{} arguments:", input ? "Input" : "Output");
			for (final Argument argument : arguments) {
				final NodeId dataTypeId = argument.getDataType();
				final DataTypeTree.DataType dataType = dataTypeTree.getDataType(dataTypeId);
				assert dataType != null;
				final String dataTypeName = dataType.getBrowseName().getName();

				logger.info("  {}: {} ({}) \"{}\"", argument.getName(), dataTypeName, dataTypeId.toParseableString(),
						argument.getDescription().getText());
			}
		}
	}

	private void logArguments(final OpcUaClient client, final UaMethod method) throws UaException {
		final DataTypeTree dataTypeTree = DataTypeTreeBuilder.build(client);
		final Argument[] inputArguments = method.getInputArguments();
		final Argument[] outputArguments = method.getOutputArguments();

		logArguments(inputArguments, dataTypeTree, true);
		logArguments(outputArguments, dataTypeTree, false);
	}

}
