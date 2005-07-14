package org.deltava.taglib.content;

import java.io.IOException;
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
    public void setVisible(String isVisible) {
        _visible = Boolean.valueOf(isVisible).booleanValue();
    }
  
    private void displayCopyrightComment(JspWriter jw) throws IOException {
        jw.print("<!-- " + VersionInfo.APPNAME + " " + VersionInfo.TXT_COPYRIGHT + " (Build " +
              VersionInfo.BUILD + ") -->");
    }
    
    private void displayCopyright(JspWriter jw) throws IOException {
        jw.println("<hr />");
        jw.print("<span class=\"copyright\">");
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
                displayCopyright(pageContext.getOut());
            } else {
                displayCopyrightComment(pageContext.getOut());
            }    
        } catch (IOException ie) {
            JspException je = new JspException("Error writing " + getClass().getName());
            je.initCause(ie);
            je.setStackTrace(ie.getStackTrace());
            throw je;
        }

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