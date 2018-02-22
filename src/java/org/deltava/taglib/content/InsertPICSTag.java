// Copyright 2005, 2012, 2015, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert an PICS-1.1 content rating.
 * @author Luke
 * @version 8.2
 * @since 1.0
 */

public class InsertPICSTag extends TagSupport {

	private boolean _doICRA;
	private boolean _doSafeSurf;

	/**
	 * Initializes the JSP Tag.
	 */
	public InsertPICSTag() {
		super();
		setIcra(true);
		setSafeSurf(true);
	}

	/**
	 * Controls whether an ICRA content rating tag should be displayed.
	 * @param doICRA TRUE if the rating tag should be shown, otherwise FALSE
	 */
	public void setIcra(boolean doICRA) {
		_doICRA = doICRA && (SystemData.get("content.icra") != null);
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
		setIcra(true);
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
		String url = (pageContext.getRequest().isSecure() ? "https" : "http") + "://" + pageContext.getRequest().getServerName();
		try {
			JspWriter out = pageContext.getOut();
			if (_doICRA) {
				out.print("<meta http-equiv=\"PICS-Label\" content=\'(PICS-1.1 \"http://www.icra.org/ratingsv02.html\" l gen true for \"");
				out.print(url);
				out.print("\" r (");
				out.print(SystemData.get("content.icra"));
				out.print("))\'>");
				if (_doSafeSurf)
					out.println();
			}

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