// Copyright 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

/**
 * An enumeration to store VATSIM/IVAO/FSD user ratings.
 * @author Luke
 * @version 5.0
 * @since 3.2
 */

public enum Rating {
	DISABLED("Disabled"), OBS("Observer"), S1("Student"), S2("Senior Student"), S3("Senior Student"),
	C1("Controller"), C2("Senior Controller"), C3("Senior Controller"), I1("Instructor"),
	I2("Senior Instructor"), I3("Senior Instructor"), SUP("Supervisor"), ADM("Administrator");

	private final String _desc;
	
	Rating(String desc) {
		_desc = desc;
	}
	
	/**
	 * Returns the rating description.
	 * @return the description
	 */
	public String getName() {
		return _desc;
	}
}