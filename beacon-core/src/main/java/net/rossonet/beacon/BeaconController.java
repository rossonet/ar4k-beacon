package net.rossonet.beacon;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import org.rossonet.beacon.utils.LogHelper;

import net.rossonet.beacon.flink.FlinkWrapper;
import net.rossonet.beacon.nifi.NiFiWrapper;

public class BeaconController implements AutoCloseable {
	public static final long WHILE_DELAY = 60 * 1000L;
	private static final Logger logger = Logger.getLogger(BeaconController.class.getName());
	private final ExecutorService executorService;
	private FlinkWrapper flinkWrapper = null;

	private NiFiWrapper nifi = null;

	private boolean running = false;

	public BeaconController(final ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public void close() throws Exception {
		if (flinkWrapper != null) {
			flinkWrapper.stop();
		}
		if (nifi != null) {
			nifi.stopNifi();
		}
	}

	public void start() {
		startFlink();
		startNifi();
		running = true;
	}

	public void stop() {
		running = false;
		try {
			close();
		} catch (final Exception e) {
			logger.severe("exception stopping Beacon Controller\n" + LogHelper.stackTraceToString(e));
		}
	}

	public void waitTermination() {
		while (running) {
			try {
				Thread.sleep(WHILE_DELAY);
			} catch (final InterruptedException e) {
				logger.severe("interrupted wait thread\n" + LogHelper.stackTraceToString(e));
			}

		}

	}

	private void startFlink() {
		flinkWrapper = new FlinkWrapper();
	}

	private void startNifi() {

		executorService.submit(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.currentThread().setName("nifi-wrapper");
					logger.info("starting Apache NiFi");
					nifi = new NiFiWrapper();
				} catch (final IOException e) {
					logger.severe("exception in Apache NiFi\n" + LogHelper.stackTraceToString(e));
				}

			}
		});

	}

}
