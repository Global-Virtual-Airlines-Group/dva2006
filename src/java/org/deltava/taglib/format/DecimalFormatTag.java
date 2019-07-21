// Copyright 2005, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

/**
 * A JSP tag to support the rendering of formatted decimal values.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class DecimalFormatTag extends NumberFormatTag {

    private static final String DEFAULT_PATTERN = "##,###,##0.0";
    
    /**
     * Initializes the tag and the number formatter.
     */
    public DecimalFormatTag() {
        super(DEFAULT_PATTERN);
    }
    
    /**
     * Resets this tag's data when its lifecycle is complete.
     */
    @Override
	public void release() {
        super.release(DEFAULT_PATTERN);
    }    
}