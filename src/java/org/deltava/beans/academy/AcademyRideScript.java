// Copyright 2005, 2009, 2010, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import org.deltava.beans.testing.CheckRideScript;

/**
 * A bean to store Flight Academy Check Ride scripts.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class AcademyRideScript extends CheckRideScript {
	
	private final int _idx;
	
   /**
    * Creates a new Check Ride script.
    * @param certName the certification name
    * @param idx the script index, for courses with multiple check rides 
    * @throws NullPointerException if certName is null
    */
   public AcademyRideScript(String certName, int idx) {
      super(certName);
      _idx = Math.max(1, idx);
   }
   
   /**
    * Returns the Equipment Program for this check ride script.
    * @return the equipment program name
    */
   public String getCertificationName() {
      return _programName;
   }
   
   /**
    * Returns the Check Ride index.
    * @return the index
    */
   public int getIndex() {
	   return _idx;
   }
   
   /**
    * Returns the Check Ride ID.
    * @return the ID
    */
   public AcademyRideID getID() {
	   return new AcademyRideID(getCertificationName(), _idx);
   }
}