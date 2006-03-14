// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.http;

import java.io.*;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;

import org.apache.log4j.Logger;

import org.deltava.util.ConfigLoader;

/**
 * A utility class to allow SSL connections based on non-standard certificates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class SSLUtils {
	
	private static final Logger log = Logger.getLogger(SSLUtils.class);
	
	/**
	 * Loads an X.509 certitifcate from a file.
	 * @param fileName the file name
	 * @return an X.509 certificate
	 * @throws IOException if the file cannot be read
	 * @throws CertificateException if the certificate is invalid
	 * @see ConfigLoader#getStream(String)
	 */
	public static X509Certificate load(String fileName) throws IOException, CertificateException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		InputStream is = ConfigLoader.getStream(fileName);
		X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
		is.close();
		return cert;
	}

	/**
	 * Creates an SSL context using a Trust store with a given X.509 certificate.
	 * @param cert the X.509 certificate
	 * @return an SSL context
	 */
	public static SSLContext getContext(X509Certificate cert) throws SecurityException {
		try {
			// Init the trust manager
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			ks.setCertificateEntry("x509.cert", cert);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);

			// Get the SSL context
			SSLContext ctxt = SSLContext.getInstance("SSLv3");
			ctxt.init(null, tmf.getTrustManagers(), null);
			return ctxt;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new SecurityException(e);
		}
	}
}