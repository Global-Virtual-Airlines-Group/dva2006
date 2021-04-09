package org.deltava.crypt;

import java.security.*;
import java.security.interfaces.*;
import java.security.spec.ECGenParameterSpec;

import java.util.Base64;

import junit.framework.TestCase;

public class TestECDSAKeys extends TestCase {
	
	private static final byte[] P256_HEAD = Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgA");

	private static byte[] toUncompressedECPublicKey(ECPublicKey pk) {
		byte[] result = new byte[65];
		byte[] encoded = pk.getEncoded();
		System.arraycopy(encoded, P256_HEAD.length, result, 0, encoded.length - P256_HEAD.length);
		return result;
	}
	
	@SuppressWarnings("static-method")
	public void testGenerateKeys() throws Exception {
		
		KeyPairGenerator kpgen = KeyPairGenerator.getInstance("EC");
		assertNotNull(kpgen);
		kpgen.initialize(new ECGenParameterSpec("secp256r1"));
		
		KeyPair kp = kpgen.generateKeyPair();
		assertNotNull(kp);
		ECPublicKey pubKey = (ECPublicKey) kp.getPublic();
		assertNotNull(pubKey);
		ECPrivateKey pvtKey = (ECPrivateKey) kp.getPrivate();
		assertNotNull(pvtKey);

		byte[] pubUC = toUncompressedECPublicKey(pubKey);
		String pubB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(pubUC);
		String pvtB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(pvtKey.getEncoded());
		System.out.println("pub = " + pubB64);
		System.out.println("pvt = " + pvtB64);
	}
}