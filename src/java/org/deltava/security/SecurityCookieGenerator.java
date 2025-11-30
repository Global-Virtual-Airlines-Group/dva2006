// Copyright 2004, 2008, 2009, 2011, 2013, 2014, 2015, 2016, 2021, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.deltava.crypt.*;

/**
 * A class to generate/interpret cookies to store persistent authentication information.
 * @author Luke
 * @version 12.4
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
	 * @return a SecurityCookieData bean
	 * @throws SecurityException if the cookie contains invalid data
	 */
	public static synchronized SecurityCookieData readCookie(String cookieText) throws SecurityException {
		
		String rawToken = null;
		byte[] encData = Base64.getDecoder().decode(cookieText);
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
		String alg = cookieData.getOrDefault("alg", "SHA-256");
		boolean isUser = cookieData.containsKey("ln");
		StringBuilder buf = new StringBuilder("uid:");
		buf.append(cookieData.get("uid"));
		buf.append("@addr:");
		buf.append(cookieData.get("addr"));
		buf.append("@login:");
		buf.append(cookieData.get("login"));
		buf.append("@expiry:");
		buf.append(cookieData.get("expiry"));
		buf.append("@alg:");
		buf.append(alg);
		if (isUser) {
			buf.append("@fn:");
			buf.append(cookieData.get("fn"));
			buf.append("@ln:");
			buf.append(cookieData.get("ln"));
		}
		
		// Get the message digest for the token
		Base64.Encoder b64e = Base64.getEncoder();
		MessageDigester md = new MessageDigester(alg);
		String digest = b64e.encodeToString(md.digest(buf.toString().getBytes(StandardCharsets.UTF_8)));
		
		// Validate the token signature against what we calculated
		if (!digest.equals(cookieData.get("sig")))
			throw new SecurityException("Security Cookie decryption failure - " + buf);
		
		// Initalize the cookie data
		String uid = cookieData.get("uid");
		SecurityCookieData scData = isUser ? new UserCookieData(uid, cookieData.get("fn"), cookieData.get("ln")) : new SecurityCookieData(uid);
		scData.setRemoteAddr(cookieData.get("addr").replace('%', ':'));
		scData.setSignatureAlgorithm(alg);
		try {
			scData.setLoginDate(Instant.ofEpochMilli(Long.parseLong(cookieData.get("login"), 16)));
		    scData.setExpiryDate(Instant.ofEpochMilli(Long.parseLong(cookieData.get("expiry"), 16)));
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
		buf.append(Long.toHexString(scData.getLoginDate().toEpochMilli()));
		buf.append("@expiry:");
		buf.append(Long.toHexString(scData.getExpiryDate().toEpochMilli()));
		buf.append("@alg:");
		buf.append(scData.getSignatureAlgorithm());
		if (scData instanceof UserCookieData ucData) {
			buf.append("@fn:");
			buf.append(ucData.getFirstName());
			buf.append("@ln:");
			buf.append(ucData.getLastName());
		}
		
		// Get the message digest for the token
		Base64.Encoder b64e = Base64.getEncoder();
		MessageDigester md = new MessageDigester(scData.getSignatureAlgorithm());
		String digest = b64e.encodeToString(md.digest(buf.toString().getBytes(StandardCharsets.UTF_8)));
		
		// Append the message digest
		buf.append("@sig:");
		buf.append(digest);
		
		// Encrypt and encode the token
		return b64e.encodeToString(_encryptor.encrypt(buf.toString().getBytes(StandardCharsets.UTF_8)));
	}
}