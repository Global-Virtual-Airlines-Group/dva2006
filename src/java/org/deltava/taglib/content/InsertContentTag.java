// Copyright 2005, 2006, 2009 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * An abstract class for content insertion JSP tags.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public abstract class InsertContentTag extends TagSupport {
	
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