// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.EquipmentType;

/**
 * A class to store Check Ride data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRide extends Test {
    
	private static final String[] CLASS_NAMES = {"opt1", "opt1", null};
	
	private String _eqType;
	private String _acType;
	
	private int _acarsID;
	private int _courseID;

    /**
     * Creates a new Check Ride/Video.
     * @param name the name of the checkride
     * @see Test#getName()
     */
    public CheckRide(String name) {
        super(name);
    }
    
    /**
     * Returns the ACARS Flight ID for this checkride.
     * @return the ACARS Flight ID
     * @see CheckRide#setFlightID(int)
     */
    public int getFlightID() {
       return _acarsID;
    }
    
    /**
     * Returns the Flight Academy Course this CheckRide is associated with.
     * @return the Course's database ID, or zero if no Course
     * @see CheckRide#setCourseID(int)
     */
    public int getCourseID() {
    	return _courseID;
    }
    
    /**
     * Returns the score for this Check Ride.
     * @return 1 if passed, otherwise 0
     * @see CheckRide#setScore(boolean)
     */
    public final int getScore() {
    	return getPassFail() ? 1 : 0;
    }

    /**
     * Returns the type of Test.
     * @return Test.CHECKRIDE
     */
    public int getType() {
        return Test.CHECKRIDE;
    }
    
    /**
     * Returns the aircraft type used in this Check Ride.
     * @return the aircraft type
     * @see CheckRide#setAircraftType(String)
     */
    public String getAircraftType() {
    	return _acType;
    }
    
    /**
     * Returns the equipment type for the check ride.
     * @return the equipment type
     * @see CheckRide#setEquipmentType(String)
     * @see CheckRide#setEquipmentType(EquipmentType)
     */
    public String getEquipmentType() {
    	return _eqType;
    }
    
    /**
     * Returns the size of the Test. <i>Not Implemented</i>
     * @return 1
     * @see CheckRide#setSize(int)
     */
    public int getSize() {
       return 1;
    }
    
    /**
     * Sets the ACARS Flight ID for this Check Ride.
     * @param id the ACARS Flight ID
     * @throws IllegalArgumentException if id is negative
     * @see CheckRide#getFlightID()
     */
    public void setFlightID(int id) {
       if (id != 0)
          validateID(_acarsID, id);
       
       _acarsID = id;
    }

    /**
     * Updates the Flight Academy Course ID for this Check Ride.
     * @param id the Flight Academy Course database ID
     * @throws IllegalStateException if this is not a Flight Academy Check Ride
     * @throws IllegalArgumentException if id is negative
     * @see CheckRide#getCourseID()
     * @see Test#getAcademy()
     */
    public void setCourseID(int id) {
    	if (!getAcademy() && (id != 0))
    		throw new IllegalStateException("Not a Flight Academy Check Ride");
    	else if (id != 0)
    		validateID(_courseID, id);
    	
    	_courseID = id;
    }
    
    /**
     * Sets the size of the Test. <i>NOT IMPLEMENTED</i>
     * @throws UnsupportedOperationException
     */
    public void setSize(int size) {
       throw new UnsupportedOperationException();
    }
    
    /**
     * Sets the score for the Check Ride.
     * @param passFail TRUE if a pass (1), otherwise FALSE (0) for a fail
     * @see CheckRide#setScore(int)
     * @see Test#setPassFail(boolean)
     */
    public void setScore(boolean passFail) {
        setScore((passFail) ? 1 : 0);
        setPassFail(passFail);
    }
    
    /**
     * Sets the score for the Check ride.
     * @param score the score, either 0 or 1 for pass or fail
     * @throws IllegalArgumentException if the score is not 0 or 1
     * @see CheckRide#setScore(boolean)
     */
    public final void setScore(int score) {
        if ((score != 0) && (score != 1))
            throw new IllegalArgumentException("Score must be 0 or 1");
        
        _score = score;
    }
    
    /**
     * Sets the aircraft type used for this Check Ride.
     * @param acType the aircraft type
     * @see CheckRide#getAircraftType()
     */
    public void setAircraftType(String acType) {
    	_acType = acType;
    }
    
    /**
     * Sets the equipment program for this Check Ride.
     * @param eqType the equipment program.
     * @see CheckRide#getEquipmentType()
     * @see CheckRide#setEquipmentType(EquipmentType)
     */
    public void setEquipmentType(String eqType) {
    	_eqType = eqType;
    }
    
    /**
     * Sets the equipment program and stage for this Check Ride.
     * @param eq the Equipment Program bean
     * @see CheckRide#setEquipmentType(String)
     * @see Test#setStage(int)
     */
    public void setEquipmentType(EquipmentType eq) {
    	setEquipmentType(eq.getName());
    	setStage(eq.getStage());
    }
    
    /**
     * Returns the CSS class name for a view table row.
     * @return the CSS class name 
     */
    public String getRowClassName() {
 	   return CLASS_NAMES[getStatus()];
    }
}