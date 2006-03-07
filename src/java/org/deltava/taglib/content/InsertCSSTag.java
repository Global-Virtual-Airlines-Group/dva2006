// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.net.*;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;

import org.deltava.beans.Person;
import org.deltava.taglib.ContentHelper;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert a Cascading Style Sheet.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InsertCSSTag extends InsertContentTag {

	private static final String DEFAULT_SCHEME = "legacy";

	private String _host;
	private String _scheme;
	private boolean _browserSpecific;

	/**
	 * Sets wether to include a brower-specific Cascading Style Sheet.
	 * @param isSpecific TRUE if the CSS is browser-specific, otherwise FALSE
	 */
	public void setBrowserSpecific(boolean isSpecific) {
		_browserSpecific = isSpecific;
	}

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
		_scheme = name;
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
	public int doEndTag() throws JspException {
		
		// Build the path to the CSS file
		StringBuilder buf = new StringBuilder("/");
		buf.append(SystemData.get("path.css"));
		buf.append('/');
		buf.append(getScheme());
		buf.append('/');
		buf.append(_resourceName);

		// Append browser-specific extension
		if (_browserSpecific) {
			if (isFirefox()) {
				buf.append("_ff");
			} else if (isIE()) {
				buf.append("_ie");
			}
		}

		buf.append(".css");
		
		try {
			// Build the resource name if host specified
			if (_host != null) {
				String protocol = pageContext.getRequest().getProtocol(); 
				URL url = new URL(protocol.substring(0, protocol.indexOf('/')), _host, buf.toString());
				buf = new StringBuilder(url.toString());
			}
			
			// Check if the content has already been added
			if (ContentHelper.containsContent(pageContext, "CSS", buf.toString()) && (!_forceInclude)) {
				release();
				return EVAL_PAGE;
			}

			// Write the tag
			JspWriter out = pageContext.getOut();			
			out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			out.print(buf.toString());
			out.print("\" />");
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "CSS", buf.toString());
		release();
		return EVAL_PAGE;
	}

	/**
	 * Release's the tag's state.
	 */
	public void release() {
		super.release();
		_scheme = null;
		_host = null;
	}
}