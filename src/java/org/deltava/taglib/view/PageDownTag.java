// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.view;

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.deltava.commands.ViewContext;

/**
 * A JSP tag to handle Page Down links at the bottom of a view page.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class PageDownTag extends ScrollTag {
    
    /**
     * Creates a new tag with the default label of &quot;PAGE DOWN&quot;.
     */
    public PageDownTag() {
        super("PAGE DOWN");
    }
    
    /**
     * Renders the link and label to the JSP output stream.
     * @return TagSupport.SKIP_BODY
     * @throws JspException if an error occurs
     */
    public int doStartTag() throws JspException {
        validateTag();
        
        // Check if we're at the end of the view; if so render nothing
        if (_scrollBarTag.isViewEnd() && !_scrollBarTag.isForced())
            return SKIP_BODY;
        
        // Add Reserved parameters to the map
        // Yes, converting to a string array is stupid
        ViewContext vc = _scrollBarTag.getContext();
        Map<String, Object> params = vc.getParameters();
        params.put(ViewContext.START, new String[] { String.valueOf(vc.getStart() + vc.getCount()) } );
        params.put(ViewContext.COUNT, new String[] { String.valueOf(vc.getCount()) } );
        if (vc.getSortType() != null)
            params.put(ViewContext.SORTBY, new String[] { vc.getSortType() } );
        
        // Get the view command name
        StringBuilder url = new StringBuilder(_viewTag.getCmd());
        url.append(".do?");
        
        // Append the query parameters
        url.append(buildParameters(params));
       
        // Set the link and call the superclass renderer
        _data.setAttribute("href", url.toString());
        super.doStartTag();
        renderLabel();
        return SKIP_BODY;
    }
    
    /**
     * Closes the link tag and outputs it to the JSP output stream.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
    public int doEndTag() throws JspException {

        // Check if we're at the start of the view; if so render nothing
        if (!_scrollBarTag.isViewEnd())
        	super.doEndTag();

        release();
        return EVAL_PAGE;
    }
}