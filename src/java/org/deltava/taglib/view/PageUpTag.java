package org.deltava.taglib.view;

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.deltava.commands.ViewContext;

/**
 * A JSP tag to handle Page Up links at the bottom of a view page.
 * @author Luke
 * @version 1.0
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
    public int doStartTag() throws JspException {
        
        validateTag();
        ViewContext vc = (ViewContext) pageContext.findAttribute(ViewContext.VIEW_CONTEXT);
        
        // Check if we're at the start of the view; if so render nothing
        if ((vc == null) || (vc.getStart() == 0))
            return SKIP_BODY;
        
        // Add Reserved parameters to the map
        // And yes, converting to a String array is stupid
        Map params = vc.getParameters();
        params.put(ViewContext.START, new String[] { String.valueOf(vc.getPreviousStart()) } );
        params.put(ViewContext.COUNT, new String[] { String.valueOf(vc.getCount()) } );
        if (vc.getSortType() != null)
            params.put(ViewContext.SORTBY, new String[] { vc.getSortType() } );        
        
        // Get the view command name
        StringBuffer url = new StringBuffer(_viewTag.getCmd());
        url.append(".do?");
        
        // Append the query parameters
        url.append(buildParameters(params));
        
        // Set the link and call the superclass renderer
        _attrs.put("href", url.toString());
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
        ViewContext vc = (ViewContext) pageContext.findAttribute(ViewContext.VIEW_CONTEXT);

        // Check if we're at the start of the view; if so render nothing
        if ((vc == null) || (vc.getStart() == 0))
            return EVAL_PAGE;

        super.doEndTag();
        release();
        return EVAL_PAGE;
    }
}