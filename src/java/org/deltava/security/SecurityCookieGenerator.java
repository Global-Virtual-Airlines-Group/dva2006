// Copyright 2004, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;
import javax.servlet.http.Cookie;

import org.deltava.crypt.*;

import org.deltava.util.Base64;

/**
 * A class to generate/interpret cookies to store persistent authentication information.
 * Cookie data is defined as uid:<b>userID</b>@pwd:<b>password</b>@addr:<b>IP</b>@expiry:<b>date
 * </b>@x:<b>screenX</b>@y:<b>screenY</b>. There is an additional parameter <i>md5</i> which is the MD5
 * signature of the above string encoded in Base64. The password is converted into hex bytes, and the entire
 * string is encrypted using a SecretKeyEncryptor.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class SecurityCookieGenerator {

	private static SecretKeyEncryptor _encryptor;
	
	// Private constructor
	private SecurityCookieGenerator() {
	}
	
	/**
	 * Initialize the security cookie generate with an initialized SecretKeyEncryptor.
	 * @param crypt the secret key encryptor
	 */
	public static synchronized void init(SecretKeyEncryptor crypt) {
		_encryptor = crypt;
	}
	
	/**
	 * Parses a supplied security cookie value into its component parts. 
	 * @param cookieText the Security Cookie value
	 * @throws SecurityException if the cookie contains invalid data
	 * @throws CryptoException if the decryption failed
	 */
	public static SecurityCookieData readCookie(String cookieText) {
		
		// Decode the Base64 data
		byte[] encData = Base64.decode(cookieText);
		String rawToken = new String(_encryptor.decrypt(encData));
		
		// Check that it decrypted properly
		if (!rawToken.startsWith("uid:"))
			throw new SecurityException("Security Cookie decryption failure");
		
		// Parse the token
		Map<String, String> cookieData = new HashMap<String, String>();
		StringTokenizer tkns = new StringTokenizer(rawToken, "@"); 
		while (tkns.hasMoreTokens()) {
			StringTokenizer tkn2 = new StringTokenizer(tkns.nextToken(), ":");
			if (tkn2.countTokens() == 2)
				cookieData.put(tkn2.nextToken(), tkn2.nextToken());
		}
		
		// Rebuild the message token
		StringBuilder buf = new StringBuilder("uid:");
		buf.append(cookieData.get("uid"));
		buf.append("@pwd:");
		buf.append(cookieData.get("pwd"));
		buf.append("@addr:");
		buf.append(cookieData.get("addr"));
		buf.append("@expiry:");
		buf.append(cookieData.get("expiry"));
		buf.append("@x:");
		buf.append(cookieData.get("x"));
		buf.append("@y:");
		buf.append(cookieData.get("y"));
		
		// Get the message digest for the token
		MessageDigester md = new MessageDigester("MD5");
		String digest = Base64.encode(md.digest(buf.toString().getBytes()));
		
		// Validate the token signature against what we calculated
		if (!digest.equals(cookieData.get("md5")))
			throw new SecurityException("Security Cookie decryption failure - " + buf.toString());
		
		// Initalize the cookie data
		SecurityCookieData scData = new SecurityCookieData(cookieData);
			
		try {
		    scData.setExpiryDate(Long.parseLong(cookieData.get("expiry"), 16));
			
			// Convert the hex password into a String
			String rawPwd = cookieData.get("pwd");
			StringBuilder pwdBuf = new StringBuilder();
			for (int x = 0; x < rawPwd.length(); x += 2) {
				int hexByte = Integer.parseInt(rawPwd.substring(x, x+ 2), 16);
				pwdBuf.append((char) hexByte);
			}
			
			scData.setPassword(pwdBuf.toString());
		} catch (NumberFormatException nfe) {
			throw new SecurityException("Invalid Security Cookie Data");
		}
		
		return scData;
	}
	
	/**
	 * Generates the encrypted cookie.
	 * @param cookieName
	 * @param scData
	 * @return the encrypted cookie.
	 */
	public static Cookie getCookie(String cookieName, SecurityCookieData scData) {
		
		// Build the cookie token
		StringBuilder buf = new StringBuilder("uid:");
		buf.append(scData.getUserID());
		buf.append("@pwd:"); // Convert password bytes to HEX to allow commas and colons
		buf.append(scData.getPasswordBytes());
		buf.append("@addr:");
		buf.append(scData.getRemoteAddr());
		buf.append("@expiry:");
		buf.append(Long.toHexString(scData.getExpiryDate()));
		buf.append("@x:");
		buf.append(Integer.toHexString(scData.getScreenX()));
		buf.append("@y:");
		buf.append(Integer.toHexString(scData.getScreenY()));
		
		// Get the message digest for the token
		MessageDigester md = new MessageDigester("MD5");
		String digest = Base64.encode(md.digest(buf.toString().getBytes()));
		
		// Append the message digest
		buf.append("@md5:");
		buf.append(digest);
		
		// Encrypt the token
		byte[] encToken = _encryptor.encrypt(buf.toString().getBytes());
		
		// Encode the encrypted data via Base64
		Cookie c = new Cookie(cookieName, Base64.encode(encToken));
		c.setPath("/");
		c.setVersion(1);
		c.setMaxAge(-1);
		return c;
	}
}