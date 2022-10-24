package org.rossonet.beacon.utils;

import java.io.IOException;
import java.util.Base64;

public final class DnsHelper {

	public static String toDnsRecord(final String name, final String payload) throws IOException {
		final Iterable<String> chunks = TextHelper.splitFixSize(Base64.getEncoder().encodeToString(payload.getBytes()),
				254);
		final StringBuilder result = new StringBuilder();
		int counter = 0;
		for (final String s : chunks) {
			result.append(name + "-" + String.valueOf(counter) + "\tIN\tTXT\t" + '"' + s + '"' + "\n");
			counter++;
		}
		result.append(name + "-max" + "\tIN\tTXT\t" + '"' + String.valueOf(counter) + '"' + "\n");
		return result.toString();
	}

	private DnsHelper() {
		throw new UnsupportedOperationException("Just for static usage");

	}

}
