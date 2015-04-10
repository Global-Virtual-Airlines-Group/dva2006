// Copyright 2005, 2006, 2007, 2010, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.io.*;
import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.VersionInfo;
import org.deltava.util.ConfigLoader;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to insert a copyright notice. This tag is a useful test to ensure that the tag libraries are being loaded.
 * @author Luke
 * @version 6.0
 * @since 1.0
 */

public class CopyrightTag extends TagSupport {

	private final Map<String, String> _props = new HashMap<String, String>();
	private boolean _visible = true;

	/**
	 * Marks the copyright tag as visible instead of embedded in the HTML code.
	 * @param isVisible TRUE if the tag should be visible, otherwise FALSE
	 */
	public void setVisible(boolean isVisible) {
		_visible = isVisible;
	}

	private void displayCopyrightComment() throws Exception {
		JspWriter jw = pageContext.getOut();
		jw.print("<!-- ");
		jw.print(pageContext.getServletContext().getServletContextName());
		jw.print(' ');
		jw.print(VersionInfo.APPNAME);
		jw.print(' ');
		jw.print(VersionInfo.TXT_COPYRIGHT);
		jw.print(" (Build ");
		jw.print(String.valueOf(VersionInfo.BUILD));
		jw.print(" ");
		jw.print(_props.get("build.date"));
		jw.print(") -->");
	}

	private void displayCopyright() throws Exception {
		JspWriter jw = pageContext.getOut();
		jw.println("<hr />");
		jw.print("<div class=\"small\">");
		jw.print(pageContext.getServletContext().getServletContextName());
		jw.print(' ');
		jw.print(VersionInfo.APPNAME );
		jw.print(' ');
		jw.print(VersionInfo.HTML_COPYRIGHT .replace("http", pageContext.getRequest().getScheme()));
		jw.print(" (Build ");
		jw.print(VersionInfo.BUILD);
		jw.print(")</div>");

		// Display disclaimer
		String disclaimer = SystemData.get("airline.copyright");
		if (disclaimer != null) {
			jw.print("\n<span class=\"copyright small\">");
			jw.print(disclaimer);
			jw.print("</span>");
		}
	}
	
	/**
	 * Loads the build properties.
	 * @return TagSupport#SKIP_BODY always
	 */
	@Override
	public int doStartTag() throws JspException {
		if (_props.isEmpty()) {
			Properties p = new Properties(); p.put("build.date", "");
			try (InputStream is = ConfigLoader.getStream("/golgotha_build.properties")) {
				p.load(is);
			} catch (Exception e) {
				// 	empty
			} finally {
				for (Object n : p.keySet()) {
					String k  = String.valueOf(n);
					_props.put(k, p.getProperty(k));
				}
			}
		}
		
		return SKIP_BODY;
	}

	/**
	 * Renders the copyright tag to the JSP output stream.
	 * @return TagSupport#EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			if (_visible)
				displayCopyright();
			else
				displayCopyrightComment();
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_visible = true;
	}
}