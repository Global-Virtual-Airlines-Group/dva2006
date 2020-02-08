// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;

import org.json.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to validate Google CAPTCHA tokens. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class GetGoogleCAPTCHA extends DAO {

	/**
	 * Validates a Google RECAPTCHA response.
	 * @param response the response
	 * @param remoteAddr the client IP address
	 * @return TRUE if valid, otherwise FALSE
	 * @throws DAOException if an error occurs
	 */
	public boolean validate(String response, String remoteAddr) throws DAOException {
		
		// Build the payload
		POSTBuilder pb = new POSTBuilder();
		pb.put("secret", SystemData.get("security.key.recaptcha.secret"));
		pb.put("response", response);
		pb.addIfPresent("remoteip", remoteAddr);
		
		try {
			setReturnErrorStream(true);
			setMethod("POST");
			init("https://www.google.com/recaptcha/api/siteverify");
			try (OutputStream out = getOut()) {
				out.write(pb.toString().getBytes(UTF_8));
				out.flush();
			}

			// Parse the response
			try (InputStream in = getIn()) {
				JSONObject jo = new JSONObject(new JSONTokener(in));
				return jo.optBoolean("success");
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}