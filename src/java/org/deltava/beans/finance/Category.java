// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.finance;

/**
 * A bean to track cost categories. 
 * @author Luke
 * @version 13.1
 * @since 13.1
 */

public class Category {
	
	private final String _name;
	private final boolean _isActive;

	/**
	 * Creates the bean.
	 * @param name the category name
	 * @param isActive TRUE if active, otherwise FALSE 
	 */
	public Category(String name, boolean isActive) {
		super();
		_name = name;
		_isActive = isActive;
	}

	/**
	 * Returns the category name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns whether the cateogry is active. 
	 * @return TRUE if active, otherwise FALSE
	 */
	public boolean getActive() {
		return _isActive;
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
}