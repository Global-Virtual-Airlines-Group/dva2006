// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration of Library entry security options.
 * @author Luke
 * @version 5.3
 * @since 5.3
 */

public enum Security implements ComboAlias {
	PUBLIC("Public Resource"), AUTH("Authorized Users"), STAFF("Staff Only");
	
	private final String _name;
	
	Security(String name) {
		_name = name;
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