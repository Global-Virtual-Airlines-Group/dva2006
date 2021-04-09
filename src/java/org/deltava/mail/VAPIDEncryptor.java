// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import java.nio.*;
import java.nio.charset.StandardCharsets;

import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;

import org.deltava.beans.PushEndpoint;
import org.deltava.util.StringUtils;

/**
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

class VAPIDEncryptor {

	private static final byte[] P256_HEAD = Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgA");
	
	private static final SecureRandom SR = new SecureRandom();

	// static class
	private VAPIDEncryptor() {
		super();
	}

	public static byte[] encrypt(String msg, PushEndpoint ep) throws GeneralSecurityException {
		if (StringUtils.isEmpty(msg)) return null;

		byte[] payload = msg.getBytes(StandardCharsets.UTF_8);

		// Get the key pair
		KeyPairGenerator kpgen = KeyPairGenerator.getInstance("EC");
		kpgen.initialize(new ECGenParameterSpec("secp256r1"));
		KeyPair asKeyPair = kpgen.genKeyPair();
		ECPublicKey asPublicKey = (ECPublicKey) asKeyPair.getPublic();
		byte[] uncompressedASPublicKey = toUncompressedECPublicKey(asPublicKey);
		
		ECPublicKey uaPublicKey = fromX509Key(ep.getPub256DH());
		
		KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
	    keyAgreement.init(asKeyPair.getPrivate());
	    keyAgreement.doPhase(uaPublicKey, true);
	    byte[] ecdhSecret = keyAgreement.generateSecret();
	    
	    byte[] salt = new byte[16];
	    SR.nextBytes(salt);
	    
	    // ## Use HKDF to combine the ECDH and authentication secrets
	    // # HKDF-Extract(salt=auth_secret, IKM=ecdh_secret)
	    // PRK_key = HMAC-SHA-256(auth_secret, ecdh_secret)
	    Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
	    hmacSHA256.init(new SecretKeySpec(Base64.getUrlDecoder().decode(ep.getAuth()), "HmacSHA256"));
	    byte[] prkKey = hmacSHA256.doFinal(ecdhSecret);
	    
	    // # HKDF-Expand(PRK_key, key_info, L_key=32)
	    // key_info = "WebPush: info" || 0x00 || ua_public || as_public
	    byte[] keyInfo = concat("WebPush: info\0".getBytes(StandardCharsets.UTF_8), toUncompressedECPublicKey(uaPublicKey), uncompressedASPublicKey);

	    // IKM = HMAC-SHA-256(PRK_key, key_info || 0x01)
        hmacSHA256.init(new SecretKeySpec(prkKey, "HmacSHA256"));
        hmacSHA256.update(keyInfo);
        hmacSHA256.update((byte) 1);
        byte[] ikm = hmacSHA256.doFinal();
        
        // ## HKDF calculations from RFC 8188
        // # HKDF-Extract(salt, IKM)
        // PRK = HMAC-SHA-256(salt, IKM)
        hmacSHA256.init(new SecretKeySpec(salt, "HmacSHA256"));
        byte[] prk = hmacSHA256.doFinal(ikm);
        
        // # HKDF-Expand(PRK, cek_info, L_cek=16)
        // cek_info = "Content-Encoding: aes128gcm" || 0x00
        byte[] cekInfo = "Content-Encoding: aes128gcm\0".getBytes(StandardCharsets.UTF_8);
        // CEK = HMAC-SHA-256(PRK, cek_info || 0x01)[0..15]
        hmacSHA256.init(new SecretKeySpec(prk, "HmacSHA256"));
        hmacSHA256.update(cekInfo);
        hmacSHA256.update((byte) 1);
        byte[] cek = hmacSHA256.doFinal();
        cek = Arrays.copyOfRange(cek, 0, 16);
        
        // # HKDF-Expand(PRK, nonce_info, L_nonce=12)
        // nonce_info = "Content-Encoding: nonce" || 0x00
        byte[] nonceInfo = "Content-Encoding: nonce\0".getBytes(StandardCharsets.UTF_8);
        // NONCE = HMAC-SHA-256(PRK, nonce_info || 0x01)[0..11]
        hmacSHA256.init(new SecretKeySpec(prk, "HmacSHA256"));
        hmacSHA256.update(nonceInfo);
        hmacSHA256.update((byte) 1);
        byte[] nonce = hmacSHA256.doFinal();
        nonce = Arrays.copyOfRange(nonce, 0, 12);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(cek, "AES"), new GCMParameterSpec(128, nonce));
        
        byte[] encrypted = cipher.doFinal(concat(payload, new byte[] {2}));
        
        ByteBuffer encryptedArrayLength = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        encryptedArrayLength.putInt(encrypted.length);
        
        byte[] header = concat(salt, encryptedArrayLength.array(), new byte[] { (byte) uncompressedASPublicKey.length }, uncompressedASPublicKey);
        return concat(header, encrypted);
	}

	private static byte[] concat(byte[]... arrays) {

		// Determine the length of the result array
		int totalLength = 0;
		for (byte[] array : arrays)
			totalLength += array.length;

		// create the result array
		byte[] result = new byte[totalLength];

		// copy the source arrays into the result array
		int currentIndex = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, result, currentIndex, array.length);
			currentIndex += array.length;
		}

		return result;
	}

	static ECPrivateKey fromPCKS8Key(String base64) throws GeneralSecurityException {

		byte[] data = Base64.getUrlDecoder().decode(base64);

		KeyFactory kf = KeyFactory.getInstance("EC");
		PKCS8EncodedKeySpec pkcs8spec = new PKCS8EncodedKeySpec(data);
		return (ECPrivateKey) kf.generatePrivate(pkcs8spec);
	}

	static ECPublicKey fromX509Key(String base64) throws GeneralSecurityException {

		byte[] w = Base64.getUrlDecoder().decode(base64);
		byte[] encodedKey = new byte[P256_HEAD.length + w.length];
		System.arraycopy(P256_HEAD, 0, encodedKey, 0, P256_HEAD.length);
		System.arraycopy(w, 0, encodedKey, P256_HEAD.length, w.length);

		KeyFactory kf = KeyFactory.getInstance("EC");
		X509EncodedKeySpec ecpks = new X509EncodedKeySpec(encodedKey);
		return (ECPublicKey) kf.generatePublic(ecpks);
	}

	static byte[] toUncompressedECPublicKey(ECPublicKey pubKey) {
		byte[] result = new byte[65];
		byte[] encoded = pubKey.getEncoded();
		System.arraycopy(encoded, P256_HEAD.length, result, 0, encoded.length - P256_HEAD.length);
		return result;
	}
}