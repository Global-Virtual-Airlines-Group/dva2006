// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.crypt;

import java.util.*;

import org.apache.log4j.Logger;
import org.deltava.util.StringUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.security.Signature;
import java.security.cert.*;

/**
 * A utility class to verify Amazon SNS messages.
 * @author Luke
 * @version 8.5
 * @since 8.5
 */

public class SNSVerifier {

	private static final Logger log = Logger.getLogger(SNSVerifier.class);
	
	private static final Map<String, X509Certificate > _certs = new HashMap<String, X509Certificate>();
	
	/**
	 * Loads an X.509 certificate from a URL
	 * @param url the URL
	 * @return the Certificate
	 * @throws IOException if the Certificate data cannot be retrieved
	 * @throws CertificateException if the Certificate cannot be parsed
	 */
	public static Certificate loadCertificate(String url) throws IOException, CertificateException {
		if (_certs.containsKey(url))
			return _certs.get(url);
		
		URL u = new URL(url);
		try (InputStream inStream = u.openStream()) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
			_certs.put(url, cert);
			log.info("Loaded certificate at " + url);
			return cert;
		}
	}
	
	private static String getMessageBytes(JSONObject jo) {
		StringBuilder buf = new StringBuilder("Message\n");
		buf.append(jo.getString("Message"));
		buf.append("\nMessageId\n");
		buf.append(jo.getString("MessageId"));
		if (!StringUtils.isEmpty(jo.optString("Subject"))) {
			buf.append("\nSubject\n");
			buf.append(jo.getString("Subject"));
		}
		
		buf.append("\nTimestamp\n");
		buf.append(jo.getString("Timestamp"));
		buf.append("\nTopicArn\n");
		buf.append(jo.getString("TopicArn"));
		buf.append("\nType\n");
		buf.append(jo.getString("Type"));
		buf.append('\n');
		return buf.toString();
	}
	
	private static String getConfirmationBytes(JSONObject jo) {
		StringBuilder buf = new StringBuilder("Message\n");
		buf.append(jo.getString("Message"));
		buf.append("\nMessageId\n");
		buf.append(jo.getString("MessageId"));
		buf.append("\nSubscribeURL\n");
		buf.append(jo.getString("SubscribeURL"));
		buf.append("\nTimestamp\n");
		buf.append(jo.getString("Timestamp"));
		buf.append("\nToken\n");
		buf.append(jo.getString("Token"));
		buf.append("\nTopicArn\n");
		buf.append(jo.getString("TopicArn"));
		buf.append("\nType\n");
		buf.append(jo.getString("Type"));
		buf.append('\n');
		return buf.toString();
	}
	
	/**
	 * Validates an SNS message.
	 * @param jo a JSONObject with the payload
	 * @return TRUE if valid, otherwise FALSE
	 */
	public static boolean validate(JSONObject jo) {
		try {
			Certificate c = loadCertificate(jo.getString("SigningCertURL"));
			String validationData = "SubscriptionConfirmation".equals(jo.getString("Type")) ? getConfirmationBytes(jo) : getMessageBytes(jo);
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(c.getPublicKey());
			sig.update(validationData.getBytes());
			return sig.verify(Base64.getDecoder().decode(jo.getString("Signature")));
		} catch (Exception e) {
			throw new SecurityException(e.getMessage());
		}
	}
}