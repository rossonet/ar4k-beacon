package org.rossonet.beacon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class Studio {

	@Test
	public void testDataImport() {
		final String dataString = "20221029T161049+0000";
		final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'+0000'");
		final LocalDateTime dateTime = LocalDateTime.parse(dataString, format);
		System.out.println(dateTime.toString());
	}
}
