// Copyright 2005, 2006, 2012, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.time.Instant;

import org.deltava.beans.EquipmentType;

import org.deltava.util.StringUtils;

/**
 * A class to store Check Ride data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CheckRide extends Test {
    
	private static final String[] CLASS_NAMES = {"opt2", "opt1", null};
	
	private RideType _type = RideType.CHECKRIDE;
	
	private String _eqType;
	private String _acType;
	private Instant _expiryDate;
	
	private int _acarsID;
	private int _courseID;
	private int _idx;

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
    @Override
    public final int getScore() {
    	return getPassFail() ? 1 : 0;
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
     * Returns the expiration of this check ride.
     * @return the expiry date/time, or null if never
     * @see CheckRide#setExpirationDate(Instant)
     */
    public Instant getExpirationDate() {
    	return _expiryDate;
    }
    
    /**
     * Returns the size of the Test. <i>Not Implemented</i>
     * @return 1
     */
    @Override
    public int getSize() {
       return 1;
    }
    
    /**
     * Returns the Check Ride index for a Flight Academy course.
     * @return the Check Ride index
     */
    public int getIndex() {
    	return _idx;
    }
    
    /**
     * Returns the Check Ride type.
     * @return the RideType
     * @see CheckRide#setType(RideType)
     */
    public RideType getType() {
    	return _type;
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
    @Override
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
    @Override
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
     * Sets the expiration date for this Check Ride.
     * @param dt the expiration date/time, or null if never
     * @see CheckRide#getExpirationDate()
     */
    public void setExpirationDate(Instant dt) {
    	_expiryDate = dt;
    }
    
    /**
     * Sets the Check Ride Type.
     * @param rt the RideType
     * @see CheckRide#getType()
     */
    public void setType(RideType rt) {
    	_type = rt;
    }
    
    /**
     * Marks this Test as part of the Flight Academy.
     * @param academy TRUE if the Test is part of the Flight Academy, otherwise FALSE
     * @see Test#getAcademy()
     */
    @Override
    public void setAcademy(boolean academy) {
    	super.setAcademy(academy);
    	if (academy) {
    		String n = getName();
    		int ofs = n.lastIndexOf(' ');
    		_idx = (ofs < 0) ? 1 : StringUtils.parse(n.substring(ofs + 1), 1);
    	}
    }
    
    /**
     * Returns the CSS class name for a view table row.
     * @return the CSS class name 
     */
    @Override
    public String getRowClassName() {
 	   return CLASS_NAMES[getStatus().ordinal()];
    }
}