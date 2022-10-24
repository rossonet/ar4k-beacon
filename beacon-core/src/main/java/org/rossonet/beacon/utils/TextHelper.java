package org.rossonet.beacon.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public final class TextHelper {

	private static final String ENCRYPTION_ALGORITHM = "AES";

	public static String convertByteArrayToHexString(final byte[] arrayBytes) {
		final StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arrayBytes.length; i++) {
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return stringBuffer.toString();
	}

	public static byte[] decryptData(final byte[] encryptedData, final byte[] key) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		final Cipher c = Cipher.getInstance(ENCRYPTION_ALGORITHM);
		final SecretKeySpec k = new SecretKeySpec(key, ENCRYPTION_ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, k);
		return c.doFinal(encryptedData);
	}

	public static void deleteDirectory(final File file) {
		if (Files.exists(Paths.get(file.getAbsolutePath()))) {
			for (final File subfile : file.listFiles()) {
				if (subfile.isDirectory()) {
					deleteDirectory(subfile);
				}
				subfile.delete();
			}
		}
	}

	public static byte[] encryptData(final byte[] dataToEncrypt, final byte[] key) throws IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		final Cipher c = Cipher.getInstance(ENCRYPTION_ALGORITHM);
		final SecretKeySpec k = new SecretKeySpec(key, ENCRYPTION_ALGORITHM);
		c.init(Cipher.ENCRYPT_MODE, k);
		return c.doFinal(dataToEncrypt);
	}

	public static Map<String, String> getParametersInUrlQuery(final String query) {
		final String[] params = query.split("&");
		final Map<String, String> map = new HashMap<String, String>();

		for (final String param : params) {
			final String name = param.split("=")[0];
			final String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;

	}

	@SuppressWarnings("unchecked")
	public static <O extends Serializable> O objectFromString(final String string, final Class<O> clazz)
			throws IOException, ClassNotFoundException {
		final byte[] data = Base64.getDecoder().decode(string);
		final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		final Object o = ois.readObject();
		ois.close();
		return (O) o;
	}

	public static String objectToString(final Serializable object) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(object);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	public static List<String> splitFixSize(final String s, final int chunkSize) {
		final List<String> chunks = new ArrayList<>();
		for (int i = 0; i < s.length(); i += chunkSize) {
			chunks.add(s.substring(i, Math.min(s.length(), i + chunkSize)));
		}
		return chunks;
	}

	private TextHelper() {
		throw new UnsupportedOperationException("Just for static usage");
	}

}
