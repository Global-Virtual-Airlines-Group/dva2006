// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store Content Security Policy violation data. 
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class CSPViolations implements Comparable<CSPViolations> {
	
	private final Instant _date;
	private final String _type;
	private String _host;
	private int _count;
	
	private final Collection<String> _urls = new HashSet<String>();

	/**
	 * Creates the bean.
	 * @param dt the date
	 * @param type policy violation type
	 */
	public CSPViolations(Instant dt, String type) {
		super();
		_date = dt;
		_type = type;
	}

	/**
	 * Returns the violation date.
	 * @return the date
	 */
	public Instant getDate() {
		return _date;
	}
	
	/**
	 * Returns the CSP directive violated.
	 * @return the directive name
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * Returns the number of violations
	 * @return the violation count
	 */
	public int getCount() {
		return _count;
	}
	
	/**
	 * Returns the offending host name.
	 * @return the host name
	 */
	public String getHost() {
		return _host;
	}
	
	/**
	 * Returns the offending site URLs
	 * @return the URLs
	 */
	public Collection<String> getURLs() {
		return _urls;
	}
	
	/**
	 * Adds a site URL. THe protocol and host names will be stripped if present.
	 * @param url the URL
	 */
	public void addURL(String url) {
		if (url == null) return;
		int pos = url.indexOf('/', url.indexOf("//") + 2);
		if (pos > -1)
			_urls.add(url.substring(pos + 1));
	}

	/**
	 * Updates the offending host name.
	 * @param host the host name
	 */
	public void setHost(String host) {
		_host = host;
	}
	
	/**
	 * Updates the violation count.
	 * @param cnt the violation count
	 */
	public void setCount(int cnt) {
		_count = cnt;
	}

	@Override
	public int compareTo(CSPViolations cv2) {
		int tmpResult = _date.compareTo(cv2._date);
		if (tmpResult == 0)
			tmpResult = _type.compareTo(cv2._type);
		if (tmpResult == 0)
			tmpResult = Integer.compare(_count, cv2._count);

		return (tmpResult == 0) ? _host.compareTo(cv2._host) : tmpResult;
	}
}