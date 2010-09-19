// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean to track Senior Captain nomination comments.
 * @author Luke
 * @version 3.3
 * @since 3.3
 */

public class NominationComment extends DatabaseBean implements AuthoredBean {
	
	private Date _created;
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
	 * @see NominationComment#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
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
	public void setCreatedOn(Date dt) {
		_created = dt;
	}
	
	public int compareTo(Object o2) {
		NominationComment nc2 = (NominationComment) o2;
		int tmpResult = _created.compareTo(nc2._created);
		return (tmpResult == 0) ? Integer.valueOf(getID()).compareTo(Integer.valueOf(nc2.getID())) : tmpResult;
	}
}