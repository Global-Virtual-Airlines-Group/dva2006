package org.deltava.taglib.content;

import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert a JavaScript include file.
 * @author Luke
 * @version 1.0
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
		if (containsContent("JS", _resourceName))
			return EVAL_PAGE;

		// Mark the content as added
		addContent("JS", _resourceName);

		// Calculate the resource name, if it's on the local machine
		if (!_resourceName.startsWith("http://"))
			_resourceName = SystemData.get("path.js") + "/" + _resourceName + ".js";

		JspWriter out = pageContext.getOut();
		try {
			out.print("<script language=\"JavaScript\" type=\"text/javascript\" src=\"");
			out.print(_resourceName);
			out.print("\"></script>");
		} catch (IOException ie) {
			throw wrap(ie);
		}

		return EVAL_PAGE;
	}
}