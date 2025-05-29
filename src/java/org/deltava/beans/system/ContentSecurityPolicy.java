// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

/**
 * A bean to store dynamic Content Security Policy data. 
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class ContentSecurityPolicy {

	private final boolean _enforce;
	private final Map<ContentSecurity, Collection<String>> _data = new LinkedHashMap<ContentSecurity, Collection<String>>();

	/**
	 * Creates the bean and populates default values.
	 * @param enforce TRUE if the CSP is enforced, otherwise FALSE for warn-only mode
	 */
	public ContentSecurityPolicy(boolean enforce) {
		super();
		_enforce = enforce;
		add(ContentSecurity.DEFAULT, "'self'");
		add(ContentSecurity.CONNECT, "'self'");
		add(ContentSecurity.SCRIPT, "'self'");
		add(ContentSecurity.SCRIPT, "'unsafe-inline'");
		add(ContentSecurity.STYLE, "'self'");
		add(ContentSecurity.STYLE, "'unsafe-inline'");
		add(ContentSecurity.IMG, "'self'");
		add(ContentSecurity.WORKER, "'self'");
	}
	
	/**
	 * Adds an entry to this Security policy.
	 * @param cs a ContentSecurity type
	 * @param host a permitted host
	 */
	public void add(ContentSecurity cs, String host) {
		Collection<String> hosts = _data.getOrDefault(cs, new LinkedHashSet<String>());
		hosts.add(host);
		_data.put(cs, hosts);
	}
	
	/**
	 * Returns the name of the CSP response header, which varies depending on enfrocement mode.
	 * @return the header name
	 */
	public String getHeader() {
		return _enforce ? "Content-Security-Policy" : "Content-Security-Policy-Report-Only";
	}
	
	/**
	 * Generates the value to place into the security policy HTTP header.
	 * @return a Header value
	 */
	public String getData() {
		
		StringBuilder buf = new StringBuilder(64);
		for (Map.Entry<ContentSecurity, Collection<String>> me : _data.entrySet()) {
			buf.append(me.getKey().name().toLowerCase());
			buf.append("-src");
			for (String host : me.getValue()) {
				buf.append(' ');
				buf.append(host);
			}
			
			buf.append("; ");
		}
		
		buf.append("frame-ancestors none");
		return buf.toString();
	}
}