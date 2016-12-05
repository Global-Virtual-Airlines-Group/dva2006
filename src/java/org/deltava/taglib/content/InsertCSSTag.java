// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.net.*;
import java.security.Principal;

import javax.servlet.jsp.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.Person;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert a Cascading Style Sheet.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class InsertCSSTag extends InsertMinifiedContentTag {

	/**
	 * Default CSS scheme name.
	 */
	static final String DEFAULT_SCHEME = "legacy";
	
	private String _host;
	private String _scheme;

	/**
	 * Specifies that the Style Sheet is located on a different web server.
	 * @param hostName the host name
	 */
	public void setHost(String hostName) {
		_host = hostName;
	}

	/**
	 * Sets the CSS scheme to display.
	 * @param name the scheme name
	 * @see InsertCSSTag#getScheme()
	 */
	public void setScheme(String name) {
		_scheme = (name == null) ? null : name.toLowerCase().replace(' ', '_');
	}
	
	/**
	 * Gets the scheme in use, or DEFAULT_SCHEME if none specified
	 * @return the scheme name
	 * @see InsertCSSTag#setScheme(String)
	 */
	protected String getScheme() {
		return (_scheme == null) ? DEFAULT_SCHEME : _scheme;
	}

	/**
	 * Loads the UI scheme name from the user object, if present.
	 * @param ctxt the JSP page context
	 */
	@Override
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
		Principal user = req.getUserPrincipal();
		if (user instanceof Person) {
			Person p = (Person) user;
			setScheme(p.getUIScheme());
		}
	}

	/**
	 * Renders the tag.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		
		// Build the path to the CSS file
		StringBuilder buf = new StringBuilder("/");
		buf.append(SystemData.get("path.css"));
		buf.append('/');
		buf.append(getScheme());
		buf.append('/');
		buf.append(getFileName());
		buf.append(".css");
		
		try {
			// Build the resource name if host specified
			if (_host != null) {
				URL url = new URL(pageContext.getRequest().isSecure() ? "https" : "http", _host, buf.toString());
				buf = new StringBuilder(url.toString());
			}
			
			// Check if the content has already been added
			if (ContentHelper.containsContent(pageContext, "CSS", buf.toString())) {
				release();
				return EVAL_PAGE;
			}

			// Write the tag
			JspWriter out = pageContext.getOut();			
			out.print("<link rel=\"stylesheet\" href=\"");
			out.print(buf.toString());
			out.print("\" />");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "CSS", buf.toString());
		ContentHelper.pushContent(pageContext, buf.toString(), "style");
		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state.
	 */
	@Override
	public void release() {
		super.release();
		_scheme = null;
		_host = null;
	}
}