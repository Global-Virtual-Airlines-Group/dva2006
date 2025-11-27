// Copyright 2005, 2006, 2009, 2016, 2018, 2020, 2022, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import jakarta.servlet.jsp.*;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP Tag to insert a link to an RSS data feed.
 * @author Luke
 * @version 12.3
 * @since 1.0
 */

public class InsertRSSTag extends InsertContentTag {

	private static final String DEFAULT_PROTOCOL = "https";

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

	@Override
	public void release() {
		super.release();
		_host = null;
		_protocol = null;
	}

	@Override
	public int doStartTag() throws JspException {
		if (_protocol == null)
			_protocol = DEFAULT_PROTOCOL;
		if (_host == null)
			_host = pageContext.getRequest().getServerName();

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {

		// Check if the content has already been added
		if (ContentHelper.containsContent(pageContext, "RSS", _resourceName)) {
			release();
			return EVAL_PAGE;
		}

		// Build the URL
		String url = String.format("%s://%s/%s", _protocol, _host, _path);
		JspWriter out = pageContext.getOut();
		try {
			out.print("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"");
			out.print(_title);
			out.print("\" href=\"");
			out.print(url);
			out.println("\">");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		// Mark the content as added and return
		ContentHelper.addContent(pageContext, "RSS", url);
		return EVAL_PAGE;
	}
}