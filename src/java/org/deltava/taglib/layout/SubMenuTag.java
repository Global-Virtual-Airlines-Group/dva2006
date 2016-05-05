// Copyright 2009, 2010, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.DeviceType;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to render a top level menu item in a JSP tag.
 * @author Luke
 * @version 7.0
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
	 */
	void writeSubMenu() throws Exception {
		if (_hasSubMenu || _renderTable)
			return;
		
		_hasSubMenu = true;
		JspWriter out = pageContext.getOut();
		out.println();
		out.print("<ul class=\"submenu\"");
		if (_width > 0) {
			out.print(" style=\"width:");
			out.print(_width);
			out.print("px;\"");
		}
		
		out.println('>');
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_hasSubMenu = false;
	}

	/**
	 * Writes the menu item's opening tag to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		try {
			JspWriter out = pageContext.getOut();
			if (_renderTable)
				out.print("<tr class=\"submenuTitle\"><td>");
			else {
				MenuTag parent = (MenuTag) TagSupport.findAncestorWithClass(this, MenuTag.class);
				if (parent == null)
					throw new JspException("Not contained within MenuTag");
				
				out.print("<ul class=\"menu\"");
				if (_width > 0) {
					// Shrink if we're on a tablet
					int w = _width;
					if (getBrowserContext().getDeviceType() == DeviceType.TABLET) {
						w -= 24;
						w = Math.max(w, (_width * 4 / 5));
						_width -= 5;
					}
					
					out.print(" style=\"width:");
					out.print(w);
					out.print("px");
					
					// Render maxWidth if present
					String maxWidth = parent.getMaxMenuWidth();
					if (!StringUtils.isEmpty(maxWidth)) {
						out.print(";max-width:");
						out.print(maxWidth);
						if (!maxWidth.endsWith("%"))
							out.print("px");
					}
					
					out.print(";\"");
				}
				
				out.print("><li class=\"submenuTitle\"><span>");
			}
			
			out.print(_title);
			if (_renderTable)
				out.println("</td></tr>");
			else
				out.print("</span></li><li class=\"submenu\">");
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
	@Override
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