// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import org.deltava.beans.CalendarEntry;

/**
 * An interface to store common properties for Flight Academy instruction sessions with an Instructor
 * and a Pilot.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Instruction extends CalendarEntry {

	/**
	 * Returns the databae ID of the Instructor.
	 * @return the Instructor's database ID
	 */
	public int getInstructorID();
	
	/**
	 * Returns the databae ID of the Student.
	 * @return the Student's database ID
	 */
	public int getPilotID();
	
	/**
	 * Returns the databae ID of the Flight Academy Course.
	 * @return the Course's database ID
	 */
	public int getCourseID();
}