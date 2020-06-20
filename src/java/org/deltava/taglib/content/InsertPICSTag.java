// Copyright 2005, 2012, 2015, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert an PICS-1.1 content rating.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class InsertPICSTag extends TagSupport {

	private boolean _doSafeSurf;

	/**
	 * Initializes the JSP Tag.
	 */
	public InsertPICSTag() {
		super();
		setSafeSurf(true);
	}

	/**
	 * Controls whether a SafeSurf content rating tag should be displayed.
	 * @param doSafeSurf TRUE if the rating tag should be shown, otherwise FALSE
	 */
	public void setSafeSurf(boolean doSafeSurf) {
		_doSafeSurf = doSafeSurf && (SystemData.get("content.safesurf") != null);
	}
	
	/**
	 * Releases the Tag's state data.
	 */
	@Override
	public void release() {
		super.release();
		setSafeSurf(true);
	}

	/**
	 * Renders the PICS-1.1 content to the JSP output stream. No content will be written if no rating data is found or selected.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {

		// Check if the content has already been added
		if (ContentHelper.containsContent(pageContext, "PICS", "PICS"))
			return EVAL_PAGE;

		// Build the site URL
		String url = "https://" + pageContext.getRequest().getServerName();
		try {
			JspWriter out = pageContext.getOut();
			if (_doSafeSurf) {
				out.print("<meta http-equiv=\"PICS-Label\" content=\'(PICS-1.1 \"http://www.classify.org/safesurf/\" L gen true for \"");
				out.print(url);
				out.print("\" r (");
				out.print(SystemData.get("content.safesurf"));
				out.println("))\'>");
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();	
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "PICS", "PICS");
		return EVAL_PAGE;
	}
}