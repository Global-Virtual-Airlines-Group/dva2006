// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean to track Flight Academy Instruction Flights.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionFlight extends DatabaseBean implements Instruction, InstructorBean {

	private int _instructorID;
	private int _pilotID;
	private int _courseID;
	private String _courseName;
	private Date _startDate;
	private String _eqType;
	private int _length;
	private String _comments;

	/**
	 * Creates a new Instruction Log entry.
	 * @param instructorID the Instructor pilot's database ID
	 * @param courseID the Course's database ID
	 * @throws IllegalArgumentException if instructorID or courseID are zero or negative
	 */
	public InstructionFlight(int instructorID, int courseID) {
		super();
		setInstructorID(instructorID);
		setCourseID(courseID);
	}

	/**
	 * Returns the aircraft type used for this flight.
	 * @return the equipment type
	 * @see InstructionFlight#setEquipmentType(String)
	 */
	public String getEquipmentType() {
		return _eqType;
	}

	/**
	 * Returns any comments about this flight.
	 * @return the comments
	 * @see InstructionFlight#setComments(String)
	 */
	public String getComments() {
		return _comments;
	}

	/**
	 * Returns the database ID of the Flight Academy course associated with this flight.
	 * @return the Course's database ID
	 * @see InstructionFlight#setCourseID(int)
	 */
	public int getCourseID() {
		return _courseID;
	}
	
	/**
	 * Returns the name of the Flight Academy course associated with this flight.
	 * @return the Course's name
	 * @see InstructionFlight#setCourseName(String)
	 * @see InstructionFlight#getCourseID()
	 */
	public String getCourseName() {
		return _courseName;
	}
	
	/**
	 * Returns the database ID of the Instructor Pilot associated with this flight.
	 * @return the Pilot's database ID
	 * @see InstructionFlight#setInstructorID(int)
	 * @see InstructionFlight#getPilotID()
	 */
	public int getInstructorID() {
		return _instructorID;
	}
	
	/**
	 * Returns the database ID of the student associated with this flight.
	 * @return the Pilot's database ID
	 * @see InstructionFlight#setPilotID(int)
	 * @see InstructionFlight#getInstructorID()
	 */
	public int getPilotID() {
		return _pilotID;
	}

	/**
	 * Returns the date this flight was started.
	 * @return the start date/time of this flight
	 * @see InstructionFlight#setDate(Date)
	 */
	public Date getDate() {
		return _startDate;
	}

	/**
	 * Returns the length of the fllight <i>in hours multiplied by ten</i>. This is done to avoid rounding errors when
	 * using a floating point number.
	 * @return the length of the flight <i>in hours multiplied by ten</i>
	 * @see InstructionFlight#setLength(int)
	 */
	public int getLength() {
		return _length;
	}

	/**
	 * Updates the aircraft type used in this flight.
	 * @param eqType the aircraft type
	 * @see InstructionFlight#getEquipmentType()
	 */
	public void setEquipmentType(String eqType) {
		_eqType = eqType;
	}

	/**
	 * Updates the the Flight Academy course associated with this flight.
	 * @param id the Course's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see InstructionFlight#getCourseID()
	 */
	public void setCourseID(int id) {
		validateID(_courseID, id);
		_courseID = id;
	}
	
	/**
	 * Updates the the Instructor Pilot associated with this flight.
	 * @param id the Pilot's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see InstructionFlight#getInstructorID()
	 * @see InstructionFlight#setPilotID(int)
	 */
	public void setInstructorID(int id) {
        if (id < 1)
            throw new IllegalArgumentException("Database ID cannot be zero or negative");
        
		_instructorID = id;
	}
	
	/**
	 * Updates the Student Pilot associated with this flight.
	 * @param id the Pilot's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see InstructionFlight#getPilotID()
	 * @see InstructionFlight#setInstructorID(int)
	 */
	public void setPilotID(int id) {
		validateID(_pilotID, id);
		_pilotID = id;
	}
	
	/**
	 * Updates the the Flight Academy course associated with this flight.
	 * @param name the Course's name
	 * @see InstructionFlight#getCourseName()
	 * @see InstructionFlight#setCourseID(int)
	 */
	public void setCourseName(String name) {
		_courseName = name;
	}

	/**
	 * Updates this flight's comments.
	 * @param comments the updated comments
	 * @see InstructionFlight#getComments()
	 */
	public void setComments(String comments) {
		_comments = comments;
	}

	/**
	 * Updates the date this flight was flown.
	 * @param dt the start date/time of this flight
	 * @see InstructionFlight#getDate()
	 */
	public void setDate(Date dt) {
		_startDate = dt;
	}

	/**
	 * Sets the length of this Flight, in <i>hours multiplied by 10</i>.
	 * @param length the length of the flight, in <i>hours multiplied by 10</i>
	 * @see InstructionFlight#getLength()
	 */
	public void setLength(int length) {
		if ((length < 0) || (length > 180))
			length = 180;

		_length = length;
	}
}