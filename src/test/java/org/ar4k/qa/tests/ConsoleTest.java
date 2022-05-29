package org.ar4k.qa.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.ar4k.agent.Ar4kAgent;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class ConsoleTest {

	private static final String DATA1 = "data1";
	private static final String DATA2 = "data2";
	private static final String EXTESION = ".test.json";
	private static final String FIRST_VALUE = "!{FIRST_VALUE}!";
	private static final Logger logger = Logger.getLogger(ConsoleTest.class.getName());
	private static final String REPLACED_DATA1 = "replaced_data1";
	private static final String REPLACED_DATA2 = "replaced_data2";
	private static final String REPLACED_SECRET = "replaced secret";
	private static final String REPLACED_VALUE = "replaced value";
	private static final String SECRET1_VALUE = "!{secret1}!";
	private static final String TEST1_VALUE = "test1";
	private static final String TEST2_VALUE = "test2";

	private static void setEnv(final Map<String, String> newenv) throws Exception {
		try {
			final Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			final Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);
			final Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
			env.putAll(newenv);
			final Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
					.getDeclaredField("theCaseInsensitiveEnvironment");
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			final Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
			cienv.putAll(newenv);
		} catch (final NoSuchFieldException e) {
			final Class[] classes = Collections.class.getDeclaredClasses();
			final Map<String, String> env = System.getenv();
			for (final Class cl : classes) {
				if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
					final Field field = cl.getDeclaredField("m");
					field.setAccessible(true);
					final Object obj = field.get(env);
					final Map<String, String> map = (Map<String, String>) obj;
					map.clear();
					map.putAll(newenv);
				}
			}
		}
	}

	private JSONObject checkData1 = null;

	private JSONObject checkData2 = null;

	private String checkDate = null;
	final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private String statusFilePath = null;

	@Test
	@Order(1)
	public void checkConfigUpdate() throws Exception {
		final String tempDirectory = "temp-test-" + UUID.randomUUID().toString();
		final Path tmpDirectory = Files.createDirectories(Paths.get(tempDirectory));
		final JSONObject config1 = new JSONObject();
		config1.put(DATA1, TEST1_VALUE);
		config1.put(REPLACED_DATA1, FIRST_VALUE);
		final JSONObject config2 = new JSONObject();
		config2.put(DATA2, TEST2_VALUE);
		config2.put(REPLACED_DATA2, SECRET1_VALUE);
		Files.write(Paths.get(tmpDirectory.toAbsolutePath().toString() + File.separator + "config1" + EXTESION),
				config1.toString(2).getBytes());
		Files.write(Paths.get(tmpDirectory.toAbsolutePath().toString() + File.separator + "config2" + EXTESION),
				config2.toString(2).getBytes());
		final Map<String, String> envs = new HashMap<>();
		envs.put("AR4K_CONFIG_EXTENSION", EXTESION);
		envs.put("FIRST_VALUE", REPLACED_VALUE);
		envs.put("secret1", REPLACED_SECRET);
		setEnv(envs);
		stopAfterSeconds(65);
		setVariableAfterSeconds(5, tempDirectory);
		printConfigAfterSeconds(20);
		Ar4kAgent.runApp();
		assertEquals(TEST1_VALUE, checkData1.getString(DATA1));
		assertEquals(TEST2_VALUE, checkData2.getString(DATA2));
		assertEquals(REPLACED_VALUE, checkData1.getString(REPLACED_DATA1));
		assertEquals(REPLACED_SECRET, checkData2.getString(REPLACED_DATA2));
		assertTrue(Files.exists(Paths.get(statusFilePath)));
		final JSONObject jsonObjectStatus = new JSONObject(
				new String(Files.readAllBytes(Paths.get(statusFilePath)), StandardCharsets.UTF_8));
		assertEquals("ok", jsonObjectStatus.getString("status"));
		assertEquals(checkDate, jsonObjectStatus.getString("last-update"));
		logger.info("ok");
	}

	@AfterEach
	public void cleanAgentInstance() throws Exception {
		logger.info("test completed");
	}

	private void printConfigAfterSeconds(final int delay) {

		final Runnable task = new Runnable() {

			@Override
			public void run() {
				System.out.println("config files " + Ar4kAgent.getAppManager().getConfigs().size());
				for (final Entry<String, JSONObject> conf : Ar4kAgent.getAppManager().getConfigJsons().entrySet()) {
					if (conf.getKey().endsWith("config2.test.json")) {
						checkData2 = conf.getValue();
					}
					if (conf.getKey().endsWith("config1.test.json")) {
						checkData1 = conf.getValue();
					}
					System.out.println("file " + conf.getKey() + ":\n" + conf.getValue().toString(2));
				}
				statusFilePath = Ar4kAgent.getAppManager().getStatusFilePath();
				checkDate = new Date().toString();
				Ar4kAgent.getAppManager().registerStatusValue("last-update", checkDate);
			}
		};

		scheduler.schedule(task, delay, TimeUnit.SECONDS);

	}

	private void setVariableAfterSeconds(final int delay, final String confDirectory) {

		final Runnable task = new Runnable() {
			@Override
			public void run() {
				Ar4kAgent.getAppManager().setConfigDirectoryPath(confDirectory);
			}
		};

		scheduler.schedule(task, delay, TimeUnit.SECONDS);
	}

	private void stopAfterSeconds(final int delay) {

		final Runnable task = new Runnable() {
			@Override
			public void run() {
				Ar4kAgent.stopAgent();
			}
		};

		scheduler.schedule(task, delay, TimeUnit.SECONDS);
	}

}
