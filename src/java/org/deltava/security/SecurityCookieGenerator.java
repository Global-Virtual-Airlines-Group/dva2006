// Copyright 2004, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;
import java.io.UnsupportedEncodingException;

import org.deltava.crypt.*;

import org.deltava.util.Base64;

/**
 * A class to generate/interpret cookies to store persistent authentication information.
 * Cookie data is defined as uid:<b>userID</b>@pwd:<b>password</b>@addr:<b>IP</b>@expiry:<b>date
 * </b>@x:<b>screenX</b>@y:<b>screenY</b>. There is an additional parameter <i>md5</i> which is the MD5
 * signature of the above string encoded in Base64. The password is converted into hex bytes, and the entire
 * string is encrypted using a SecretKeyEncryptor.
 * @author Luke
 * @version 2.2
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
	 */
	public static SecurityCookieData readCookie(String cookieText) throws SecurityException {
		
		// Decode the Base64 data
		if (!cookieText.endsWith("="))
			cookieText += "=";
			
		String rawToken = null;
		try {
			byte[] encData = Base64.decode(cookieText);
			try {
				rawToken = new String(_encryptor.decrypt(encData), "UTF-8");
			} catch (CryptoException ce) {
				if (ce.getPayload() instanceof byte[])
					throw new SecurityException("Cannot decode " + new String((byte[]) ce.getPayload()), ce.getCause());
			}
		} catch (UnsupportedEncodingException uee) {
			throw new SecurityException("UTF-8 not available", uee);
		}
		
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
		try {
			MessageDigester md = new MessageDigester("MD5");
			String digest = Base64.encode(md.digest(buf.toString().getBytes("UTF-8")));
		
			// Validate the token signature against what we calculated
			if (!digest.equals(cookieData.get("md5")))
				throw new SecurityException("Security Cookie decryption failure - " + buf.toString());
		} catch (UnsupportedEncodingException uee) {
			throw new SecurityException("UTF-8 not available");
		}
		
		// Initalize the cookie data
		SecurityCookieData scData = new SecurityCookieData(cookieData);
		try {
		    scData.setExpiryDate(Long.parseLong(cookieData.get("expiry"), 16));
		    scData.setScreenSize(Integer.parseInt(cookieData.get("x"), 16), 
		    		Integer.parseInt(cookieData.get("y"), 16));
			
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
	 * @param scData the cookie data
	 * @return the encrypted cookie.
	 */
	public static String getCookieData(SecurityCookieData scData) {
		
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
		try {
			MessageDigester md = new MessageDigester("MD5");
			String digest = Base64.encode(md.digest(buf.toString().getBytes("UTF-8")));
		
			// Append the message digest
			buf.append("@md5:");
			buf.append(digest);
		
			// Encrypt the token
			return Base64.encode(_encryptor.encrypt(buf.toString().getBytes("UTF-8")));
		} catch (UnsupportedEncodingException uee) {
			return "";
		}
	}
}