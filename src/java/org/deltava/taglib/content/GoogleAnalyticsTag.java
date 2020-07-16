// Copyright 2007, 2008, 2009, 2010, 2012, 2013, 2015, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to embed Google analytics data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GoogleAnalyticsTag extends TagSupport {
	
	private boolean _eventSupport;
	private boolean _asyncLoad = true;
	
	/**
	 * Sets if event support functionality should be enabled on this page.
	 * @param doSupport TRUE to enable event support, otherwise FALSE
	 */
	public void setEventSupport(boolean doSupport) {
		_eventSupport = doSupport;
	}
	
	/**
	 * Sets whether to load the API asynchronously.
	 * @param doAsync TRUE to load asynchronously, otherwise FALSE
	 */
	public void setAsync(boolean doAsync) {
		_asyncLoad = doAsync;
	}
	
	/*
	 * Helper method to generate synchronous loading snippet.
	 */
	private void writeSync(String accountID) throws Exception {
		JspWriter out = pageContext.getOut();
		
		// Write the script include tag
		out.println("<script src=\"https://ssl.google-analytics.com/ga.js\"></script>");
		
		// Write the analytics script
		out.println("<script>");
		out.println("try { ");
		out.print("golgotha.event.tracker = _gat._getTracker('");
		out.print(accountID);
		out.println("'); golgotha.event.tracker._trackPageview();");
		out.println("} catch(err) { }");
		
		// Write event tracker function
		if (_eventSupport) {
			out.println();
			out.println("golgotha.event.beacon = function(category, action, label, count) {");
			out.print("if (golgotha.event.tracker == null) return false; ");
			out.print("golgotha.event.tracker._trackEvent(category, action, label, count); ");
			out.print("return true; ");
			out.println("};");
		}
		
		out.println("</script>");
	}
	
	/*
	 * Helper method to generate asynchronous loading snippet.
	 */
	private void writeAsync(String accountID) throws Exception {
		JspWriter out = pageContext.getOut();
		out.println("<script async>");
		out.println("var _gaq = _gaq || [];");
		out.println("_gaq.push(['_setAccount', '" + accountID + "']);");
		out.println("_gaq.push(['_trackPageview']);");

		// Create DOM entry
		out.println("(function() { var ga = document.createElement('script');");
		out.println("ga.src = 'https://ssl.google-analytics.com/ga.js'; ga.setAttribute('async', 'true'); document.documentElement.firstChild.appendChild(ga); })();");
		out.println("</script>");
	}
	
	/**
	 * Inserts the JavaScript for Google Analytics into the JSP.
	 * @return TagSupport#EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		String accountID = SystemData.get("security.key.analytics");
		if (accountID == null)
			return EVAL_PAGE;
		
		try {
			if (!_asyncLoad || _eventSupport)
				writeSync(accountID);
			else
				writeAsync(accountID);
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
		_eventSupport = false;
		_asyncLoad = true;
		super.release();
	}
}