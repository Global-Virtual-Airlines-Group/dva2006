// Copyright 2005, 2006, 2009, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.net.*;
import java.util.StringTokenizer;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.cooler.Emoticons;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.*;
import org.deltava.util.bbcode.*;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support writing formatted text with URLs and emoticons.
 * @author Luke
 * @version 6.0
 * @since 1.0
 */

public class MessageFormatTag extends TagSupport {

	private String _msg;
	private boolean _bbCode;
	private final BBCodeHandler _bbHandler = new BBCodeHandler();

	/**
	 * Sets whether BBCodes are to be translated.
	 * @param useBBCode TRUE to translate, otherwise FALSE
	 */
	public void setBbCode(boolean useBBCode) {
		_bbCode = useBBCode;
	}

	/**
	 * Updates the text to format.
	 * @param msg the text to format
	 */
	public void setValue(String msg) {
		_msg = msg;
	}

	/*
	 * Helper method to generate an emotion IMG tag.
	 */
	private static String emoticonURL(Emoticons ei) {
		StringBuilder imgbuf = new StringBuilder("<img src=\"");
		imgbuf.append(SystemData.get("path.img"));
		imgbuf.append("/cooler/emoticons/");
		imgbuf.append(ei.getName());
		imgbuf.append(".gif\" class=\"noborder\" alt=\"");
		imgbuf.append(ei.getName());
		imgbuf.append("\" title=\"");
		imgbuf.append(ei.getName());
		imgbuf.append("\" />");
		return imgbuf.toString();
	}

	/**
	 * Checks for the inclusion of common.js in the request.
	 * @return TagSupport.SKIP_BODY
	 * @throws JspException if common.js is not included
	 */
	@Override
	public int doStartTag() throws JspException {

		// Ensure that the common JS file has been included
		if (!ContentHelper.containsContent(pageContext, "JS", "common"))
			throw new IllegalStateException("common.js not included in request");

		// Check if we have any tokens
		_bbCode &= ((_msg.indexOf('[') > -1) && (_msg.indexOf(']') > -1));
		if (_bbCode && (_bbHandler.getAll().isEmpty()))
			_bbHandler.init();
		
		return SKIP_BODY;
	}

	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_bbCode = false;
	}

	/**
	 * Renders the formatted message text to the JSP output stream.
	 * @return the formatted text
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {

		// Break out the string
		StringBuilder buf = new StringBuilder();
		StringTokenizer tkns = new StringTokenizer(_msg, " \n\r", true);
		while (tkns.hasMoreTokens()) {
			String token = tkns.nextToken();
			if (!_bbCode && (token.startsWith("http://") || token.startsWith("https://"))) {
				try {
					URL url = new URL(token);
					buf.append("<a href=\"");
					buf.append(token);
					if (!SystemData.get("airline.url").equals(url.getHost()))
						buf.append("\" target=\"_new\" rel=\"external");

					buf.append("\">");
					buf.append(StringUtils.stripInlineHTML(token));
					buf.append("</a>");
				} catch (MalformedURLException mue) {
					buf.append(token);
				}
			} else if ((token.charAt(0) == ':') && (token.length() > 2) && (token.indexOf(':', 2) > 0)) {
				int endPos = token.indexOf(':', 2);
				String emIcon = token.substring(1, endPos);
				Emoticons ei = Emoticons.find(emIcon);
				if (ei != null) {
					buf.append(emoticonURL(ei));
					buf.append(token.substring(endPos + 1));
				} else
					buf.append(StringUtils.stripInlineHTML(token));
			} else if (((token.charAt(0) == ':') || (token.charAt(0) == ';')) && (token.length() == 2)) {
				Emoticons ei = Emoticons.find(token);
				if (ei != null)
					buf.append(emoticonURL(ei));
			} else {
				if (token.length() > 1)
					buf.append(StringUtils.stripInlineHTML(token));
				else if (token.equals("\n"))
					buf.append("<br />\n");
				else
					buf.append(token);
			}
		}

		// Do BBCode parsing
		String msg = buf.toString();
		if (_bbCode) {
			for (BBCode bb : _bbHandler.getAll())
				msg = msg.replaceAll(bb.getRegex(), bb.getReplace());
		}
		
		try {
			pageContext.getOut().print(msg);
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}