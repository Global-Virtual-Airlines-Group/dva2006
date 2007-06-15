// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to embed Google analytics data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GoogleAnalyticsTag extends TagSupport {
	
	private String _accountID;

	/**
	 * Inserts the JavaScript for Google Analytics into the JSP.
	 * @return TagSupport#EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doEndTag() throws JspException {
		_accountID = SystemData.get("security.key.analytics");
		if (_accountID == null)
			return EVAL_PAGE;
		
		try {
			JspWriter out = pageContext.getOut();
			
			// Write the script include tag
			out.println("<script src=\"http://www.google-analytics.com/urchin.js\" type=\"text/javascript\"></script>");
			
			// Write the analytics script
			out.println("<script language=\"JavaScript\" type=\"text/javascript\">");
			out.print("_uacct = '");
			out.print(_accountID);
			out.println("';");
			out.println("urchinTracker();");
			out.println("</script>");
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
	public void release() {
		_accountID = null;
		super.release();
	}
}