// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 1.0
 * @since 1.0
 */

public class InsertCSSTag extends InsertContentTag {

	/**
	 * Default CSS scheme name.
	 */
	static final String DEFAULT_SCHEME = "legacy";

	private String _host;
	private String _scheme;
	private boolean _browserSpecific;
	private String _ie7suffix;

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
		_scheme = (name == null) ? null : name.toLowerCase().replace(' ', '_');
	}
	
	/**
	 * Overrides the file suffix to use for Internet Explorer 7.
	 * @param suffix the suffix
	 */
	public void setIe7suffix(String suffix) {
		_ie7suffix = suffix;
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
			buf.append('_');
			if (ContentHelper.isFirefox(pageContext))
				buf.append("ff");
			else if (ContentHelper.isIE7(pageContext) && (_ie7suffix != null))
				buf.append(_ie7suffix);
			else if (ContentHelper.isIE6(pageContext) || ContentHelper.isIE7(pageContext))
				buf.append("ie");
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
		} finally {
			release();
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "CSS", buf.toString());
		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state.
	 */
	public void release() {
		super.release();
		_scheme = null;
		_host = null;
		_ie7suffix = null;
	}
}