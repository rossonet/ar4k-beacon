package org.ar4k.beacon.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.ar4k.agent.Ar4kAgent;

public interface ManagedService {

	public static String getResourceFileAsString(final String fileName) throws IOException {
		final InputStream ioStream = Ar4kAgent.class.getClassLoader().getResourceAsStream(fileName);

		if (ioStream == null) {
			throw new IllegalArgumentException(fileName + " is not found");
		}
		final StringBuilder sb = new StringBuilder();
		try (InputStreamReader isr = new InputStreamReader(ioStream); BufferedReader br = new BufferedReader(isr);) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			ioStream.close();
		}
		return sb.toString();
	}

	public void reload() throws IOException;

	public void start() throws IOException;

	public void stop() throws IOException;

}
