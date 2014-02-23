// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspException;

import org.deltava.beans.system.HTTPContextData;

/**
 * A JSP tag to insert a add-on JSON formatting via a JavaScript include file.
 * @author Luke
 * @version 5.3
 * @since 5.3
 */

public class InsertJSONTag extends InsertJSTag {
	
	private static final String JS_FILENAME = "json2";

	/**
	 * Creates the tag.
	 */
	public InsertJSONTag() {
		setName(JS_FILENAME);
	}
	
	/**
	 * Renders the tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		HTTPContextData ctxt = getBrowserContext();
		return (ctxt.hasJSON()) ? EVAL_PAGE : super.doEndTag();
	}
}