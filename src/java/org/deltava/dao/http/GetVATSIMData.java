// Copyright 2007, 2008, 2009, 2011, 2012, 2016, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import static java.net.HttpURLConnection.HTTP_OK;

import org.json.*;

import org.deltava.beans.servinfo.Certificate;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read VATSIM API data.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class GetVATSIMData extends DAO {
	
	/**
	 * Returns information about the selected VATSIM certificate.
	 * @param id the VATSIM certificate ID
	 * @return a Certificate bean
	 * @throws DAOException if an error occurs
	 */
	public Certificate getInfo(String id) throws DAOException {
		
		// Get the URL
		if (StringUtils.isEmpty(id)) return null;
		String url = String.format("%s/%s/", SystemData.get("online.vatsim.validation_url"), id);
		
		try {
			setCompression(Compression.GZIP);
			init(url);
			if (getResponseCode() != HTTP_OK)
				return null;
			
			// Process the JSON document
			JSONObject jo = null;
			try (InputStream is = getIn()) {
				jo = new JSONObject(new JSONTokener(is));
			} catch (IOException ie) {
				throw new DAOException(ie);
			}
			
			// Get user element
			String cid = jo.optString("id");
			if (cid == null)
				return null;
			
			// Check if inactive
			String sDate = jo.optString("susp_date");
			boolean isInactive = !StringUtils.isEmpty(sDate) && !"null".equals(sDate);
			
			// Create the return object
			Certificate c = new Certificate(StringUtils.parse(cid, 0));
			c.setRegistrationDate(StringUtils.parseInstant(jo.getString("reg_date"), "yyyy-MM-dd'T'HH:mm:ss"));
			c.setActive(!isInactive);
			
			// Load Pilot rating
			int pRating = jo.optInt("pilotrating", -1);
			if (pRating >= 0)
				c.addPilotRating("P" + pRating);
			
			return c;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}