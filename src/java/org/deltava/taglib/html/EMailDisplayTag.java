package org.deltava.taglib.html;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Person;

/**
 * A JSP Tag to display a Pilot's e-mail address.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EMailDisplayTag extends TagSupport {

	private Person _usr;
	
	private String _className;
	private String _label;
	
	public void setUser(Person p) {
		_usr = p;
	}
	
	public void setClassName(String cName) {
		_className = cName;
	}
	
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
	
	public int doEndTag() throws JspException {
		
		// Get the HTTP servlet request
		HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
		
		// Check if we can display the pilot e-mail address
		boolean canDisplay = false;
		switch (_usr.getEmailAccess()) {
			case Person.SHOW_EMAIL :
				canDisplay = true;
				break;
				
			case Person.AUTH_EMAIL :
				canDisplay = (req.getUserPrincipal() != null);
				break;
				
			case Person.HIDE_EMAIL :
			default :
				canDisplay = req.isUserInRole("Moderator") || req.isUserInRole("HR") || req.isUserInRole("PIREP");
				break;
		}
		
		// If we can't display the address, abort
		if (!canDisplay)
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