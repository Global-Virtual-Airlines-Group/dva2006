// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import java.io.IOException;

import javax.servlet.jsp.*;

/**
 * A JSP tag to render a top level menu item in a JSP tag.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class SubMenuTag extends MenuElementTag {
	
	private String _title;
	private int _width;
	private boolean _hasSubMenu;
	
	/**
	 * Sets the menu title.
	 * @param title the title
	 */
	public void setTitle(String title) {
		_title = title;
	}
	
	/**
	 * Sets the width of the menu title.
	 * @param width the width in pixels
	 */
	public void setWidth(int width) {
		_width = Math.max(0, width);
	}

	/**
	 * Writes a sub-menu &lt;URL&gt; element.
	 * @throws IOException if an I/O error occurs
	 */
	void writeSubMenu() throws IOException {
		if (_hasSubMenu || _renderTable)
			return;
		
		_hasSubMenu = true;
		JspWriter out = pageContext.getOut();
		out.println();
		out.print("<ul class=\"submenu\"");
		if (_width > 0)
			out.print(" style=\"width:" + String.valueOf(_width) + "px;\"");
		
		out.println(">");
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_hasSubMenu = false;
	}

	/**
	 * Writes the menu item's opening tag to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		super.doStartTag();
		try {
			JspWriter out = pageContext.getOut();
			if (_renderTable)
				out.print("<tr class=\"submenuTitle\"><td>");
			else {
				out.print("<ul class=\"menu\"");
				if (_width > 0) {
					out.print(" style=\"width:");
					out.print(String.valueOf(_width));
					out.print("px;\"");
				}
				
				out.print("><li class=\"submenuTitle\">");
			}
			
			out.print(_title);
			if (_renderTable)
				out.println("</td></tr>");
			else
				out.print("</li><li class=\"submenu\">");
		} catch (Exception e) {
			throw new JspException(e);
		}
			
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Writes the menu item's closing tag to the JSP output stream.
	 * 	@return TagSuppport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		try {
			if (!_renderTable) {
				if (_hasSubMenu)
					pageContext.getOut().println("</ul>");
			
				pageContext.getOut().print("</li></ul>");
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}