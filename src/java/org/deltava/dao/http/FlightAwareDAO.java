// Copyright 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.util.*;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * An abstract class to describe FlightAware RESTful Data Access Objects. 
 * @author Luke
 * @version 11.1
 * @since 8.0
 */

abstract class FlightAwareDAO extends DAO {
	
	private String _key;
	private int _maxResults;
	
	/**
	 * Sets the API Key to use.
	 * @param key the key
	 */
	public final void setKey(String key) {
		_key = key;
	}
	
	/**
	 * Sets the maximum number of results to retrieve.
	 * @param maxResults the maximum number of results
	 */
	public void setMaxResults(int maxResults) {
		_maxResults = Math.max(0, maxResults);
	}
	
	@Override
	protected void init(String url) throws IOException {
		setCompression(Compression.GZIP, Compression.BROTLI);
		super.init(url);
		setRequestHeader("x-apikey", _key);
	}
	
	/**
	 * Builds a FlightAware REST URL.
	 * @param method the API method name
	 * @param params a Map of parameters and values
	 * @return the URL to call
	 */
	protected String buildURL(String method, Map<String, String> params) {
		StringBuilder buf = new StringBuilder("https://aeroapi.flightaware.com/aeroapi/");
		buf.append(method);
		buf.append('?');
		if (_maxResults > 0)
			params.put("howMany", String.valueOf(_maxResults));
		
		for (Iterator<Map.Entry<String, String>> i = params.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String, String> me = i.next();
			buf.append(URLEncoder.encode(me.getKey(), ISO_8859_1));
			buf.append('=');
			buf.append(URLEncoder.encode(me.getValue(), ISO_8859_1));
			if (i.hasNext())
				buf.append('&');
		}
		
		return buf.toString();
	}
}