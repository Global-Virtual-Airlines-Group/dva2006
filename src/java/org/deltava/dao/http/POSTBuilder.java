// Copyright 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import org.deltava.util.StringUtils;

/**
 * A helper class to build POST bodies.
 * @author Luke
 * @version 7.2
 * @since 3.4
 */

class POSTBuilder extends LinkedHashMap<String, String> {
	
	/**
	 * Adds a value to the Map provided it is not null or empty.
	 * @param key the key
	 * @param value the value
	 */
	public void addIfPresent(String key, String value) {
		if (!StringUtils.isEmpty(value))
			put(key, value);
	}
	
	/**
	 * Builds a POST body of name/value pairs.
	 * @return the POST body
	 */
	public String getBody() {
		StringBuilder dataBuf = new StringBuilder();
		try {
			for (Iterator<Map.Entry<String, String>> i = entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, String> me = i.next();
				dataBuf.append(URLEncoder.encode(me.getKey(), "UTF-8"));
				dataBuf.append('=');
				dataBuf.append(URLEncoder.encode(me.getValue(), "UTF-8"));
				if (i.hasNext())
					dataBuf.append('&');
			}
		} catch (UnsupportedEncodingException ue) {
			// swallow
		}
		
		return dataBuf.toString();
	}
}