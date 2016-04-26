// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to track Senior Captain nomination comments.
 * @author Luke
 * @version 3.3
 * @since 3.3
 */

public class NominationComment extends DatabaseBean implements AuthoredBean, ViewEntry {
	
	private boolean _support = true;
	private Instant _created;
	private String _body;

	/**
	 * Creates a new bean.
	 * @param id the author's database ID
	 * @param body the comments
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public NominationComment(int id, String body) {
		super();
		setID(id);
		_body = body;
	}
	
	/**
	 * Returns the comment creation date.
	 * @return the date/time the comment was written
	 * @see NominationComment#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _created;
	}
	
	/**
	 * Returns the comment body.
	 * @return the body
	 * @see NominationComment#setBody(String)
	 */
	public String getBody() {
		return _body;
	}
	
	/**
	 * Returns whether this comment is in support of the nomination.
	 * @return TRUE if in support, otherwise FALSE
	 * @see NominationComment#setSupport(boolean)
	 */
	public boolean getSupport() {
		return _support;
	}

	@Override
	public int getAuthorID() {
		return getID();
	}

	@Override
	public void setAuthorID(int id) {
		setID(id);
	}
	
	/**
	 * Updates the comment body.
	 * @param body the body
	 * @see NominationComment#getBody()
	 */
	public void setBody(String body) {
		_body = body;
	}
	
	/**
	 * Updates the comment creation date.
	 * @param dt the date/time the comment was created
	 * @see NominationComment#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
		_created = dt;
	}
	
	/**
	 * Marks whether this comment is in support of the nomination.
	 * @param isSupport TRUE if in support, otherwise FALSE
	 * @see NominationComment#getSupport()
	 */
	public void setSupport(boolean isSupport) {
		_support = isSupport;
	}
	
	@Override
	public int compareTo(Object o2) {
		NominationComment nc2 = (NominationComment) o2;
		int tmpResult = _created.compareTo(nc2._created);
		return (tmpResult == 0) ? Integer.valueOf(getID()).compareTo(Integer.valueOf(nc2.getID())) : tmpResult;
	}
	
	@Override
	public String getRowClassName() {
		return _support ? null : "warn";
	}
}