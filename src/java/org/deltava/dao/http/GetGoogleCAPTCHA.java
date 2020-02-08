// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;

import org.json.*;

import org.deltava.beans.system.CAPTCHAResult;
import org.deltava.dao.DAOException;
import org.deltava.util.StringUtils;
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
	 * @return a CAPTCHAResult bean
	 * @throws DAOException if an error occurs
	 */
	public CAPTCHAResult validate(String response, String remoteAddr) throws DAOException {
		
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
				CAPTCHAResult result = new CAPTCHAResult (jo.optBoolean("success"));
				result.setHostName(jo.optString("hostname"));
				result.setChallengeTime(StringUtils.parseInstant(jo.getString("challenge_ts"), "yyyy-MM-dd'T'HH:mm:ssZZ"));
				JSONArray msgs = jo.optJSONArray("error-codes");
				if (msgs != null)
					msgs.forEach(msg -> result.addMessage(String.valueOf(msg))); 
				
				return result;
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}