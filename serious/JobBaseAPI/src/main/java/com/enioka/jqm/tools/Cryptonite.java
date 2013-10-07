
package com.enioka.jqm.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Cryptonite {

	static String sha1(String input) throws NoSuchAlgorithmException {

		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
			        .substring(1));
		}

		return sb.toString();
	}

	/**
	 * Verifies file's SHA1 checksum
	 * 
	 * @param Filepath
	 *            and name of a file that is to be verified
	 * @param testChecksum
	 *            the expected checksum
	 * @return true if the expeceted SHA1 checksum matches the file's SHA1
	 *         checksum; false otherwise.
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static boolean verifyChecksum(String file, String testChecksum)
	        throws NoSuchAlgorithmException, IOException {

		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		@SuppressWarnings("resource")
		FileInputStream fis = new FileInputStream(file);

		byte[] data = new byte[1024];
		int read = 0;
		while ((read = fis.read(data)) != -1) {
			sha1.update(data, 0, read);
		}
		;
		byte[] hashBytes = sha1.digest();

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < hashBytes.length; i++) {
			sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16)
			        .substring(1));
		}

		String fileHash = sb.toString();

		return fileHash.equals(testChecksum);
	}
}
