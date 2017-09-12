// Copyright 2005, 2006, 2007, 2010, 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.VersionInfo;

import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert a copyright notice.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class CopyrightTag extends TagSupport {

	private boolean _visible = true;

	/**
	 * Marks the copyright tag as visible instead of embedded in the HTML code.
	 * @param isVisible TRUE if the tag should be visible, otherwise FALSE
	 */
	public void setVisible(boolean isVisible) {
		_visible = isVisible;
	}

	/**
	 * Renders the copyright tag to the JSP output stream.
	 * @return TagSupport#EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter jw = pageContext.getOut();
			if (_visible) {
				jw.println("<hr />");
				jw.print("<div class=\"small copyright\">");
				jw.print(pageContext.getServletContext().getServletContextName());
				jw.print(' ');
				jw.print(VersionInfo.APPNAME );
				jw.print(' ');
				jw.print(VersionInfo.HTML_COPYRIGHT);
				jw.print(" (Build ");
				jw.print(VersionInfo.BUILD);
				jw.print(")</div>");

				// Display disclaimer
				String disclaimer = SystemData.get("airline.copyright");
				if (disclaimer != null) {
					jw.print("\n<span class=\"copyright small\">");
					jw.print(disclaimer);
					jw.print("</span>");
				}
			} else {
				jw.print("<!-- ");
				jw.print(pageContext.getServletContext().getServletContextName());
				jw.print(' ');
				jw.print(VersionInfo.APPNAME);
				jw.print(' ');
				jw.print(VersionInfo.TXT_COPYRIGHT);
				jw.print(" (Build ");
				jw.print(String.valueOf(VersionInfo.BUILD));
				jw.print(") -->");
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_visible = true;
	}
}