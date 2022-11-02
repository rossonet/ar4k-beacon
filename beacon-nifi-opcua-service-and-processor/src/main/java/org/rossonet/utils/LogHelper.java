package org.rossonet.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;

public final class LogHelper {

	public static void changeLogLevel(final String logLevel) throws ConfigurationException {
		final Logger rootLogger = Logger.getLogger("");
		Level targetLevel = Level.INFO;
		switch (logLevel) {
		case "all":
			targetLevel = Level.ALL;
			break;
		case "config":
			targetLevel = Level.CONFIG;
			break;
		case "fine":
			targetLevel = Level.FINE;
			break;
		case "finer":
			targetLevel = Level.FINER;
			break;
		case "finest":
			targetLevel = Level.FINEST;
			break;
		case "info":
			targetLevel = Level.INFO;
			break;
		case "off":
			targetLevel = Level.OFF;
			break;
		case "severe":
			targetLevel = Level.SEVERE;
			break;
		case "warning":
			targetLevel = Level.WARNING;
			break;
		default:
			throw new ConfigurationException("log level " + logLevel
					+ " not exists. You can use: all, config, fine, finer, finest, info, off, severe or warning");
		}
		rootLogger.setLevel(targetLevel);
		for (final Handler handler : rootLogger.getHandlers()) {
			handler.setLevel(targetLevel);
		}
	}

	public static String stackTraceToString(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		String response = null;
		if (throwable.getCause() != null && throwable.getCause().getMessage() != null) {
			response = " [M] " + throwable.getCause().getMessage() + " -> " + sw.toString();
		} else {
			response = " [M] " + sw.toString();
		}
		return response;
	}

	public static String stackTraceToString(final Throwable throwable, final int numLines) {
		try {
			final List<String> lines = Arrays.asList(stackTraceToString(throwable).split("\n"));
			final ArrayList<String> al = new ArrayList<>(lines.subList(0, Math.min(lines.size(), numLines)));
			final StringBuilder returnString = new StringBuilder();
			for (final String line : al) {
				returnString.append(line + "\n");
			}
			return returnString.toString();
		} catch (final Exception n) {
			return stackTraceToString(throwable);
		}

	}

	private LogHelper() {
		throw new UnsupportedOperationException("Just for static usage");
	}

}
