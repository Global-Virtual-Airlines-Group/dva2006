// Copyright 2012, 2017, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.deltava.util.StringUtils;

/**
 * A helper class to build POST bodies.
 * @author Luke
 * @version 9.2
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
			put(key, String.valueOf(value));
	}
	
	/**
	 * Builds a POST body of name/value pairs.
	 * @param c the Charset to encode with
	 * @return the POST body
	 */
	public byte[] getBody(Charset c) {
		StringBuilder dataBuf = new StringBuilder();
		for (Iterator<Map.Entry<String, String>> i = entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String, String> me = i.next();
			dataBuf.append(URLEncoder.encode(me.getKey(), c));
			dataBuf.append('=');
			dataBuf.append(URLEncoder.encode(me.getValue(), c));
			if (i.hasNext())
				dataBuf.append('&');
		}
		
		return dataBuf.toString().getBytes(c);
	}
}