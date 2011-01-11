// Copyright 2005, 2006, 2008, 2009, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * A JSP tag to render page layouts in a user-specific way.
 * @author Luke
 * @version 3.6
 * @since 1.0
 */

public class RegionTag extends TagSupport {

	private final Map<String, String> _attrs = new HashMap<String, String>();
	private PageTag _parent;

	/**
	 * Sets the DOM ID of the Region element.
	 * @param id the ID
	 */
	public void setId(String id) {
		_attrs.put("id", id);
	}

	/**
	 * Sets the CSS class name(s) of the Region element.
	 * @param cName the CSS class names
	 */
	public void setClassName(String cName) {
		_attrs.put("class", cName);
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		_attrs.clear();
		super.release();
	}

	/**
	 * Writes the layout element's opening tag to the JSP output stream.
	 * @return TagSuppport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		
		// Make sure the parent page tag has been set
		_parent = (PageTag) TagSupport.findAncestorWithClass(this, PageTag.class);
		if (_parent == null)
			throw new JspException("Must be contained within a PAGE ELEMENT tag");

		JspWriter out = pageContext.getOut();
		try {
			out.print("<div ");

			// Write the attributes
			for (Iterator<Map.Entry<String, String>> i = _attrs.entrySet().iterator(); i.hasNext();) {
				Map.Entry<String, String> me = i.next();
				out.print(me.getKey());
				out.print("=\"");
				out.print(me.getValue());
				out.print(i.hasNext() ? "\" " : "\"");
			}

			// Close the element opening tag
			out.print('>');
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Writes the layout element's closing tag to the JSP output stream.
	 * @return TagSuppport.EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().print("</div>");
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		// Release state and return
		release();
		return EVAL_PAGE;
	}
}