// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.layout;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * An abstract clas to JSP tags that display navigation menu elements. 
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

abstract class MenuElementTag extends TagSupport {
	
	/**
	 * Stores whether elements are rendered via tables or lists.
	 */
	protected boolean _renderTable;

	/**
	 * Checks if we are rendering page regions as TABLEs or DIVs. If we are using
	 * TABLEs, this tag and its subclasses will degrede to generating &lt;TR&gt; elements,
	 * rather than &lt;UL&gt;  and &lt;LI&gt; elements.
	 * @return EVAL_BODY_INCLUDE always
	 */
	public int doStartTag() throws JspException {
		
		// Check parent for side menu rendering
		PageTag parent = (PageTag) TagSupport.findAncestorWithClass(this, PageTag.class);
		_renderTable = ((parent == null) || parent.sideMenu());
		return EVAL_BODY_INCLUDE;
	}
}