// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.util.system.SystemData;

/**
 * An JSP Tag to generate an IMG element.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ImageTag extends ElementTag {

    /**
     * Creates a new Image element tag.
     */
    public ImageTag() {
        super("img");
    }

    /**
     * Generates this image's HTML.
     * @throws JspException if an error occurs
     */
    public int doEndTag() throws JspException {
        
        // Do a proper XHTML tag
        try {
            _out.print(_data.open(true, true));
        } catch (Exception e) {
            throw new JspException(e);
        }
        
        // Reset the parameters for next time
        release();
        return EVAL_PAGE;
    }
    
    /**
     * Sets the width of ths image. This does nothing if a negative, zero or non-numeric value is passed.
     * @param width the width of the image in pixels
     * @see ElementTag#setNumericAttr(String, int, int)
     */
    public void setX(int width) {
        setNumericAttr("width", width, 1);
    }
    
    /**
     * Sets the height of ths image. This does nothing if a negative, zero or non-numeric value is passed.
     * @param height the height of the image in pixels
     * @see ElementTag#setNumericAttr(String, int, int)
     */
    public void setY(int height) {
        setNumericAttr("height", height, 1);
    }
    
    /**
     * Sets the border of ths image. This does nothing if a negative or non-numeric value is passed.
     * @param border the border of the image in pixels
     * @see ElementTag#setNumericAttr(String, int, int)
     */
    public void setBorder(int border) {
    	setNumericAttr("border", border, 0);
    }
    
    /**
     * Sets the source of this image.
     * @param url the location of the image
     */
    public void setSrc(String url) {
        StringBuilder buf = new StringBuilder(SystemData.get("path.img"));
        buf.append('/');
        buf.append(url);
        _data.setAttribute("src", buf.toString());
    }
    
    /**
     * Sets the alternate caption for this image.
     * @param caption the caption for this mage
     */
    public void setCaption(String caption) {
        _data.setAttribute("alt", caption);
    }
}