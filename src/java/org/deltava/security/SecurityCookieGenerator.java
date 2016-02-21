// Copyright 2004, 2008, 2009, 2011, 2013, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;
import java.nio.charset.StandardCharsets;

import org.deltava.crypt.*;

/**
 * A class to generate/interpret cookies to store persistent authentication information.
 * Cookie data is defined as uid:<b>userID</b>@pwd:<b>password</b>@addr:<b>IP</b>@expiry:<b>date
 * </b>@x:<b>screenX</b>@y:<b>screenY</b>. There is an additional parameter <i>md5</i> which is the MD5
 * signature of the above string encoded in Base64. The password is converted into hex bytes, and the entire
 * string is encrypted using a SecretKeyEncryptor.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public final class SecurityCookieGenerator {

	private static SecretKeyEncryptor _encryptor;
	
	// Singleton
	private SecurityCookieGenerator() {
		super();
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
	public static synchronized SecurityCookieData readCookie(String cookieText) throws SecurityException {
		
		String rawToken = null;
		Base64.Decoder b64d = Base64.getDecoder();
		byte[] encData = b64d.decode(cookieText);
		try {
			rawToken = new String(_encryptor.decrypt(encData), StandardCharsets.UTF_8);
		} catch (CryptoException ce) {
			throw new SecurityException("Cannot decode Security Cookie (size=" + encData.length + ")", ce.getCause());
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
		buf.append("@addr:");
		buf.append(cookieData.get("addr"));
		buf.append("@login:");
		buf.append(cookieData.get("login"));
		buf.append("@expiry:");
		buf.append(cookieData.get("expiry"));
		
		// Get the message digest for the token
		Base64.Encoder b64e = Base64.getEncoder();
		MessageDigester md = new MessageDigester("SHA-1");
		String digest = b64e.encodeToString(md.digest(buf.toString().getBytes(StandardCharsets.UTF_8)));
		
		// Validate the token signature against what we calculated
		if (!digest.equals(cookieData.get("sha1")))
			throw new SecurityException("Security Cookie decryption failure - " + buf.toString());
		
		// Initalize the cookie data
		SecurityCookieData scData = new SecurityCookieData(cookieData.get("uid"));
		scData.setRemoteAddr(cookieData.get("addr").replace('%', ':'));
		try {
			scData.setLoginDate(Long.parseLong(cookieData.get("login"), 16));
		    scData.setExpiryDate(Long.parseLong(cookieData.get("expiry"), 16));
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
	public static synchronized String getCookieData(SecurityCookieData scData) {
		
		// Build the cookie token
		StringBuilder buf = new StringBuilder("uid:");
		buf.append(scData.getUserID());
		buf.append("@addr:");
		buf.append(scData.getRemoteAddr().replace(':', '%'));
		buf.append("@login:");
		buf.append(Long.toHexString(scData.getLoginDate()));
		buf.append("@expiry:");
		buf.append(Long.toHexString(scData.getExpiryDate()));
		
		// Get the message digest for the token
		Base64.Encoder b64e = Base64.getEncoder();
		MessageDigester md = new MessageDigester("SHA-1");
		String digest = b64e.encodeToString(md.digest(buf.toString().getBytes(StandardCharsets.UTF_8)));
		
		// Append the message digest
		buf.append("@sha1:");
		buf.append(digest);
		
		// Encrypt the token
		return b64e.encodeToString(_encryptor.encrypt(buf.toString().getBytes(StandardCharsets.UTF_8)));
	}
}