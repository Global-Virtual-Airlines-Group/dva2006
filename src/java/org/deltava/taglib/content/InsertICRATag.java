// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to insert an ICRA rating file link.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InsertICRATag extends InsertContentTag {

	public int doEndTag() throws JspException {
		
		// Check if the content has already been added
		if (ContentHelper.containsContent(pageContext, "ICRA", _resourceName) && (!_forceInclude))
			return EVAL_PAGE;
		
		JspWriter out = pageContext.getOut();
		try {
			out.print("<link rel=\"meta\" title=\"ICRA labels\" type=\"application/rdf+xml\" href=\"");
			out.print(_resourceName);
			out.print(".rdf\" />");
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "ICRA", _resourceName);
		return EVAL_PAGE;
	}
}