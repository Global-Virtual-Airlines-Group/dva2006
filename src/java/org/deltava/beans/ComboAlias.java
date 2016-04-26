// Copyright 2004, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface used by beans available for display in an HTML combobox.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface ComboAlias {
	
    /**
     * Returns the alias to use in the HTML &lt;OPTION&gt; element.
     * @return The alias for this entry
     */
	public String getComboAlias();
	
	/**
	 * Returns the visible name to use in the HTML &lt;OPTION&gt; element.
	 * @return The visible name for this entry
	 */
	public String getComboName();
}