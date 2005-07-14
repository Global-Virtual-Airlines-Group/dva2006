// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to define row formatting for beans that can be displayed in view tables.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @see org.deltava.taglib.view.RowTag
 */

public interface ViewEntry {

	/**
	 * Returns the CSS class for this object if rendered in a view table.
	 * @return the CSS class name, or NULL if none
	 */
	public String getRowClassName();
}