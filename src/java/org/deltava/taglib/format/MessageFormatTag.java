// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.net.*;
import java.util.StringTokenizer;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.cooler.Emoticons;

import org.deltava.taglib.ContentHelper;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support writing formatted text with URLs and emoticons.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class MessageFormatTag extends TagSupport {

	private String _msg;

	/**
	 * Updates the text to format.
	 * @param msg the text to format
	 */
	public void setValue(String msg) {
		_msg = msg;
	}
	
	/**
	 * Helper method to generate an emotion IMG tag.
	 */
	private String emoticonURL(String name) {
		StringBuilder imgbuf = new StringBuilder("<img src=\"");
		imgbuf.append(SystemData.get("path.img"));
		imgbuf.append("/cooler/emoticons/");
		imgbuf.append(name);
		imgbuf.append(".gif\" border=\"0\" alt=\"");
		imgbuf.append(name);
		imgbuf.append("\" />");
		return imgbuf.toString();
	}
	
	/**
	 * Checks for the inclusion of common.js in the request.
	 * @return TagSupport.SKIP_BODY
	 * @throws JspException if common.js is not included
	 */
	public int doStartTag() throws JspException {

		// Ensure that the common JS file has been included
    	if (!ContentHelper.containsContent(pageContext, "JS", "common"))
    		throw new IllegalStateException("common.js not included in request");
    	
    	return SKIP_BODY;
	}

	/**
	 * Renders the formatted message text to the JSP output stream.
	 * @return the formatted text
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		
		// Break out the string
		StringTokenizer tkns = new StringTokenizer(_msg, " \n\r", true);
		try {
			JspWriter out = pageContext.getOut();
			while (tkns.hasMoreTokens()) {
				String token = tkns.nextToken();
				if (token.startsWith("http://") || token.startsWith("https://")) {
					try {
						URL url = new URL(token); 
						out.print("<a href=\"");
						out.print(token);
						if (!SystemData.get("airline.url").equals(url.getHost()))
							out.print("\" rel=\"external");
					
						out.print("\">");
						out.print(StringUtils.stripInlineHTML(token));
						out.print("</a>");
					} catch (MalformedURLException mue) {
						out.print(token);
					}
				} else if ((token.charAt(0) == ':') && (token.length() > 2)
						&& (token.charAt(token.length() - 1) == ':')) {
					int iCode = StringUtils.arrayIndexOf(Emoticons.ICON_NAMES, token.substring(1, token.length() - 1));
					if (iCode != -1)
						out.print(emoticonURL(Emoticons.ICON_NAMES[iCode]));
					else
						out.print(StringUtils.stripInlineHTML(token));
				} else if (((token.charAt(0) == ':') || (token.charAt(0) == ';')) && (token.length() == 2)) {
					for (int x = 0; x < Emoticons.ICON_CODES.length; x++) {
						if (token.equals(Emoticons.ICON_CODES[x])) {
							out.print(emoticonURL(Emoticons.ICON_NAMES[x]));
							break;
						}
					}
				} else {
					if (token.length() > 1) {
						out.print(StringUtils.stripInlineHTML(token));
					} else if (token.equals("\n")) {
						out.print("<br />\n");
					} else {
						out.print(token);
					}
				}
			}
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_PAGE;
	}
}