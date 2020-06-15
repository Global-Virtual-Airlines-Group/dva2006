// Copyright 2005, 2006, 2011, 2016, 2020 Global Virtual Airlnes Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.AbstractComment;

/**
 * A bean for storing development issue comments.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class IssueComment extends AbstractComment {
	
	/**
	 * Creates a new Issue Comment.
	 * @param id the database ID
	 * @param comments the comment text
	 * @throws NullPointerException if comments is null
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public IssueComment(int id, String comments) {
		super();
		if (id > 0) setID(id);
		setBody(comments);
	}
}