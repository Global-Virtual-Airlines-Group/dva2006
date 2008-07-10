// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to embed Google analytics data.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class GoogleAnalyticsTag extends TagSupport {
	
	private String _jsVar;
	private String _virtualURL;
	private String _customVar;
	
	/**
	 * Sets the name of the JavaScript variable to use for the tracker object.
	 * @param varName the variable name
	 */
	public void setVar(String varName) {
		_jsVar = varName;
	}
	
	/**
	 * Sets the virtual URL to send the tracker, overriding the page URL.
	 * @param url the URL
	 */
	public void setUrl(String url) {
		_virtualURL = url;
	}
	
	/**
	 * Sets a custom user value.
	 * @param value the value
	 */
	public void setCustom(String value) {
		_customVar = value;
	}
	
	/**
	 * Inserts the JavaScript for Google Analytics into the JSP.
	 * @return TagSupport#EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doEndTag() throws JspException {
		String accountID = SystemData.get("security.key.analytics");
		if (accountID == null)
			return EVAL_PAGE;
		
		// Set the JS variable
		if (_jsVar == null)
			_jsVar = "tracker";
		
		try {
			JspWriter out = pageContext.getOut();
			
			// Write the script include tag
			out.print("<script src=\"");
			if (pageContext.getRequest().isSecure())
				out.print("https://ssl");
			else
				out.print("http://www");
			
			out.println(".google-analytics.com/ga.js\" type=\"text/javascript\"></script>");
			
			// Write the analytics script
			out.println("<script language=\"JavaScript\" type=\"text/javascript\">");
			out.print("var ");
			out.print(_jsVar);
			out.print(" = _gat._getTracker('");
			out.print(accountID);
			out.println("');");
			out.print(_jsVar);
			out.println("._initData();");
			out.print(_jsVar);
			out.print("._trackPageview(");
			if (_virtualURL != null) {
				out.print("'");
				out.print(_virtualURL);
				out.print("'");
			}
			
			out.println(");");
			
			// Add custom value
			if (_customVar != null) {
				out.print(_jsVar);
				out.print("._setVar('");
				out.print(_customVar);
				out.println("');");
			}
			
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
		_jsVar = null;
		_virtualURL = null;
		_customVar = null;
		super.release();
	}
}