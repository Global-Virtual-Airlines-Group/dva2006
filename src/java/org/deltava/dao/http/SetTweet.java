// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;

import org.json.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write to the Twitter API.
 * @author Luke
 * @version 7.2
 * @since 7.2
 */

public class SetTweet extends DAO {

	/**
	 * Posts a tweet.
	 * @param s the tweet text
	 * @return the tweet ID
	 * @throws DAOException if an I/O error occurs
	 */
	public String tweet(String s) throws DAOException {
		
		// Create the post body
		POSTBuilder postData = new POSTBuilder();
		postData.addIfPresent("status", s);
		
		try {
			setMethod("POST");
			
			// Send the data
			init(SystemData.get("users.twitter.url.tweet"));
			try (DataOutputStream out = new DataOutputStream(getOut())) {
				out.writeBytes(postData.getBody());
				out.flush();
			}
			
			// Parse the return value
			JSONObject jo = null;
			try (InputStream is = getIn()) {
				JSONTokener jtk = new JSONTokener(new InputStreamReader(is));
				jo = new JSONObject(jtk);
			}

			return jo.getString("id");
		} catch (Exception e) {
			throw new HTTPDAOException(e);
		}
	}
}