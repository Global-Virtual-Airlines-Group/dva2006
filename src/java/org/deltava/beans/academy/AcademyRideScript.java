// Copyright 2005, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import org.deltava.beans.testing.CheckRideScript;

/**
 * A bean to store Flight Academy Check Ride scripts.
 * @author Luke
 * @version 3.4
 * @since 1.0
 */

public class AcademyRideScript extends CheckRideScript {

   /**
    * Creates a new Check Ride script.
    * @param certName the certification name
    * @throws NullPointerException if certName is null
    */
   public AcademyRideScript(String certName) {
      super(certName);
   }
   
   /**
    * Returns the Equipment Program for this check ride script.
    * @return the equipment program name
    */
   public String getCertificationName() {
      return _programName;
   }
}