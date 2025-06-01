// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store Reporting API payloads. 
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class BrowserReport extends DatabaseBean implements RemoteAddressBean {

	private final int _build;
	private final String _type;
	private Instant _createdOn;
	private String _url;
	private String _body;
	
	private String _host;
	private String _directive;
	
	private String _remoteHost;
	private String _remoteAddr;
	
	/**
	 * Creates the bean.
	 * @param build the build number
	 * @param type the report type
	 */
	public BrowserReport(int build, String type) {
		super();
		_build = build;
		_type = type;
	}

	/**
	 * Returns the report type.
	 * @return the type
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * Returns the build numebr.
	 * @return the build
	 */
	public int getBuild() {
		return _build;
	}
	
	/**
	 * Returns the creation date of this Report.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the triggered CSP directive.
	 * @return the directive name
	 */
	public String getDirective() {
		return _directive;
	}

	/**
	 * Returns the URL of the report violation.
	 * @return the URL
	 */
	public String getURL() {
		return _url;
	}
	
	/**
	 * Returns the blocked host name.
	 * @return the host name
	 */
	public String getHost() {
		return _host;
	}
	
	/**
	 * Returns the report body.
	 * @return the body JSON text
	 */
	public String getBody() {
		return _body;
	}

	@Override
	public String getRemoteAddr() {
		return _remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return _remoteHost;
	}

	/**
	 * Updates the creation date of this Report.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the blocked host name.
	 * @param host the host name
	 */
	public void setHost(String host) {
		_host = host;
	}

	/**
	 * Updates the triggered CSP directive.
	 * @param d the directive name
	 */
	public void setDirective(String d) {
		_directive = d;
	}

	/**
	 * Updates the URL of the report violation.
	 * @param url the URL
	 */
	public void setURL(String url) {
		int pos = url.indexOf('?');
		_url = (pos > 0) ? url.substring(0, pos) : url;
	}
	
	/**
	 * Updates the report body.
	 * @param body the body JSON text
	 */
	public void setBody(String body) {
		_body = body;
	}
	
	/**
	 * Updates the remote address that sumbitted this report.
	 * @param addr the remote IP address
	 * @param host the remost host name
	 */
	public void setRemoteAddress(String addr, String host) {
		_remoteAddr = addr;
		_remoteHost = host;
	}
}