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

public class BrowserReport extends DatabaseBean implements AuthoredBean {
	
	private final String _type;
	private Instant _createdOn;
	private int _authorID;
	private String _url;
	private String _body;

	/**
	 * Creates the bean.
	 * @param type the report type
	 */
	public BrowserReport(String type) {
		super();
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
	 * Returns the creation date of this Report.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}

	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the URL of the report violation.
	 * @return the URL
	 */
	public String getURL() {
		return _url;
	}
	
	/**
	 * Returns the report body.
	 * @return the body JSON text
	 */
	public String getBody() {
		return _body;
	}

	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id, true);
		_authorID = id;
	}

	/**
	 * Updates the creation date of this Report.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}

	/**
	 * Updates the URL of the report violation.
	 * @param url the URL
	 */
	public void setURL(String url) {
		_url = url;
	}
	
	/**
	 * Updates the report body.
	 * @param body the body JSON text
	 */
	public void setBody(String body) {
		_body = body;
	}
}