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
	
	/**
	 * Self-referential constant.
	 */
	public static final String SELF = "'self'";

	private final boolean _enforce;
	private final Map<ContentSecurity, Collection<String>> _data = new LinkedHashMap<ContentSecurity, Collection<String>>();
	
	private String _reportGroup;
	private String _reportURI;

	/**
	 * Creates the bean and populates default values.
	 * @param enforce TRUE if the CSP is enforced, otherwise FALSE for warn-only mode
	 */
	public ContentSecurityPolicy(boolean enforce) {
		super();
		_enforce = enforce;
		add(ContentSecurity.DEFAULT, SELF);
		add(ContentSecurity.CONNECT, SELF);
		add(ContentSecurity.SCRIPT, SELF);
		add(ContentSecurity.SCRIPT, "'unsafe-inline'");
		add(ContentSecurity.STYLE, SELF);
		add(ContentSecurity.STYLE, "'unsafe-inline'");
		add(ContentSecurity.IMG, SELF);
		add(ContentSecurity.WORKER, SELF);
	}
	
	/**
	 * Adds an entry to this Security Policy.
	 * @param cs a ContentSecurity type
	 * @param host a permitted host
	 */
	public void add(ContentSecurity cs, String host) {
		Collection<String> hosts = _data.getOrDefault(cs, new LinkedHashSet<String>());
		hosts.add(host);
		_data.put(cs, hosts);
	}
	
	/**
	 * Updates a Reporting API endpoint for this Security Policy.
	 * @param group the group name
	 * @param url the endpoint URL
	 */
	public void setReportURI(String group, String url) {
		_reportGroup = group;
		_reportURI = url;
	}
	
	/**
	 * Returns whether a Reporting API endpoint has been defined for this Security Policy.
	 * @return TRUE if a URI has been defined, otherwise FALSE
	 */
	public boolean hasReportURI() {
		return (_reportGroup != null);
	}
	
	/**
	 * Returns the name of the CSP response header, which varies depending on enfrocement mode.
	 * @return the header name
	 */
	public String getHeader() {
		return _enforce ? "Content-Security-Policy" : "Content-Security-Policy-Report-Only";
	}
	
	/**
	 * Generates the value to place into Reporting API header.
	 * @return the Header value
	 */
	public String getReportHeader() {
		StringBuilder buf = new StringBuilder();
		if (_reportGroup != null) {
			buf.append(_reportGroup);
			buf.append("=\"");
			buf.append(_reportURI);
			buf.append('\"');
		}
		
		return buf.toString();
	}
	
	/**
	 * Generates the value to place into the Security Plicy HTTP header.
	 * @return the Header value
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
		
		if (_reportURI != null) {
			buf.append("report-to ");
			buf.append(_reportGroup);
			buf.append("; ");
		}
		
		buf.append("frame-ancestors none");
		return buf.toString();
	}
}