// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts3;

import java.util.*;

/**
 * A bean to store TeamSpeak 3 Server Query server responses. This can contain
 * an error code and message, or data if the code is zero (ok). 
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public class ServerQueryResponse {
	
	private final int _code;
	private final String _msg;
	
	private final Map<String, String> _data = new LinkedHashMap<String, String>();

	/**
	 * Creates the bean.
	 * @param code the error code
	 * @param msg the error message
	 */
	public ServerQueryResponse(int code, String msg) {
		super();
		_code = code;
		_msg = msg;
	}
	
	/**
	 * Returns the error code.
	 * @return the code
	 */
	public int getCode() {
		return _code;
	}
	
	/**
	 * Returns the error message.
	 * @return the message
	 */
	public String getMessage() {
		return _msg;
	}
	
	/**
	 * Returns a piece of data associated with the response.
	 * @param key the key 
	 * @return the value
	 */
	public String get(String key) {
		return _data.get(key);
	}
	
	public boolean isError() {
		return (_code != 0);
	}
	
	/**
	 * Copies response data to another Map.
	 * @param m the destination Map
	 */
	public void drainTo(Map<String, String> m) {
		m.putAll(_data);
	}
	
	/**
	 * Adds data to the response.
	 * @param data a Map of key/value pairs
	 */
	public void putAll(Map<String, String> data) {
		_data.putAll(data);
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(String.valueOf(_code));
		buf.append('-');
		buf.append(_msg);
		if (_data.isEmpty()) {
			buf.append('#');
			buf.append(_data.toString());
		}
		
		return buf.toString();
	}
}