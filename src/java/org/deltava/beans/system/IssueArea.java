// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * An enumeration of development issue Areas.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum IssueArea implements org.deltava.beans.ComboAlias {
	WEBSITE("Web Site"), FLEET("Fleet Library"), MANUALS("Documentation"), EXAMS("Examinations"), ACARS("ACARS"), SERVER("Server Administration"), SCHEDULE("Flight Schedule"), DISPATCH("Dispatch");
	
	private final String _desc;

	/**
	 * Creates the enumeration value.
	 * @param desc the description
	 */
	IssueArea(String desc) {
		_desc = desc;
	}
	
	/**
	 * Returns a propercased description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}

	@Override
	public String getComboName() {
		return getDescription();
	}
}