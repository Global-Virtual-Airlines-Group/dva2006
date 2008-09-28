// Copyright 2004, 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

/**
 * An enumeration listing valid Command result types.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public enum ResultType {

	FORWARD(0), REDIRECT(1), HTTPCODE(2), REQREDIRECT(3);

	private final int _code;

	ResultType(int code) {
		_code = code;
	}

	public int getCode() {
		return _code;
	}
}