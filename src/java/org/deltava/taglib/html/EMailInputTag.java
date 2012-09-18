// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.beans.system.HTTPContextData;

/**
 * An HTML 5 JSP tag for e-mail input elements. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class EMailInputTag extends InputTag {

	/**
	 * Creates an e-mail tag if executing in an HTML5 browser.
	 * @return SKIP_BODY always
	 * @throws JspException if an error occurs 
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();

		// Check for HTML5 browser
		HTTPContextData ctxt = getBrowserContext();
		if (ctxt.isHTML5())
			_data.setAttribute("type", "email");
		
		return SKIP_BODY;
	}
}