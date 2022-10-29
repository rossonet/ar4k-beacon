package org.rossonet.beacon.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

public final class SynchronizeHelper {

	public static long filesCompareByByte(final Path path1, final Path path2) throws IOException {
		try (BufferedInputStream fis1 = new BufferedInputStream(new FileInputStream(path1.toFile()));
				BufferedInputStream fis2 = new BufferedInputStream(new FileInputStream(path2.toFile()))) {
			int ch = 0;
			long pos = 1;
			while ((ch = fis1.read()) != -1) {
				if (ch != fis2.read()) {
					return pos;
				}
				pos++;
			}
			if (fis2.read() == -1) {
				return -1;
			} else {
				return pos;
			}
		}
	}

	public static String synchronizeDirectories(final Path sourcePath, final Path targetPath) throws IOException {
		final StringBuilder report = new StringBuilder();
		if (Files.exists(sourcePath)) {
			if (!Files.exists(targetPath)) {
				Files.createDirectories(targetPath);
			}
			for (final Path fileInSource : Files.list(sourcePath).collect(Collectors.toList())) {
				final Path targetFilePath = Paths.get(targetPath.toAbsolutePath().toString(),
						fileInSource.getFileName().toString());
				if (Files.exists(targetFilePath) && filesCompareByByte(fileInSource, targetFilePath) == -1L) {
					report.append(fileInSource.toString() + " == " + targetFilePath.toString() + "\n");
					// file is equals
				} else {
					Files.copy(fileInSource, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
					report.append("COPIED " + fileInSource.toString() + " TO " + targetFilePath.toString() + "\n");
				}
			}
		} else {
			report.append(sourcePath.toString() + " NOT EXISTS\n");
		}
		return report.toString();
	}

	private SynchronizeHelper() {
		throw new UnsupportedOperationException("Just for static usage");

	}

}
