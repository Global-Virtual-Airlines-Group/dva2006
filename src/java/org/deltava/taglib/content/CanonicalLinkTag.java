// Copyright 2015, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import javax.servlet.http.HttpServletRequest;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to generate canonical URLs for a page.
 * @author Luke
 * @version 8.2
 * @since 6.0
 */

public class CanonicalLinkTag extends TagSupport {

	private String _url;
	private boolean _convertHexID;
	
	/**
	 * Overrides the canonical URL for this page.
	 * @param url the URL
	 */
	public void setUrl(String url) {
		_url = url;
	}
	
	/**
	 * Sets whether to convert numeric ID parameters to hexadecimal.
	 * @param convertID TRUE if parameters should be converted, otherwise FALSE
	 */
	public void setConvertID(boolean convertID) {
		_convertHexID = convertID;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_url = null;
		_convertHexID = false;
	}

	/**
	 * Checks the input parameters and calculates the canonical URL if necessary.
	 * @return SKIP_BODY always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		if (!StringUtils.isEmpty(_url))
			return SKIP_BODY;
		if (!_convertHexID)
			throw new JspException("No URL or ID conversion specified!");
		
		String reqID = pageContext.getRequest().getParameter("id");
		if ((reqID == null) || (reqID.startsWith("0x"))) return SKIP_BODY;
		
		// Check if it's a number
		int numericID = StringUtils.parse(reqID, Integer.MIN_VALUE);
		if (numericID > Integer.MIN_VALUE) {
			HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
			String URL = (String) pageContext.findAttribute("javax.servlet.forward.request_uri");
			StringBuffer buf = (URL == null) ? req.getRequestURL() : new StringBuffer(URL); 
			String qs = req.getQueryString().replace(reqID, StringUtils.formatHex(numericID));
			_url = buf.append('?').append(qs).toString();
		}
		
		return SKIP_BODY;
	}

	/**
	 * Writes the canonical link tag to the JSP output stream.
	 * @return EVAL_BODY always
	 * @throws JspException if an error occurs 
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			if (_url != null) {
				JspWriter jw = pageContext.getOut();
				jw.print("<link rel=\"canonical\" href=\"");
				jw.print(_url);
				jw.println("\">");
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}