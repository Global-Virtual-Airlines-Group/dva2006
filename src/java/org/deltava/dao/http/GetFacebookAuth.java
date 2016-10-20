// Copyright 2010, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 7.2
 * @since 3.4
 */

public class GetFacebookAuth extends FacebookDAO {
	
	private static final Logger log = Logger.getLogger(GetFacebookAuth.class);
	
	/**
	 * Retrieves an access token from a particular user authroization callback code.
	 * @param code the callback code
	 * @param redirectURI the redirection URL when complete
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
			init(buf.toString()); String rawToken = null;
			try (InputStream is = getIn()) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
					rawToken = br.readLine();
				}
			}
			
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
	 * Exchanges a Facebook short-term token for a long-term token.
	 * @return a long-term token, or null
	 * @throws DAOException if an error occurs
	 */
	public String getLongLifeToken() throws DAOException {

		// Build the URL
		StringBuilder buf = new StringBuilder(SystemData.get("users.facebook.url.accessToken"));
		buf.append("?grant_type=fb_exchange_token&client_id=");
		buf.append(SystemData.get("users.facebook.id"));
		buf.append("&client_secret=");
		buf.append(SystemData.get("users.facebook.secret"));
		buf.append("&fb_exchange_token=");
		buf.append(_token);

		try {
			init(buf.toString()); String token = null;
			try (BufferedReader br = new BufferedReader(new InputStreamReader(getIn()))) {
				String data = br.readLine();
				
				StringTokenizer tkns = new StringTokenizer(data, "&");
				while ((token == null) && tkns.hasMoreTokens()) {
					StringTokenizer tkn = new StringTokenizer(tkns.nextToken(), "=");
					String name = tkn.nextToken();
					if ("access_token".equals(name))
						token = tkn.nextToken();
				}
			}

			return token;
		} catch (Exception e) {
			if (_warnMode) {
				log.error(e.getClass().getName() + " - " + e.getMessage());
				return null;
			}
			
			throw new DAOException(e);
		}
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
			try (DataOutputStream out = new DataOutputStream(getOut())) {
				out.writeBytes(postData.getBody());
				out.flush();
			}
			
			// Get the response code
			int resultCode = getResponseCode();
			if (resultCode != HttpURLConnection.HTTP_OK)
				throw new DAOException("Error Code " + resultCode);
			
			// Parse the return value
			String rawToken = null;
			try (InputStream is = getIn()) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
					rawToken = br.readLine();
				}
			}
			
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