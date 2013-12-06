/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.deliverabletools;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Cryptonite {

	public static String sha1(final String input) throws NoSuchAlgorithmException {

		final MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		final byte[] result = mDigest.digest(input.getBytes());
		final StringBuffer sb = new StringBuffer();

		for (int i = 0; i < result.length; i++)
		{
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
	 */
	public static boolean verifyChecksum(final String file, final String testChecksum) throws NoSuchAlgorithmException
	{
		final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		FileInputStream fis = null;

		try
		{
			fis = new FileInputStream(file);
			final byte[] data = new byte[1024];
			int read = 0;

			while ((read = fis.read(data)) != -1)
			{
				sha1.update(data, 0, read);
			};

			final byte[] hashBytes = sha1.digest();
			final StringBuffer sb = new StringBuffer();

			for (int i = 0; i < hashBytes.length; i++)
			{
				sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16)
						.substring(1));
			}

			final String fileHash = sb.toString();

			if (fis != null)
			{
				fis.close();
			}

			return fileHash.equals(testChecksum);
		} catch (IOException e)
		{
			try {
				if (fis != null)
				{
					fis.close();
				}
			} catch (IOException e1) {

			}
			e.printStackTrace();
			return false;
		}
		finally
		{
			try {
				if (fis != null)
				{
					fis.close();
				}
			} catch (IOException e) {

			}
		}
	}
}
