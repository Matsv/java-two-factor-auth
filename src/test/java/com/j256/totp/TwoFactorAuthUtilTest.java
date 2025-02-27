package com.j256.totp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.security.GeneralSecurityException;
import java.util.Random;

import org.apache.commons.codec.binary.Base32;
import org.junit.Test;

/**
 * Little test program to show how to use the TwoFactorAuthUtil utility class.
 * 
 * See: https://github.com/j256/java-two-factor-auth
 * 
 * @author graywatson
 */
public class TwoFactorAuthUtilTest {

	public static void main(String[] args) throws Exception {
		TwoFactorAuthUtil twoFactorAuthUtil = new TwoFactorAuthUtil();

		// String base32Secret = twoFactorAuthUtil.generateBase32Secret();
		String base32Secret = "NY4A5CPJZ46LXZCP";

		System.out.println("secret = " + base32Secret);

		// this is the name of the key which can be displayed by the authenticator program
		String keyId = "user@foo.com";
		System.out.println("Image url = " + twoFactorAuthUtil.qrImageUrl(keyId, base32Secret));
		// we can display this image to the user to let them load it into their auth program

		// we can use the code here and compare it against user input
		String code = twoFactorAuthUtil.generateCurrentNumber(base32Secret);

		/*
		 * this little loop is here to show how the number changes over time
		 */
		while (true) {
			long diff =
					TwoFactorAuthUtil.TIME_STEP_SECONDS
							- ((System.currentTimeMillis() / 1000) % TwoFactorAuthUtil.TIME_STEP_SECONDS);
			code = twoFactorAuthUtil.generateCurrentNumber(base32Secret);
			System.out.println("Secret code = " + code + ", change in " + diff + " seconds");
			Thread.sleep(1000);
		}
	}

	@Test
	public void testZeroPrepend() {
		TwoFactorAuthUtil tfau = new TwoFactorAuthUtil();
		Random random = new Random();
		for (int i = 0; i < 10000; i++) {
			int num = random.nextInt(1000000);
			/**
			 * NOTE: Did a speed test of these and the zeroPrepend is ~13x faster.
			 */
			assertEquals(String.format("%06d", num), tfau.zeroPrepend(num, 6));
		}
	}

	@Test
	public void testDecodeBase32() {
		TwoFactorAuthUtil tfau = new TwoFactorAuthUtil();
		Random random = new Random();
		random.nextBytes(new byte[100]);
		Base32 base32 = new Base32();
		for (int i = 0; i < 10000; i++) {
			byte[] bytes = new byte[random.nextInt(10) + 1];
			random.nextBytes(bytes);
			String encoded = base32.encodeAsString(bytes);
			byte[] expected = base32.decode(encoded);
			byte[] actual = tfau.decodeBase32(encoded);
			assertArrayEquals(expected, actual);
		}
	}

	@Test
	public void testVariusKnownSecretTimeCodes() throws GeneralSecurityException {
		TwoFactorAuthUtil tfau = new TwoFactorAuthUtil();
		String secret = "NY4A5CPJZ46LXZCP";
		assertEquals("748810", tfau.generateCurrentNumber(secret, 1000L));
		assertEquals("325893", tfau.generateCurrentNumber(secret, 7451000L));
		assertEquals("064088", tfau.generateCurrentNumber(secret, 15451000L));
		assertEquals("009637", tfau.generateCurrentNumber(secret, 348402049542546145L));
		assertEquals("000743", tfau.generateCurrentNumber(secret, 2049455124374752571L));
		assertEquals("000092", tfau.generateCurrentNumber(secret, 1359002349304873750L));
		assertEquals("000007", tfau.generateCurrentNumber(secret, 6344447817348357059L));
		assertEquals("000000", tfau.generateCurrentNumber(secret, 2125701285964551130L));
	}
}
