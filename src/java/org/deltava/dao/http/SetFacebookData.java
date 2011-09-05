// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.net.HttpURLConnection;

import org.json.*;

import org.apache.log4j.Logger;

import org.deltava.beans.fb.NewsEntry;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to send data to Facebook via the Graph API.
 * @author Luke
 * @version 4.0
 * @since 3.4
 */

public class SetFacebookData extends FacebookDAO {
	
	private static final Logger log = Logger.getLogger(SetFacebookData.class); 

	/**
	 * Writes a news entry to a user's Facebook feed.
	 * @param nws a NewsEntry bean
	 * @throws DAOException if an error occurs
	 */
	public void write(NewsEntry nws) throws DAOException {
		write(nws, true);
	}

	/**
	 * Writes a news entry to the application's Facebook feed.
	 * @param nws a NewsEntry bean
	 * @throws DAOException if an error occurs
	 */
	public void writeApp(NewsEntry nws) throws DAOException {
		write(nws, false);
	}
	
	/**
	 * Helper method to write News entries.
	 */
	private void write(NewsEntry nws, boolean asUser) throws DAOException {
		
		// Create the post body
		POSTBuilder postData = new POSTBuilder();
		postData.addIfPresent("message", nws.getBody());
		postData.addIfPresent("caption", nws.getLinkCaption());
		postData.addIfPresent("link", nws.getURL());
		postData.addIfPresent("picture", nws.getImageURL());
		postData.addIfPresent("description", nws.getLinkDescription());
		postData.addIfPresent("access_token", _token);
		
		try {
			setMethod("POST");
			String url = SystemData.get("users.facebook.url.feed");
			if (!asUser)
				url = url.replace("/me/", "/" + _appID + "/");
			
			// Send the data
			init(url);
			DataOutputStream out = new DataOutputStream(getOut());
			out.writeBytes(postData.getBody());
			out.flush();
			out.close();
			
			// Get the response code
			int resultCode = getResponseCode();
			if (resultCode != HttpURLConnection.HTTP_OK)
				throw new HTTPDAOException("Invalid Result Code", resultCode);
			
			// Parse the return value
			InputStream is = getIn();
			JSONTokener jtk = new JSONTokener(new InputStreamReader(is));
			JSONObject jo = new JSONObject(jtk);
			is.close();
			
			// Set the ID
			nws.setID(jo.getString("id"));
		} catch (Exception e) {
			if (_warnMode)
				log.error(e.getClass().getName() + " - " + e.getMessage());
			else 
				throw (e instanceof HTTPDAOException) ? (HTTPDAOException) e : new DAOException(e);
		}
	}
}