// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

/**
 * An enumeration of Flight Academy Certification pre-requisites.
 * @author Luke
 * @version 10.1
 * @since 10.1
 */

public enum Prerequisite implements org.deltava.beans.EnumDescription {
	ANY("No Pre-Requisite"), ANY_PRIOR("Any Prior Stage Certification"), ALL_PRIOR("All Prior Stage Certifications"), SPECIFIC("Specific Certification"), FLIGHTS("Flight Legs"), HOURS("Flight Hours"); 
	
	private final String _desc;
	
	Prerequisite(String desc) {
		_desc = desc;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
}