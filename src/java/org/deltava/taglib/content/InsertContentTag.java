// Copyright 2005, 2006, 2009, 2014 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.content;

import org.deltava.taglib.BrowserInfoTag;

/**
 * An abstract class for content insertion JSP tags.
 * @author Luke
 * @version 5.3
 * @since 1.0
 */

abstract class InsertContentTag extends BrowserInfoTag {
	
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
}