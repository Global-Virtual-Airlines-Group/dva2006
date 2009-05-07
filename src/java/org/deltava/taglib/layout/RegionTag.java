// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * A JSP tag to render page layouts in a browser-specific way. On Mozilla, absolutely positioned DIV elements will be
 * used, while tables will be used for Internet Explorer.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class RegionTag extends TagSupport {

	private final Map<String, String> _attrs = new HashMap<String, String>();
	private final Map<String, String> _tableAttrs = new HashMap<String, String>(); 
	
	private PageTag _parent;
	private boolean _closeRow;

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
	 * If using Internet Explorer, close the current table row at the end of this element. <i>Note that
	 * the PageTag will automatically insert a row closing element to the JSP output stream, so this
	 * attribute should not be set on the last element on a page.</i>
	 * @param doClose TRUE if a &lt;/TR&gt; element should be written, otherwise FALSE
	 */
	public void setCloseRow(boolean doClose) {
		_closeRow = doClose;
	}

	/**
	 * If using Internet Explorer, the number of rows this table cell should span.
	 * @param rowCount the value of the table cell ROWSPAN attribute
	 * @throws IllegalArgumentException if rowCount is negative
	 * @see RegionTag#setCols(int)
	 */
	public void setRows(int rowCount) {
		if (rowCount < 0)
			throw new IllegalArgumentException("Invalid row count - " + rowCount);

		_tableAttrs.put("rowspan", String.valueOf(rowCount));
	}

	/**
	 * If using Internet Explorer, the number of columns this table cell should span.
	 * @param colCount the value of the table cell COLSPAN attribute
	 * @throws IllegalArgumentException if colCount is ngeative
	 * @see RegionTag#setRows(int)
	 */
	public void setCols(int colCount) {
		if (colCount < 0)
			throw new IllegalArgumentException("Invalid column count - " + colCount);

		_tableAttrs.put("colspan", String.valueOf(colCount));
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		_attrs.clear();
		_tableAttrs.clear();
		_parent = null;
		_closeRow = false;
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
			if (_parent.renderTable()) {
				_attrs.putAll(_tableAttrs);
				if (!_parent.isRowOpen()) {
					out.print("<tr>");
					_parent.setRowOpen(true);
				}
				
				out.print("<td ");
			} else
				out.print("<div ");

			// Write the attributes
			for (Iterator<String> i = _attrs.keySet().iterator(); i.hasNext();) {
				String attrName = i.next();
				String attrValue = _attrs.get(attrName);
				out.print(attrName);
				out.print("=\"");
				out.print(attrValue);
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

		JspWriter out = pageContext.getOut();
		try {
			if (_parent.renderTable()) {
				out.print("</td>");
				if (_closeRow) {
					out.print("</tr>");
					_parent.setRowOpen(false);
				}
			} else
				out.print("</div>");
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		// Release state and return
		release();
		return EVAL_PAGE;
	}
}