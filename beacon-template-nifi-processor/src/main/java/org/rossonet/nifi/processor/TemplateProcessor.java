package org.rossonet.nifi.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.rossonet.utils.LogHelper;

@EventDriven
@SupportsBatching
@SideEffectFree
@Tags({ "Rossonet", "Ar4k", "Template" })
@CapabilityDescription("Template processor for NiFi")
public class TemplateProcessor extends AbstractProcessor {

	public static final Relationship FAILD = new Relationship.Builder().name("failed")
			.description("Failed relationship for test").build();

	public static final Relationship SUCCESS = new Relationship.Builder().name("success")
			.description("Success relationship for test").build();

	public static final PropertyDescriptor TEST_PROPERTY = new PropertyDescriptor.Builder().name("Test name")
			.displayName("Test display name").description("Test description").required(true)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR).addValidator(StandardValidators.NON_BLANK_VALIDATOR)
			.build();

	public static final Relationship TEST1 = new Relationship.Builder().name("test1").description("test1 relationship")
			.build();

	private List<PropertyDescriptor> descriptors;

	private ComponentLog log;

	private Set<Relationship> relationships;

	@Override
	public Set<Relationship> getRelationships() {
		return relationships;
	}

	@Override
	public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return descriptors;
	}

	@OnScheduled
	public void onScheduled(final ProcessContext context) {
	}

	@OnStopped
	public void onStopped() {
		descriptors = null;
		relationships = null;
	}

	@Override
	public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
		final FlowFile originalFlowFile = session.get();
		if (originalFlowFile == null) {
			return;
		}
		try {
			final Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("welcome", "message");
			final FlowFile flowFileTest = session.putAllAttributes(originalFlowFile, attributes);
			session.transfer(flowFileTest, TEST1);
			session.commit();
		} catch (final Exception e) {
			final Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("failed", "true");
			attributes.put("message", e.getMessage());
			attributes.put("exception", LogHelper.stackTraceToString(e));
			final FlowFile flowFileFailed = session.putAllAttributes(session.create(), attributes);
			session.transfer(flowFileFailed, FAILD);
			session.commit();

		}

	}

	@Override
	protected void init(final ProcessorInitializationContext context) {
		log = getLogger();
		log.info("Init Rossonet's Template Processor");

		final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
		descriptors.add(TEST_PROPERTY);
		this.descriptors = Collections.unmodifiableList(descriptors);

		final Set<Relationship> relationships = new HashSet<Relationship>();
		relationships.add(SUCCESS);
		relationships.add(FAILD);
		relationships.add(TEST1);
		this.relationships = Collections.unmodifiableSet(relationships);
	}

}
