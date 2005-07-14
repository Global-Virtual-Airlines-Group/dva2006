package org.deltava.taglib.format;

import java.text.DecimalFormat;

/**
 * A JSP tag to support the rendering of formatted decimal values.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DecimalFormatTag extends NumberFormatTag {

    private static final String DEFAULT_PATTERN = "##,###,##0.0";
    
    /**
     * Initializes the tag and the number formatter.
     */
    public DecimalFormatTag() {
        super();
        _nF = new DecimalFormat(DEFAULT_PATTERN);
        _nF.setDecimalSeparatorAlwaysShown(true);
    }

    /**
     * Resets this tag's data when its lifecycle is complete.
     */
    public void release() {
        super.release(DEFAULT_PATTERN);
    }    
}