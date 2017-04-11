// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;

import org.deltava.beans.system.*;

import org.deltava.taglib.BrowserInfoTag;
import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to display favicon link elements.
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public class FaviconTag extends BrowserInfoTag {
	
	/*
	 * Helper method to append build number into path.
	 */
	private static String generateTag(String base, String path) {
		StringBuilder buf = new StringBuilder("<");
		buf.append(base);
		buf.append(" href=\"/favicon/v");
		buf.append(VersionInfo.BUILD);
		buf.append('/');
		buf.append(path);
		buf.append("\">");
		return buf.toString();
	}

	/**
	 * Renders the favicon elements.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		
		// Check for duplicate content
		if (ContentHelper.containsContent(pageContext, "IMG", "favicon"))
			throw new JspException("Favicon already on page");

		try {
			HTTPContextData bCtxt = getBrowserContext();
			JspWriter out = pageContext.getOut();
			if (bCtxt.getBrowserType() == BrowserType.WEBKIT) {
				out.println(generateTag("link rel=\"apple-touch-icon\" sizes=\"180x180\"", "apple-touch-icon.png"));
				out.println(generateTag("link rel=\"mask-icon\" color=\"#5bbad5\"", "safari-pinned-tab.svg"));
			}
			
			out.println(generateTag("link rel=\"icon\" type=\"image/png\" sizes=\"32x32\"", "favicon-32x32.png"));
			out.println(generateTag("link rel=\"icon\" type=\"image/png\" sizes=\"16x16\"", "favicon-16x16.png"));
			out.println(generateTag("link rel=\"shortcut icon\"", "favicon.ico"));
			out.println(generateTag("link rel=\"manifest\"", "manifest.json"));
			if ((bCtxt.getBrowserType() == BrowserType.IE) && (bCtxt.getMajor() > 9)) {
				out.print("<meta name=\"msapplication-config\" content=\"/favicon/v");
				out.print(VersionInfo.BUILD);
				out.println("/browserconfig.xml\">");
				out.println("<meta name=\"theme-color\" content=\"#ffffff\">");
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		ContentHelper.addContent(pageContext, "IMG", "favicon");
		return EVAL_PAGE;
	}
}