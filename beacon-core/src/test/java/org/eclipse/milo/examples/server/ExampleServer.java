/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.examples.server;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_X509;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.X509IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaRuntimeException;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.NonceUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedHttpsCertificateBuilder;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.server.security.DefaultServerCertificateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleServer implements AutoCloseable {

	private static final int HTTPS_BIND_PORT = 8443;
	public static final String MILO_DISCOVERY_PATH = "/milo/discovery";
	public static final int TCP_BIND_PORT = 12686;

	static {
		// Required for SecurityPolicy.Aes256_Sha256_RsaPss
		Security.addProvider(new BouncyCastleProvider());

		try {
			NonceUtil.blockUntilSecureRandomSeeded(10, TimeUnit.SECONDS);
		} catch (ExecutionException | InterruptedException | TimeoutException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static EndpointConfiguration buildHttpsEndpoint(final EndpointConfiguration.Builder base) {
		return base.copy().setTransportProfile(TransportProfile.HTTPS_UABINARY).setBindPort(HTTPS_BIND_PORT).build();
	}

	private static EndpointConfiguration buildTcpEndpoint(final EndpointConfiguration.Builder base) {
		return base.copy().setTransportProfile(TransportProfile.TCP_UASC_UABINARY).setBindPort(TCP_BIND_PORT).build();
	}

	public static void main(final String[] args) throws Exception {
		final ExampleServer server = new ExampleServer();

		server.startup().get();

		final CompletableFuture<Void> future = new CompletableFuture<>();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> future.complete(null)));

		future.get();
	}

	private final ExampleNamespace exampleNamespace;

	private final OpcUaServer server;

	public ExampleServer() throws Exception {
		final Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "server", "security");
		Files.createDirectories(securityTempDir);
		if (!Files.exists(securityTempDir)) {
			throw new Exception("unable to create security temp dir: " + securityTempDir);
		}

		final File pkiDir = securityTempDir.resolve("pki").toFile();

		getLogger().info("security dir: {}", securityTempDir.toAbsolutePath());
		getLogger().info("security pki dir: {}", pkiDir.getAbsolutePath());

		final KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

		final DefaultCertificateManager certificateManager = new DefaultCertificateManager(loader.getServerKeyPair(),
				loader.getServerCertificateChain());

		final DefaultTrustListManager trustListManager = new DefaultTrustListManager(pkiDir);

		final DefaultServerCertificateValidator certificateValidator = new DefaultServerCertificateValidator(
				trustListManager);

		final KeyPair httpsKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);

		final SelfSignedHttpsCertificateBuilder httpsCertificateBuilder = new SelfSignedHttpsCertificateBuilder(
				httpsKeyPair);
		httpsCertificateBuilder.setCommonName(HostnameUtil.getHostname());
		HostnameUtil.getHostnames("0.0.0.0").forEach(httpsCertificateBuilder::addDnsName);
		final X509Certificate httpsCertificate = httpsCertificateBuilder.build();

		final UsernameIdentityValidator identityValidator = new UsernameIdentityValidator(true, authChallenge -> {
			final String username = authChallenge.getUsername();
			final String password = authChallenge.getPassword();

			final boolean userOk = "user".equals(username) && "password1".equals(password);
			final boolean adminOk = "admin".equals(username) && "password2".equals(password);

			return userOk || adminOk;
		});

		final X509IdentityValidator x509IdentityValidator = new X509IdentityValidator(c -> true);

		// If you need to use multiple certificates you'll have to be smarter than this.
		final X509Certificate certificate = certificateManager.getCertificates().stream().findFirst()
				.orElseThrow(() -> new UaRuntimeException(StatusCodes.Bad_ConfigurationError, "no certificate found"));

		// The configured application URI must match the one in the certificate(s)
		final String applicationUri = CertificateUtil.getSanUri(certificate)
				.orElseThrow(() -> new UaRuntimeException(StatusCodes.Bad_ConfigurationError,
						"certificate is missing the application URI"));

		final Set<EndpointConfiguration> endpointConfigurations = createEndpointConfigurations(certificate);

		final OpcUaServerConfig serverConfig = OpcUaServerConfig.builder().setApplicationUri(applicationUri)
				.setApplicationName(LocalizedText.english("Eclipse Milo OPC UA Example Server"))
				.setEndpoints(endpointConfigurations)
				.setBuildInfo(new BuildInfo("urn:eclipse:milo:example-server", "eclipse", "eclipse milo example server",
						OpcUaServer.SDK_VERSION, "", DateTime.now()))
				.setCertificateManager(certificateManager).setTrustListManager(trustListManager)
				.setCertificateValidator(certificateValidator).setHttpsKeyPair(httpsKeyPair)
				.setHttpsCertificateChain(new X509Certificate[] { httpsCertificate })
				.setIdentityValidator(new CompositeValidator(identityValidator, x509IdentityValidator))
				.setProductUri("urn:eclipse:milo:example-server").build();

		server = new OpcUaServer(serverConfig);

		exampleNamespace = new ExampleNamespace(server);
		exampleNamespace.startup();
		getLogger().info("OPCUA server started");
	}

	@Override
	public void close() throws Exception {
		shutdown();

	}

	private Set<EndpointConfiguration> createEndpointConfigurations(final X509Certificate certificate) {
		final Set<EndpointConfiguration> endpointConfigurations = new LinkedHashSet<>();

		final List<String> bindAddresses = newArrayList();
		bindAddresses.add("0.0.0.0");

		final Set<String> hostnames = new LinkedHashSet<>();
		hostnames.add(HostnameUtil.getHostname());
		hostnames.addAll(HostnameUtil.getHostnames("0.0.0.0"));

		for (final String bindAddress : bindAddresses) {
			for (final String hostname : hostnames) {
				final EndpointConfiguration.Builder builder = EndpointConfiguration.newBuilder()
						.setBindAddress(bindAddress).setHostname(hostname).setPath("/milo").setCertificate(certificate)
						.addTokenPolicies(USER_TOKEN_POLICY_ANONYMOUS, USER_TOKEN_POLICY_USERNAME,
								USER_TOKEN_POLICY_X509);

				final EndpointConfiguration.Builder noSecurityBuilder = builder.copy()
						.setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None);

				endpointConfigurations.add(buildTcpEndpoint(noSecurityBuilder));
				endpointConfigurations.add(buildHttpsEndpoint(noSecurityBuilder));

				// TCP Basic256Sha256 / SignAndEncrypt
				endpointConfigurations
						.add(buildTcpEndpoint(builder.copy().setSecurityPolicy(SecurityPolicy.Basic256Sha256)
								.setSecurityMode(MessageSecurityMode.SignAndEncrypt)));

				// HTTPS Basic256Sha256 / Sign (SignAndEncrypt not allowed for HTTPS)
				endpointConfigurations.add(buildHttpsEndpoint(builder.copy()
						.setSecurityPolicy(SecurityPolicy.Basic256Sha256).setSecurityMode(MessageSecurityMode.Sign)));

				/*
				 * It's good practice to provide a discovery-specific endpoint with no security.
				 * It's required practice if all regular endpoints have security configured.
				 *
				 * Usage of the "/discovery" suffix is defined by OPC UA Part 6:
				 *
				 * Each OPC UA Server Application implements the Discovery Service Set. If the
				 * OPC UA Server requires a different address for this Endpoint it shall create
				 * the address by appending the path "/discovery" to its base address.
				 */

				final EndpointConfiguration.Builder discoveryBuilder = builder.copy().setPath(MILO_DISCOVERY_PATH)
						.setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None);

				endpointConfigurations.add(buildTcpEndpoint(discoveryBuilder));
				endpointConfigurations.add(buildHttpsEndpoint(discoveryBuilder));
			}
		}

		return endpointConfigurations;
	}

	private Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	public OpcUaServer getServer() {
		return server;
	}

	public CompletableFuture<OpcUaServer> shutdown() {
		exampleNamespace.shutdown();

		return server.shutdown();
	}

	public CompletableFuture<OpcUaServer> startup() {
		return server.startup();
	}

}
