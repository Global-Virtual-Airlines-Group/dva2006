// Copyright 2005, 2009, 2010, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.Auditable;

/**
 * A bean to store Check Ride scripts.
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public abstract class CheckRideScript implements java.io.Serializable, Auditable, Comparable<CheckRideScript> {

   protected String _programName;
   private String _desc;
   
   /**
    * Creates a new Check Ride script.
    * @param program the program name
    */
   public CheckRideScript(String program) {
      super();
      _programName = program;
   }
   
   /**
    * Returns the Check Ride description.
    * @return the description
    * @see CheckRideScript#setDescription(String)
    */
   public String getDescription() {
      return _desc;
   }
   
   /**
    * Updates the check ride description.
    * @param desc the description
    * @see CheckRideScript#getDescription()
    */
   public void setDescription(String desc) {
      _desc = desc;
   }
   
   /**
    * Compares two check ride scripts by comparing their equipment types.
    */
   @Override
   public int compareTo(CheckRideScript cs2) {
	   return _programName.compareTo(cs2._programName);
   }
}