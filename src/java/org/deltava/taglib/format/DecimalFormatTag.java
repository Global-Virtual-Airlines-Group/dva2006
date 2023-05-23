// Copyright 2005, 2016, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

/**
 * A JSP tag to support the rendering of formatted decimal values.
 * @author Luke
 * @version 10.6
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
    
    @Override
	public void release() {
        super.release(DEFAULT_PATTERN);
    }
    
    /**
     * Prints the value, handling NaN values gracefully.
     */
    @Override
    protected void printValue() throws Exception {
    	if (Double.isNaN(_value.doubleValue()))
    		pageContext.getOut().print('-');
    	else
    		super.printValue();
    }
}