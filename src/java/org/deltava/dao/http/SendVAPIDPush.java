// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.IOException;
import java.io.OutputStream;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to send VAPID push messages.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class SendVAPIDPush extends DAO {
	
	private final String _url; 

	/**
	 * Initializes the Data Access Object.
	 * @param url the endpoint URL
	 */
	public SendVAPIDPush(String url) {
		super();
		_url = url;
	}
	
	/**
	 * Sends a VAPID message.
	 * @param jwt the JWT token
	 * @param body the encrypted message body
	 * @param ttl the time to live on the VAPID service in seconds
	 * @return the HTTP response code from the service
	 * @throws DAOException if an error occurs
	 */
	public int send(String jwt, byte[] body, long ttl) throws DAOException {
		
		// Build the Auth header
		StringBuilder authBuf = new StringBuilder("vapid t=");
		authBuf.append(jwt);
		authBuf.append(", k=");
		authBuf.append(SystemData.get("security.key.push.pub"));
		
		boolean hasBody = ((body != null) && (body.length > 0));
		try {
			setReturnErrorStream(true);
			setMethod("POST");
			
			// Send the data
			init(_url);
			setRequestHeader("TTL", String.valueOf(Math.max(10, ttl)));
			setRequestHeader("Authorization", authBuf.toString());
			if (hasBody) {
				setRequestHeader("Content-Type", "application/octet-stream");
				setRequestHeader("Content-Encoding", "aes128gcm");
			}
			
			try (OutputStream out = getOut()) {
				if (hasBody) out.write(body);
				out.flush();
			}
			
			return getResponseCode();
		} catch (IOException ie) {
			throw new HTTPDAOException(ie);
		}
	}
}