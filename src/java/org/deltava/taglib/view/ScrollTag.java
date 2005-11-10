package org.deltava.taglib.view;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.deltava.taglib.html.LinkTag;

import org.deltava.util.StringUtils;

/**
 * A class to support tag to handle Page Up/Down links at the bottom of a view page.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class ScrollTag extends LinkTag {
    
    protected TableTag _viewTag;
    protected String _tagLabel;
    
    /**
     * Creates a new JSP tag with the specified label.
     * @param label the label text
     * @see ScrollTag#setText(String)
     */
    protected ScrollTag(String label) {
        super();
        setText(label);
    }

    /**
     * Updates the URL for this tag. </i>NOT IMPLEMENTED</i>
     * @throws UnsupportedOperationException
     */
    public final void setUrl(String cmd) {
        throw new UnsupportedOperationException();
    }

    /**
     * Updates the target frame for this tag. </i>NOT IMPLEMENTED</i>
     * @throws UnsupportedOperationException
     */
    public final void setTarget(String targetFrame) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * A helper method to bundle request parameters into a URL string.
     * @param params a Map of paramater name/value pairs
     * @return the Query String
     */
    protected String buildParameters(Map params) {
        StringBuffer url = new StringBuffer();
        
        // Loop through the parameters
        for (Iterator i = params.keySet().iterator(); i.hasNext(); ) {
            String pName = (String) i.next();
            String[] pValues = (String[]) params.get(pName);
            url.append(StringUtils.stripInlineHTML(pName));
            url.append('=');
            url.append(StringUtils.stripInlineHTML(pValues[0]));
            if (i.hasNext())
                url.append("&amp;");
        }
        
        // Return the string
        return url.toString();
    }

    /**
     * Updates the URL label text.
     * @param label the new URL label
     */
    public void setText(String label) {
        _tagLabel = label;
    }
   
    /**
     * Validates the tag to ensure it is contained within an enclosing &lt;view:table&gt; tag.
     * @throws JspException if the tag is not enclosed
     */
    protected void validateTag() throws JspException {
        _viewTag = (TableTag) TagSupport.findAncestorWithClass(this, org.deltava.taglib.view.TableTag.class);
        if (_viewTag == null)
            throw new JspTagException("view:scroll Tag must be contained within view:table Tag");
    }
    
    /**
     * Outputs the tag label to the JSP output stream.
     * @throws JspException if an error occurs
     */
    protected void renderLabel() throws JspException {
        try {
            pageContext.getOut().print(_tagLabel);
        } catch (Exception e) {
            throw new JspException(e);
        }
    }
}