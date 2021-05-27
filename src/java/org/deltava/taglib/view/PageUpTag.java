// Copyright 2005, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.view;

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.deltava.commands.ViewContext;

/**
 * A JSP tag to handle Page Up links at the bottom of a view page.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class PageUpTag extends ScrollTag {

    /**
     * Creates a new tag with the default label of &quot;PAGE UP&quot;.
     */
    public PageUpTag() {
        super("PAGE UP");
    }
    
    /**
     * Renders the link and label to the JSP output stream.
     * @return TagSupport.SKIP_BODY
     * @throws JspException if an error occurs
     */
    @Override
	public int doStartTag() throws JspException {
        validateTag();
        
        // Check if we're at the start of the view; if so render nothing
        if (_scrollBarTag.isViewStart())
            return SKIP_BODY;
        
        // Add Reserved parameters to the map
        ViewContext<?> vc = _scrollBarTag.getContext();
        Map<String, String> params = vc.getParameters();
        params.put(ViewContext.START, String.valueOf(vc.getPreviousStart()));
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
    		if (!_scrollBarTag.isViewStart()) super.doEndTag();
    		return EVAL_PAGE;
    	} finally {
    		release();
    	}
    }
}