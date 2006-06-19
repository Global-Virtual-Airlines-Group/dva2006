// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.*;

/**
 * A JSP Tag to display IE/Google Map XHTML opening tags.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InsertXHTMLTag extends TagSupport {

	private XMLRenderer _data;
	
	/**
	 * Initializes the tag.
	 */
	public InsertXHTMLTag() {
		super();
		_data = new XMLRenderer("html");
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_data.clear();
	}
	
	/**
	 * Renders the &lt;html&gt; opening tag to the JSP output stream
	 * @return EVAL_BODY_INCLUDE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doStartTag() throws JspException {
		
		// Initialize the tag
		_data.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
		_data.setAttribute("xml:lang", "en");
		_data.setAttribute("lang", "en");
		
		// Set IE-specific attribute
		if (ContentHelper.isIE6(pageContext) || ContentHelper.isIE7(pageContext))
			_data.setAttribute("xmlns:v", "urn:schemas-microsoft-com:vml");
		
		// Render the tag
        try {
            pageContext.getOut().print(_data.open(true));
        } catch (Exception e) {
            throw new JspException(e);
        }
        
        return EVAL_BODY_INCLUDE;
	}
	
	/**
	 * Renders the &lt;/html&gt; closing tag to the JSP output stream.
	 * @return EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doEndTag() throws JspException {
        try {
            pageContext.getOut().print(_data.close());
        } catch (Exception e) {
            throw new JspException(e);
        }
        
        // Clear state and return
        release();
        return EVAL_PAGE;
	}
}