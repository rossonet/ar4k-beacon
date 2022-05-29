package org.ar4k.beacon.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogUtils {

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

	private LogUtils() {
		throw new UnsupportedOperationException("Just for static usage");
	}

}
