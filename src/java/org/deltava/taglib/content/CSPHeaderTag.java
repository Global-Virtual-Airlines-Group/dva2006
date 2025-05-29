// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to write a Content Security Policy header to the response. While the header is usually written at the bottom of the 
 * {@link org.deltava.taglib.layout.PageTag#doEndTag()} method, this tag exists for larger pages where the output buffer has already
 * been flushed by the time this occurs.   
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class CSPHeaderTag extends TagSupport {

	@Override
	public int doEndTag() {
		ContentHelper.flushCSP(pageContext);
		return EVAL_PAGE;
	}
}