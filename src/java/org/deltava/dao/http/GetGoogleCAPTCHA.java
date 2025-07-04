// Copyright 2020, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.net.SocketTimeoutException;

import org.json.*;

import org.deltava.beans.system.CAPTCHAResult;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to validate Google CAPTCHA tokens. 
 * @author Luke
 * @version 11.6
 * @since 9.0
 */

public class GetGoogleCAPTCHA extends DAO {

	/**
	 * Validates a Google RECAPTCHA response.
	 * @param response the response
	 * @param remoteAddr the client IP address
	 * @return a CAPTCHAResult bean
	 * @throws DAOException if an error occurs
	 * @throws IOException if the operation times out
	 */
	public CAPTCHAResult validate(String response, String remoteAddr) throws IOException, DAOException {
		
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
				out.write(pb.getBody(UTF_8));
				out.flush();
			}

			// Parse the response
			try (InputStream in = getIn()) {
				JSONObject jo = new JSONObject(new JSONTokener(in));
				CAPTCHAResult result = new CAPTCHAResult(jo.optBoolean("success"), jo.optString("hostname"));
				if (result.getIsSuccess())
					result.setChallengeTime(StringUtils.parseInstant(jo.getString("challenge_ts"), "yyyy-MM-dd'T'HH:mm:ssVV"));
				JSONArray msgs = jo.optJSONArray("error-codes");
				if (msgs != null)
					msgs.forEach(msg -> result.addMessage(String.valueOf(msg))); 
				
				return result;
			}
		} catch (SocketTimeoutException te) {
			throw new IOException(te);
		} catch (JSONException je) {
			throw new DAOException(je) {{ setWarning(true); }};
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}