// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to fetch authentication data from Facebook via the Graph API. 
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class GetFacebookAuth extends FacebookDAO {
	
	private static final Logger log = Logger.getLogger(GetFacebookAuth.class);
	
	/**
	 * Retrieves an access token from a particular user authroization callback code.
	 * @param code the callback code
	 * @return an access token
	 * @throws DAOException if an errror occurs
	 */
	public String getAccessToken(String code, String redirectURI) throws DAOException {
		
		// Build the URL
		StringBuilder buf = new StringBuilder(SystemData.get("users.facebook.url.accessToken"));
		buf.append("?client_id=");
		buf.append(_appID);
		buf.append("&redirect_uri=");
		buf.append(redirectURI);
		buf.append("&client_secret=");
		buf.append(_appSecret);
		buf.append("&code=");
		buf.append(code);
		
		try {
			init(buf.toString());
			InputStream is = getIn();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String rawToken = br.readLine();
			is.close();
			
			// Parse the token
			StringTokenizer tkns = new StringTokenizer(rawToken, "&");
			while (tkns.hasMoreTokens()) {
				StringTokenizer tkn = new StringTokenizer(tkns.nextToken(), "=");
				String name = tkn.nextToken();
				if ("access_token".equals(name))
					return tkn.nextToken();
			}
		} catch (IOException ie) {
			if (_warnMode)
				log.error(ie.getClass().getName() + " - " + ie.getMessage());
			else
				throw new DAOException(ie);
		}
			
		return null;
	}
	
	/**
	 * Retrieves an application access token.
	 * @return the token
	 * @throws DAOException if an error occurs
	 */
	public String getAppAccessToken() throws DAOException {

		// Create the post body
		POSTBuilder postData = new POSTBuilder();
		postData.put("grant_type", "client_cred");
		postData.put("client_id", _appID);
		postData.put("client_secret", _appSecret);
		
		try {
			setMethod("POST");
			init(SystemData.get("users.facebook.url.accessToken"));
			
			// Send the data
			DataOutputStream out = new DataOutputStream(getOut());
			out.writeBytes(postData.getBody());
			out.flush();
			out.close();
			
			// Get the response code
			int resultCode = getResponseCode();
			if (resultCode != HttpURLConnection.HTTP_OK)
				throw new DAOException("Error Code " + resultCode);
			
			// Parse the return value
			InputStream is = getIn();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String rawToken = br.readLine();
			is.close();
			
			// Parse the token
			StringTokenizer tkns = new StringTokenizer(rawToken, "&");
			while (tkns.hasMoreTokens()) {
				StringTokenizer tkn = new StringTokenizer(tkns.nextToken(), "=");
				String name = tkn.nextToken();
				if ("access_token".equals(name))
					return tkn.nextToken();
			}
		} catch (Exception e) {
			if (_warnMode)
				log.error(e.getClass().getName() + " - " + e.getMessage());
			else
				throw new DAOException(e);
		}
		
		return null;
	}
}