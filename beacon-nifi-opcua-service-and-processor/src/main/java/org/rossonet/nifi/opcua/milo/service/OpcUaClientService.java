package org.rossonet.nifi.opcua.milo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

@Tags({ "Rossonet", "Ar4k", "OPCUA", "Milo" })
@CapabilityDescription("OPC UA Client using Apache Milo")
public class OpcUaClientService extends AbstractControllerService {

	public static final PropertyDescriptor APPLICATION_NAME = new PropertyDescriptor.Builder().name("Application Name")
			.description("The application name is used to label certificates identifying this application")
			.required(true).addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

	public static final PropertyDescriptor AUTH_POLICY = new PropertyDescriptor.Builder().name("Authentication Policy")
			.description("How should Nifi authenticate with the UA server").required(true).defaultValue("Anon")
			.allowableValues("Anon", "Username").addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();
	// Properties
	public static final PropertyDescriptor ENDPOINT = new PropertyDescriptor.Builder().name("Endpoint URL")
			.description("the opc.tcp address of the opc ua server").required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();
	public static final PropertyDescriptor PASSWORD = new PropertyDescriptor.Builder().name("Password")
			.description("The Password to be used for the connection").required(false).sensitive(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();
	public static final PropertyDescriptor SECURITY_POLICY = new PropertyDescriptor.Builder().name("Security Policy")
			.description("How should Nifi create the connection with the UA server").required(true)
			.allowableValues("None", "Basic128Rsa15", "Basic256", "Basic256Rsa256")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();
	public static final PropertyDescriptor SERVER_CERT = new PropertyDescriptor.Builder()
			.name("Certificate for Server application")
			.description(
					"Certificate in .der format for server Nifi will connect, if left blank Nifi will attempt to retreive the certificate from the server")
			.addValidator(StandardValidators.FILE_EXISTS_VALIDATOR).build();
	public static final PropertyDescriptor USERNAME = new PropertyDescriptor.Builder().name("User Name")
			.description("The user name to be used for the connection.").required(false)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR).build();
	private static final List<PropertyDescriptor> properties;

	static {
		final List<PropertyDescriptor> props = new ArrayList<>();
		props.add(ENDPOINT);
		props.add(SECURITY_POLICY);
		props.add(SERVER_CERT);
		props.add(AUTH_POLICY);
		props.add(USERNAME);
		props.add(PASSWORD);
		props.add(APPLICATION_NAME);
		properties = Collections.unmodifiableList(props);
	}

	@Override
	protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return properties;
	}

	@Override
	protected void init(final ControllerServiceInitializationContext config) throws InitializationException {
		getLogger().info("starting Rossonet Template Service");
	}

}
