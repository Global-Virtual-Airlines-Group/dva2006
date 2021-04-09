// Copyright 2014, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

/**
 * A JSP tag to insert a add-on JSON formatting via a JavaScript include file.
 * @author Luke
 * @version 10.0
 * @since 5.3
 */

public class InsertJSONTag extends InsertJSTag {
	
	@Override
	public int doStartTag() {
		return SKIP_BODY;
	}
	
	/**
	 * Renders the tag.
	 * @return TagSupport.EVAL_PAGE
	 */
	@Override
	public int doEndTag() {
		release();
		return EVAL_PAGE;
	}
}