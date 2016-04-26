// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.DatabaseBean;
import org.deltava.security.UserPool;

/**
 * A JSP tag to filter content based upon whether a user is currently logged in.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ActiveUserFilterTag extends TagSupport {

	private int _userID;
	
	/**
	 * Sets the user ID to search for.
	 * @param userID the <i>database ID</i> of the user to check for.
	 * @see org.deltava.beans.Pilot#getID()
	 */
	public void setUser(int userID) {
		DatabaseBean.validateID(_userID, userID);
		_userID = userID;
	}
	
	/**
	 * Filters the content of this tag based on if the user is logged in.
	 * @return SKIP_BODY if user not found, otherwise EVAL_BODY_INCLUDE
	 * @see UserPool#contains(int)
	 */
	@Override
	public int doStartTag() throws JspException {
		int tmpResult = UserPool.contains(_userID) ? EVAL_BODY_INCLUDE : SKIP_BODY;
		release();
		return tmpResult;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_userID = 0;
	}
}