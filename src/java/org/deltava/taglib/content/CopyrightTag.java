package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.VersionInfo;

/**
 * A JSP Tag to insert a copyright notice. This tag is a useful test to ensure that the tag libraries are being loaded.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CopyrightTag extends TagSupport {

    private boolean _visible = true;
    
    /**
     * Marks the copyright tag as visible instead of embedded in the HTML code.
     * @param isVisible TRUE if the tag should be visible, otherwise FALSE
     */
    public void setVisible(boolean isVisible) {
        _visible = isVisible;
    }
  
    private void displayCopyrightComment() throws Exception {
    	JspWriter jw = pageContext.getOut();
        jw.print("<!-- ");
        jw.print(pageContext.getServletContext().getServletContextName());
        jw.print(" ");
        jw.print(VersionInfo.APPNAME);
        jw.print(" ");
        jw.print(VersionInfo.TXT_COPYRIGHT);
        jw.print(" (Build " + VersionInfo.BUILD + ") -->");
    }
    
    private void displayCopyright() throws Exception {
    	JspWriter jw = pageContext.getOut();
        jw.println("<hr />");
        jw.print("<span class=\"copyright\">");
        jw.print(pageContext.getServletContext().getServletContextName());
        jw.print(" ");
        jw.print(VersionInfo.APPNAME + " " + VersionInfo.HTML_COPYRIGHT + " (Build " + VersionInfo.BUILD + ")");
        jw.print("</span>");
    }
    
    /**
     * Renders the copyright tag to the JSP output stream.
     * @return TagSupport.EVAL_PAGE always
     * @throws JspException if an I/O error occurs
     */
    public int doEndTag() throws JspException {
        try {
            if (_visible) {
                displayCopyright();
            } else {
                displayCopyrightComment();
            }    
        } catch (Exception e) {
            throw new JspException("Error writing " + getClass().getName(), e);
        }

        // Release state and return
        release();
        return EVAL_PAGE;
    }

    /**
     * Release's the tag's state variables.
     */
    public void release() {
       super.release();
        _visible = true;
    }
}