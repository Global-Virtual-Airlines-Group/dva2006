// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import org.deltava.beans.servinfo.PilotRating;

import org.deltava.dao.DAOException;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write VATSIM user data.
 * @author Luke
 * @version 7.2
 * @since 7.2
 */

public class SetVATSIMData extends DAO {

	/**
	 * Assigns a VATSIM Pilot Rating to a user.
	 * @param pr the PilotRating bean
	 * @throws DAOException if an error occurs
	 */
	public void addRating(PilotRating pr) throws DAOException {
		
		// Build the payload
		JSONObject jo = new JSONObject();
		jo.put("cid", String.valueOf(pr.getID()));
		jo.put("rating", pr.getRatingCode().substring(1));
		jo.put("instructor", String.valueOf(pr.getInstructor()));
		
		try {
			setReturnErrorStream(true);
			setMethod("POST");

			// Send the data
			init(SystemData.get("online.vatsim.rating_url"));
			setRequestHeader("Content-Type", "application/json; charset=utf-8");
			setRequestHeader("Accept", "application/json");
			setRequestHeader("Authorization", "Token token=\"" + SystemData.get("security.key.vatsim") + "\"");
			try (OutputStream out = getOut()) {
				out.write(jo.toString().getBytes(StandardCharsets.UTF_8));
				out.flush();
			}

			int code = getResponseCode();
			if (code != HttpURLConnection.HTTP_OK) {
				JSONObject eo = new JSONObject(new JSONTokener(getIn()));
				throw new HTTPDAOException(String.valueOf(eo), code);
			}
		} catch (IOException ie) {
			throw new HTTPDAOException(ie);
		}
	}
}