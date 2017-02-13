// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;

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
			setMethod("POST");

			// Send the data
			init(SystemData.get("online.vatsim.rating_url"));
			setRequestHeader("Content-Type", "application/json");
			setRequestHeader("Authorization", "Token token=\"" + SystemData.get("security.key.vatsim") + "\"");
			try (DataOutputStream out = new DataOutputStream(getOut())) {
				out.writeBytes(jo.toString());
				out.flush();
			}

			if (getResponseCode() != HttpURLConnection.HTTP_OK)
				throw new HTTPDAOException("Invalid response code", getResponseCode());
		} catch (IOException ie) {
			throw new HTTPDAOException(ie);
		}
	}
}