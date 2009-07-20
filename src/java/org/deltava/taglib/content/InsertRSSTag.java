// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.net.*;

import javax.servlet.jsp.*;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP Tag to insert a link to an RSS data feed.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class InsertRSSTag extends InsertContentTag {

	private static final String DEFAULT_PROTOCOL = "http";

	private String _title;
	private String _protocol;
	private String _host;
	private String _path;

	/**
	 * Sets the title of the RSS feed.
	 * @param title the title
	 */
	public void setTitle(String title) {
		_title = title;
	}

	/**
	 * Sets the protocol used to retrieve the RSS feed
	 * @param protocol the protocol, usually http or https
	 */
	public void setProtocol(String protocol) {
		_protocol = protocol;
	}

	/**
	 * Sets the host name for the RSS feed.
	 * @param hostName the server host name
	 */
	public void setHost(String hostName) {
		_host = hostName;
	}

	/**
	 * Sets the parth to the RSS data feed
	 * @param path the path
	 */
	public void setPath(String path) {
		_path = path;
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_host = null;
		_protocol = null;
	}

	/**
	 * Renders the start of the JSP tag and calculates default values.
	 * @return SKIP_BODY always
	 * @throws JspException never
	 */
	public int doStartTag() throws JspException {
		if (_protocol == null)
			_protocol = DEFAULT_PROTOCOL;

		if (_host == null)
			_host = pageContext.getRequest().getServerName();

		return SKIP_BODY;
	}

	/**
	 * Renders the RSS link to the JSP output stream.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {

		// Check if the content has already been added
		if (ContentHelper.containsContent(pageContext, "RSS", _resourceName)) {
			release();
			return EVAL_PAGE;
		}

		// Build the URL
		URL url = null;
		JspWriter out = pageContext.getOut();
		try {
			url = new URL(_protocol, _host, _path);
			out.print("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"");
			out.print(_title);
			out.print("\" href=\"");
			out.print(url.toString());
			out.print("\" />");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "RSS", url.toString());
		return EVAL_PAGE;
	}
}