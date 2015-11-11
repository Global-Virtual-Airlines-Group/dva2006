// Copyright 2005, 2009, 2010, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert a JavaScript include file.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class InsertJSTag extends InsertMinifiedContentTag {

	private boolean _async;

	/**
	 * Sets whether the script should be loaded asynchronously.
	 * @param isAsync TRUE if loaded asynchronously, otherwise FALSE
	 */
	public void setAsync(boolean isAsync) {
		_async = isAsync;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_async = false;
	}
	
	/**
	 * Renders the tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {

		// Check if the content has already been added
		if (ContentHelper.containsContent(pageContext, "JS", _resourceName)) {
		   release();
		   return EVAL_PAGE;
		}

		try {
			JspWriter out = pageContext.getOut();
			out.print("<script src=\"");
			if (!_resourceName.startsWith("http://")) {
				out.print(SystemData.get("path.js"));
				out.print('/');
				out.print(getFileName());
				out.print(".js");
			}
			else
				out.print(_resourceName);

			if (_async)
				out.print(" async");
			
			out.print("\"></script>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		ContentHelper.addContent(pageContext, "JS", _resourceName);
		return EVAL_PAGE;
	}
}