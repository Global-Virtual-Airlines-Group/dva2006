// Copyright 2005, 2008, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.view;

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.deltava.commands.ViewContext;

/**
 * A JSP tag to handle Page Down links at the bottom of a view page.
 * @author Luke
 * @version 7.0
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
    @Override
	public int doStartTag() throws JspException {
        validateTag();
        
        // Check if we're at the end of the view; if so render nothing
        if (_scrollBarTag.isViewEnd() && !_scrollBarTag.isForced())
            return SKIP_BODY;
        
        // Add Reserved parameters to the map
        ViewContext<?> vc = _scrollBarTag.getContext();
        Map<String, String> params = vc.getParameters();
        params.put(ViewContext.START, String.valueOf(vc.getStart() + vc.getCount()));
        params.put(ViewContext.COUNT, String.valueOf(vc.getCount()));
        if (vc.getSortType() != null)
            params.put(ViewContext.SORTBY, vc.getSortType());
        
        // Get the view command name with parameters
        StringBuilder url = new StringBuilder(_viewTag.getCmd());
        url.append(".do?").append(buildParameters(params));
       
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
    @Override
	public int doEndTag() throws JspException {

        // Check if we're at the start of the view; if so render nothing
    	try {
    		if (!_scrollBarTag.isViewEnd()) super.doEndTag();
    		return EVAL_PAGE;
    	} finally {
    		release();
    	}
    }
}