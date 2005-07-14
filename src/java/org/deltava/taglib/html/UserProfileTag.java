// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.beans.system.UserData;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to display a User profile across applications.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserProfileTag extends ElementTag {

	public UserProfileTag() {
		super("a");
	}
	
	/**
	 * Sets the User Location for this user.
	 * @param ud the UserData bean
	 */
	public void setLocation(UserData ud) {
		
		// Determine the URL
		boolean isApplicant = "APPLICANTS".equals(ud.getTable());
		StringBuffer urlBuf = new StringBuffer("http://www.");
		urlBuf.append(ud.getDomain());
		urlBuf.append('/');
		urlBuf.append(isApplicant ? "applicant.do?id=" : "profile.do?id=");
		urlBuf.append(StringUtils.formatHex(ud.getID()));

		// Save the link URL
		_attrs.put("href", urlBuf.toString());
	}
	
    /**
     * Opens this link element by writing an &gt;A&lt; tag.
     * @throws JspException if an error occurs; 
     */
    public int doStartTag() throws JspException {
        try {
            _out.print(openHTML(true));
        } catch(Exception e) {
            throw wrap(e);
        }

        return EVAL_BODY_INCLUDE;
    }
    
    /**
     * Closes this link element by writing an &gt;/A&lt; tag.
     * @throws JspException if an I/O error occurs
     */
    public int doEndTag() throws JspException {
        try {
            _out.print(closeHTML());
        } catch(Exception e) {
            throw wrap(e);
        }
        
        return EVAL_PAGE;
    }
}