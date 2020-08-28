// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.security.Principal;

import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.CAPTCHAResult;
import org.deltava.commands.HTTPContext;
import org.deltava.taglib.ContentHelper;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert Google RECAPTCHA libraries. 
 * @author Luke
 * @version 9.1
 * @since 9.0
 */

public class GoogleCAPTCHATag extends TagSupport {
	
	private static final String JS_URL = "https://www.google.com/recaptcha/api.js?render=";
	
	private String _action;
	private boolean _authOnly;
	private boolean _force;
	
	/**
	 * Updates the action name.
	 * @param name the name
	 */
	public void setAction(String name) {
		_action = name;
	}
	
	/**
	 * Sets whether to display for authenticated users only.
	 * @param isAuthOnly TRUE for authenticated users only, otherwise FALSE
	 */
	public void setAuthOnly(boolean isAuthOnly) {
		_authOnly = isAuthOnly;
	}
	
	/**
	 * Sets whether to force a Google call even if the CAPTCHA was previously validated.
	 * @param doForce TRUE if a call is always made, otherwise FALSE
	 */
	public void setForce(boolean doForce) {
		_force = doForce;
	}

	/**
	 * Inserts the JavaScript for Google RECAPTCHA into the JSP.
	 * @return TagSupport#EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		String siteKey = SystemData.get("security.key.recaptcha.site");
		if (StringUtils.isEmpty(siteKey))
			throw new JspException("No RECAPTCHA Site Key defined");
		
		// Check if authenticated
		if (_authOnly) {
			HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
			Principal usr = req.getUserPrincipal();
			if (usr == null)
				return EVAL_BODY_INCLUDE;
		}
		
		// Check if forced
		if (!_force) {
			HttpSession s = pageContext.getSession();
			if (s != null) {
				CAPTCHAResult cr = (CAPTCHAResult) s.getAttribute(HTTPContext.CAPTCHA_ATTR_NAME);
				if ((cr != null) && cr.getIsSuccess())
					return EVAL_BODY_INCLUDE;
			}
		}
		
		// Build URL with site key
		StringBuilder urlBuf = new StringBuilder(JS_URL);
		urlBuf.append(siteKey);
		ContentHelper.pushContent(pageContext, urlBuf.toString(), "script");
		
		try {
			JspWriter out = pageContext.getOut();
			
			// Write the script tag
			out.print("<script src=\"");
			out.print(urlBuf.toString());
			out.println("\"></script>");
			
			// Write the action
			out.println("<script>grecaptcha.ready(function() {");
			out.print("grecaptcha.execute('");
			out.print(siteKey);
			out.print("',{action:'");
			out.print(_action);
			out.print("'}).then(golgotha.util.validateCAPTCHA);");
			out.println(" });</script>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
	
	@Override
	public void release() {
		_authOnly = false;
		_force = false;
		super.release();
	}
}