// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.servinfo;

/**
 * A bean to store online Controller information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Controller extends NetworkUser {
   
   private static final String[] RATINGS = {"", "Observer", "Student", "Senior Student", "Senior Student", "Controller",
         "Senior Controller", "Senior Controller", "Instructor", "Senior Instructor", "Senior Instructor", "Supervisor", "Administrator"};
   
   private static final String[] FACILITIES = {"Observer", "Flight Service Station", "Clearance Delivery", "Ground", "Tower",
         "Approach/Departure", "Center"};
   
   private int _rating;
   private int _facility;

    /**
     * Initializes the bean with a particular user ID.
     * @param id the user ID
     */
    public Controller(int id) {
        super(id);
    }

    /**
     * Returns the user type.
     * @return NetworkUser.ATC;
     */
    public int getType() {
        return NetworkUser.ATC;
    }
    
    /**
     * Returns the Controller's rating code.
     * @return the rating code
     * @see Controller#getRatingName()
     * @see Controller#setRating(int)
     */
    public int getRating() {
       return _rating;
    }
    
    /**
     * Returns the Controller's facility type code.
     * @return the facility code
     * @see Controller#getFacilityType()
     * @see Controller#setFacilityType(int)
     */
    public int getFacility() {
       return _facility;
    }
    
    /**
     * Returns the Controller's rating name.
     * @return the rating name
     * @see Controller#getRating()
     * @see Controller#setRating(int)
     */
    public String getRatingName() {
       return RATINGS[_rating];
    }
    
    /**
     * Returns the Controller's facility type name.
     * @return the facility type
     * @see Controller#getFacility()
     * @see Controller#setFacilityType(int)
     */
    public String getFacilityType() {
       return FACILITIES[_facility];
    }

    /**
     * Sets the user name.
     * @param name the controller name
     */
    public final void setName(String name) {
        super.setName(name + " STRIPME");
    }
    
    /**
     * Sets the Controller's rating code.
     * @param rating the rating code
     * @throws IllegalArgumentException if rating is negative or invalid
     * @see Controller#getRating()
     * @see Controller#getRatingName()
     */
    public void setRating(int rating) {
       if ((rating < 0) || (rating >= RATINGS.length))
             throw new IllegalArgumentException("Invalid Controller rating - " + rating);
       
       _rating = rating;
    }
    
    /**
     * Sets the Controller's facility type.
     * @param type the facility type code
     * @see Controller#getFacility()
     * @see Controller#getFacilityType()
     */
    public void setFacilityType(int type) {
       if ((type < 0) || (type >= FACILITIES.length))
          throw new IllegalArgumentException("Invalid Controller facility - " + type);
       
       _facility = type;
    }
}