package org.deltava.taglib.view;

import java.util.*;
import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP Tag to display view row color legends. These are rendered as a single row HTML table, and the class
 * names used for each row entry are used to set the background colors for each legend entry.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */

public class LegendTag extends TagSupport {

	private String _textClass;
	private String _boxWidth;
	
	private List<String> _labels = new ArrayList<String>();
	private List<String> _classNames = new ArrayList<String>();
	
	/**
	 * Sets the CSS class name for the legend text.
	 * @param cName the CSS class name
	 */
	public void setClassName(String cName) {
		_textClass = cName;
	}
	
	/**
	 * Sets the width of each legend table entry.
	 * @param width the width in pixels or percent
	 */
	public void setWidth(String width) {
		_boxWidth = width;
	}
	
	/**
	 * Sets the labels for each legend entry. Each label will be trimmed using the {@link String#trim()} method,
	 * so use a space for an empty label.
	 * @param labels the comma-delimited list of labels
	 */
	public void setLabels(String labels) {
		StringTokenizer tkns = new StringTokenizer(labels, ",");
		while (tkns.hasMoreTokens())
		    _labels.add(tkns.nextToken().trim());
	}
	
	/**
	 * Sets the CSS class names for each legend entry table cell. Each class name will be trimmed using the
	 * {@link String#trim()} method, so use a space for an empty class name.
	 * @param cNames the comma-delimited list of CSS class names
	 */
	public void setClasses(String cNames) {
		StringTokenizer tkns = new StringTokenizer(cNames, ",");
		while (tkns.hasMoreTokens())
		    _classNames.add(tkns.nextToken().trim());
	}
	
	/**
	 * Resets the tag's state variables. 
	 */
	public void release() {
		super.release();
		_textClass = null;
		_boxWidth = null;
		_labels.clear();
		_classNames.clear();
	}
	
	/**
	 * Renders the legend table to the JSP output stream.
	 * @return TagSupport.SKIP_BODY
	 * @throws JspException if an I/O error occurs, or the number of classNames != the number of labels
	 */
	public int doStartTag() throws JspException {
		
		// Validate that classNames and labels sizes are the same
		if (_labels.size() != _classNames.size())
			throw new JspException("Invalid list sizes - " + _labels.size() + " != " + _classNames.size());
		
		JspWriter out = pageContext.getOut();
		try {
			out.println("<center><table cellspacing=\"2\" cellpadding=\"2\"><tr class=\"legend\">");
			
			// Write the legend cells
			for (int x = 0; x < _labels.size(); x++) {
				String cName = _classNames.get(x);
				
				out.print(" <td class=\"");
				out.print(cName);
				if (_textClass != null)
					out.print(" " + _textClass);
				
				out.print('\"');
				if (_boxWidth != null)
					out.print(" width=\"" + _boxWidth + "\"");
				
				out.print('>');
				out.print(_labels.get(x));
				out.println("</td>");
			}
			
			// Close the table
			out.print("</tr></table></center>");
		} catch (IOException ie) {
			throw new JspException(ie);
		}
		
		release();
		return SKIP_BODY;
	}
}