// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * A JSP tag to render &quot;is&quot; and &quot;are&quot;.
 * @author Luke
 * @version 5.1
 * @since 5.1
 */

public class IsFormatTag extends SimpleTagSupport {

	private long _qty;
	
    /**
     * Sets the value to format. <i>This is a nasty hack for Tomcat 6.0.18+ EL casting.</i>
     * @param value the value to format
     */
    public void setValue(Long value) {
        _qty = (value == null) ? 0 : value.longValue();
    }
	
    /**
     * Renders &quot;is&quot; or &quot;are&quot; depending on the value.
     */
    @Override
	public void doTag() throws JspException {
		
        JspWriter out = getJspContext().getOut();
        try {
        	out.print((_qty == 1) ? "is" : "are");
        } catch (Exception e) {
        	throw new JspException(e);
        }
	}
}