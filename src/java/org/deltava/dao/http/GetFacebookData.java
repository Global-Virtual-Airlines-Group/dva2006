// Copyright 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;

import org.json.*;
import org.apache.log4j.Logger;

import org.deltava.beans.fb.ProfileInfo;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to fetch user data from Facebook via the Graph API. 
 * @author Luke
 * @version 4.1
 * @since 3.4
 */

public class GetFacebookData extends FacebookDAO {
	
	private static final Logger log = Logger.getLogger(GetFacebookData.class);
	
	/**
	 * Retrieves a user's profile information.
	 * @return a ProfileInfo bean
	 * @throws DAOException if an error occurs
	 */
	public ProfileInfo getUserInfo() throws DAOException {
		
		// Build the URL
		StringBuilder buf = new StringBuilder(SystemData.get("users.facebook.url.userInfo"));
		buf.append("?access_token=");
		buf.append(_token);
		
		try {
			init(buf.toString());
			InputStream is = getIn();
			JSONTokener jtk = new JSONTokener(new InputStreamReader(is));
			JSONObject jo = new JSONObject(jtk);
			is.close();
			
			// Check for error
			if (jo.has("error")) {
				JSONObject jerr = jo.getJSONObject("error");
				throw new IOException(jerr.optString("message", "HTTP Error " + getResponseCode()));
			}
			
			// Construct the bean
			ProfileInfo inf = new ProfileInfo(jo.getString("id"));
			inf.setFirstName(jo.getString("first_name"));
			inf.setLastName(jo.getString("last_name"));
			inf.setEMail(jo.optString("email", ""));
			inf.setVerified(jo.optBoolean("verified", false));
			if (jo.has("updated_time"))
				inf.setLastUpdated(StringUtils.parseRFC3339Date(jo.getString("updated_time")));
			return inf;
		} catch (Exception e) {
			if (_warnMode) {
				log.error(e.getClass().getName() + " - " + e.getMessage());
				return null;
			}
			
			throw new DAOException(e);
		}
	}
}