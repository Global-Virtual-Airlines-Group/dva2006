// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert a JavaScript include file.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class InsertJSTag extends InsertContentTag {

	/**
	 * Renders the tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {

		// Check if the content has already been added
		if (ContentHelper.containsContent(pageContext, "JS", _resourceName)) {
		   release();
		   return EVAL_PAGE;
		}

		JspWriter out = pageContext.getOut();
		try {
			out.print("<script language=\"JavaScript\" type=\"text/javascript\" src=\"");
			if (!_resourceName.startsWith("http://")) {
				out.print(SystemData.get("path.js") + "/" + _resourceName + ".js");
			} else {
				out.print(_resourceName);
			}

			out.print("\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "JS", _resourceName);
		return EVAL_PAGE;
	}
}