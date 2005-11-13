// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.content;

import org.deltava.taglib.BrowserDetectingTag;

/**
 * An abstract class for content insertion JSP tags.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class InsertContentTag extends BrowserDetectingTag {
	
    protected boolean _forceInclude;
    
    /**
     * The name of the resource to display, <i>without extension</i>.
     */
    protected String _resourceName;
    
    /**
     * Update the name of the resource to insert.
     * @param name the name of the resource, without extension
     */
    public void setName(String name) {
        _resourceName = name;
    }
    
    /**
     * Toggles if this content shall been included, even if already included previously in the request.
     * @param doForce TRUE if the content should be included
     */
    public void setForce(boolean doForce) {
        _forceInclude = doForce;
    }
    
    /**
     * Releases the tag's state variables.
     */
    public void release() {
       super.release();
       _forceInclude = false;
    }
}