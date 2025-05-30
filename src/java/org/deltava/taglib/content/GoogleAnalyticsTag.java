// Copyright 2007, 2008, 2009, 2010, 2012, 2013, 2015, 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.ContentSecurity;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to embed Google Analytics v4 tags.
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class GoogleAnalyticsTag extends TagSupport {
	
	private static final String JS_URL = "https://www.googletagmanager.com/gtag/js";
	
	@Override
	public int doEndTag() throws JspException {
		String accountID = SystemData.get("security.key.analytics");
		if (accountID == null)
			return EVAL_PAGE;
		
		try {
			JspWriter out = pageContext.getOut();
			out.print("<script async src=\"");
			out.print(JS_URL);
			out.print("?id=");
			out.print(accountID);
			out.println("\"></script>");
			
			out.println("<script>");
			out.println("window.dataLayer = window.dataLayer || [];");
			out.println("function gtag(){ dataLayer.push(arguments); };");
			out.println("gtag('js', new Date());");
			out.print("gtag('config', '");
			out.print(accountID);
			out.println("');");
			out.println("</script>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		ContentHelper.pushContent(pageContext, JS_URL, "script");
		ContentHelper.addCSP(pageContext, ContentSecurity.SCRIPT, "www.googletagmanager.com");
		ContentHelper.addCSP(pageContext, ContentSecurity.CONNECT, "www.google-analytics.com");
		return EVAL_PAGE;
	}
}