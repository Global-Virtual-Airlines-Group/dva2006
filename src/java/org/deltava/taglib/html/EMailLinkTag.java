// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Person;
import org.deltava.beans.EMailAddress;

/**
 * A JSP Tag to display a Pilot's e-mail address.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EMailLinkTag extends TagSupport {

	private Person _usr;
	
	private String _className;
	private String _label;
	
	/**
	 * Updates the Person to display.
	 * @param p the Person
	 */
	public void setUser(Person p) {
		_usr = p;
	}
	
	/**
	 * Updates the CSS class to format the label with.
	 * @param cName the CSS class name
	 */
	public void setClassName(String cName) {
		_className = cName;
	}
	
	/**
	 * Sets a label to display instead of the e-mail address.
	 * @param label the label
	 */
	public void setLabel(String label) {
		_label = label;
	}
	
	/**
	 * Resets the tag's state variables.
	 */
	public void release() {
		super.release();
		_className = null;
		_label = null;
	}
	
	/**
	 * Renders a link to the user's e-mail address to the JSP output stream
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		
		// If we can't display the address, abort
		if (EMailAddress.INVALID_ADDR.equals(_usr.getEmail()))
			return EVAL_PAGE;
		
		JspWriter out = pageContext.getOut();
		try {
			out.print("<a href=\"mailto:");
			out.print(_usr.getEmail());
			out.print("\">");
			
			// Add HTML style if present
			if (_className != null) {
				out.print("<span class=\"");
				out.print(_className);
				out.print("\">");
			}
			
			// Write the e-mail address or the label
			out.print((_label == null) ? _usr.getEmail() : _label);
			
			// Close the tags
			if (_className != null)
				out.print("</span>");
			
			out.print("</a>");
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		// Release state and return
		release();
		return EVAL_PAGE;
	}
}