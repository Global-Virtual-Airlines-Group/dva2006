// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.testing;

/**
 * A class to store Check Ride and Flight Video data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRide extends Test {
    
	private static final String[] CLASS_NAMES = {"opt1", "opt1", null};
	
	private String _comments;
	private int _acarsID;

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
     * Returns the Check Ride comments.
     * @return the comments
     * @see CheckRide#setComments(String)
     */
    public String getComments() {
    	return _comments;
    }
    
    /**
     * Returns the equipment type for the check ride, from the name.
     * @return the equipment type
     */
    public String getEquipmentType() {
    	String name = getName();
    	return name.substring(0, name.indexOf(' '));
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
     * Updates the Flight Video comments.
     * @param comments the comments
     * @see CheckRide#getComments()
     */
    public void setComments(String comments) {
    	_comments = comments;
    }
    
    /**
     * Sets the ACARS Flight ID for this check ride.
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
     * Returns the CSS class name for a view table row.
     * @return the CSS class name 
     */
    public String getRowClassName() {
 	   return CLASS_NAMES[getStatus()];
    }
}