/**
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */
package org.rossonet.beacon.utils;

import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/*
 *
 * @author Andrea Ambrosini
 *
 *         Classe helper per la gestione delle password OTP.
 *         https://datatracker.ietf.org/doc/html/rfc6238
 */
public final class OtpHelper {
	private final static int[] DIGITS_POWER = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };

	public static boolean checkTOTP(final String seedOTP, final String otpCode, final int finestraOTP,
			final String returnDigits) {
		boolean result = false;
		final List<String> validOtps = new ArrayList<String>();
		final long millisec = new java.util.Date().getTime();
		final long end = millisec + (Long.valueOf(finestraOTP * 500));
		long counter = millisec - (Long.valueOf(finestraOTP * 500));
		while (counter <= end) {
			validOtps.add(generateTOTP512(seedOTP, (String.valueOf(counter)), returnDigits));
			counter++;
		}
		if (validOtps.contains(otpCode)) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	public static String generateTOTP(final String key, final String time, final String returnDigits) {
		return generateTOTP(key, time, returnDigits, "HmacSHA1");
	}

	public static String generateTOTP(final String key, String time, final String returnDigits, final String crypto) {
		final int codeDigits = Integer.decode(returnDigits).intValue();
		String result = null;
		while (time.length() < 16) {
			time = "0" + time;
		}
		final byte[] msg = hexStr2Bytes(time);
		final byte[] k = hexStr2Bytes(key);
		final byte[] hash = hmac_sha(crypto, k, msg);
		final int offset = hash[hash.length - 1] & 0xf;
		final int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
				| ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
		final int otp = binary % DIGITS_POWER[codeDigits];
		result = Integer.toString(otp);
		while (result.length() < codeDigits) {
			result = "0" + result;
		}
		return result;
	}

	public static String generateTOTP256(final String key, final String time, final String returnDigits) {
		return generateTOTP(key, time, returnDigits, "HmacSHA256");
	}

	public static String generateTOTP512(final String key, final String time, final String returnDigits) {
		return generateTOTP(key, time, returnDigits, "HmacSHA512");
	}

	public static String getRandomHexString(final int numchars) {
		final Random r = new Random();
		final StringBuffer sb = new StringBuffer();
		while (sb.length() < numchars) {
			sb.append(Integer.toHexString(r.nextInt()));
		}
		return sb.toString().substring(0, numchars);
	}

	private static byte[] hexStr2Bytes(final String hex) {
		final byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
		final byte[] ret = new byte[bArray.length - 1];
		for (int i = 0; i < ret.length; i++)
			ret[i] = bArray[i + 1];
		return ret;
	}

	private static byte[] hmac_sha(final String crypto, final byte[] keyBytes, final byte[] text) {
		try {
			final Mac hmac = Mac.getInstance(crypto);
			final SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
			hmac.init(macKey);
			return hmac.doFinal(text);
		} catch (final GeneralSecurityException gse) {
			throw new UndeclaredThrowableException(gse);
		}
	}

	private OtpHelper() {
		throw new UnsupportedOperationException("Just for static usage");
	}
}
