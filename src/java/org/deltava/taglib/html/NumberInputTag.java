// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

/**
 * An HTML 5 JSP tag for numeric input elements. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

abstract class NumberInputTag extends HTML5InputTag {

	/**
	 * Creates a number input field if executing in an HTML5 browser.
	 * @return SKIP_BODY always
	 * @throws JspException if an error occurs 
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		
		// Check for HTML5 browser
		if (!isHTML5()) {
			if (_data.has("max"))
				setNumericAttr("maxlength", _data.get("max").length(), 1);
			
			removeHTML5Attributes();
		} else
			_data.setAttribute("type", "number");
		
		return SKIP_BODY;
	}
}