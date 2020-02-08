// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert Google RECAPTCHA libraries. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class GoogleCAPTCHATag extends TagSupport {
	
	private String _action;
	
	/**
	 * Updates the action name.
	 * @param name the name
	 */
	public void setAction(String name) {
		_action = name;
	}

	/**
	 * Inserts the JavaScript for Google RECAPTCHA into the JSP.
	 * @return TagSupport#EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		String siteKey = SystemData.get("security.key.recaptcha.site");
		if (StringUtils.isEmpty(siteKey))
			throw new JspException("No RECAPTCHA Site Key defined");
		
		try {
			JspWriter out = pageContext.getOut();
			
			// Write the script tag
			out.print("<script src=\"https://www.google.com/recaptcha/api.js?render=");
			out.print(siteKey);
			out.println("\"></script>");
			
			// Write the action
			out.println("<script>grecaptcha.ready(function() {");
			out.print("grecaptcha.execute('");
			out.print(siteKey);
			out.print("',{action:'");
			out.print(_action);
			out.print("'}).then(golgotha.util.validateCAPTCHA);");
			out.println(" });</script>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}