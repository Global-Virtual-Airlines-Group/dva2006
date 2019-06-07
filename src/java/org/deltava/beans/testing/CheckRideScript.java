// Copyright 2005, 2009, 2010, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.deltava.beans.*;

/**
 * A bean to store Check Ride scripts.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public abstract class CheckRideScript implements java.io.Serializable, Auditable, Comparable<CheckRideScript> {

   protected String _programName;
   private String _desc;
   private final Collection<Simulator> _sims = new TreeSet<Simulator>();
   
   /**
    * Creates a new Check Ride script.
    * @param program the program name
    */
   public CheckRideScript(String program) {
      super();
      _programName = program;
   }
   
   /**
    * Returns the Simulators that can be used with this Check Ride.
    * @return a Collection of Simulators
    */
   public Collection<Simulator> getSimulators() {
	   return _sims;
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
    * Adds a Simulator to the list of available Simulators for this Check Ride.
    * @param s a Simulator
    */
   public void addSimulator(Simulator s) {
	   _sims.add(s);
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