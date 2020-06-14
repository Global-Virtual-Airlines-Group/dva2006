// Copyright 2006, 2010, 2012, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.help;

import org.deltava.beans.AbstractComment;

/**
 * A bean to store Help Desk Issue comments.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class IssueComment extends AbstractComment {
	
	private boolean _faq;

	/**
	 * Creates a new Comment bean.
	 * @param authorID the database ID of the author
	 * @throws IllegalArgumentException if authorID is zero or negative
	 * @see IssueComment#getAuthorID()
	 */
	public IssueComment(int authorID) {
		super();
		setAuthorID(authorID);
	}

	/**
	 * Returns whether this Comment is an FAQ answer.
	 * @return TRUE if the Comment is an FAQ answer, otherwise FALSE
	 * @see IssueComment#setFAQ(boolean)
	 */
	public boolean getFAQ() {
		return _faq;
	}
	
	/**
	 * Updates whether this Comment is an FAQ answer.
	 * @param isFAQ TRUE if the Comment is an FAQ answer, otherwise FALSE
	 * @see IssueComment#getFAQ()
	 */
	public void setFAQ(boolean isFAQ) {
		_faq = isFAQ;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof IssueComment) ? (compareTo(o) == 0) : false;
	}
}