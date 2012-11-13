// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration to store ACARS access restrictions. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public enum Restriction implements ComboAlias {
	OK("Unlimited Usage"), RESTRICT("Restricted Messaging"), NOMSGS("Flight Reports Only"), 
		BLOCK("Blocked"), NOMANUAL("No Manual Flight Reports");

	private final String _name;
	
	Restriction(String name) {
		_name = name;
	}
	
	public String getName() {
		return _name;
	}

	@Override
	public String getComboAlias() {
		return name();
	}

	@Override
	public String getComboName() {
		return _name;
	}
}