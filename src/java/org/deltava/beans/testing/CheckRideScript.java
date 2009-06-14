// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

/**
 * A bean to store default Check Ride descriptions.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class CheckRideScript implements java.io.Serializable, Comparable<CheckRideScript> {

   private String _eqType;
   private String _programName;
   private String _desc;
   
   /**
    * Creates a new Check Ride script.
    * @param eqType the equipment type
    * @throws NullPointerException if eqType is null
    * @see CheckRideScript#setEquipmentType(String)
    */
   public CheckRideScript(String eqType) {
      super();
      setEquipmentType(eqType);
   }
   
   /**
    * Returns the equipment type for this script.
    * @return the equipment type
    * @see CheckRideScript#setEquipmentType(String)
    */
   public String getEquipmentType() {
      return _eqType;
   }
   
   /**
    * Returns the Equipment Program for this check ride script.
    * @return the equipment program name
    * @see CheckRideScript#setProgram(String)
    */
   public String getProgram() {
      return _programName;
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
    * Sets the equipment program for this check ride script.
    * @param eqType the equipment program name
    * @see CheckRideScript#getProgram()
    */
   public void setProgram(String eqType) {
      _programName = eqType;
   }
   
   /**
    * Sets the aircraft type for this check ride script.
    * @param eqType the aircraft type
    * @throws NullPointerException if eqType is null
    * @see CheckRideScript#getEquipmentType()
    */
   public void setEquipmentType(String eqType) {
	   _eqType = eqType.trim();
   }
   
   /**
    * Compares two check ride scripts by comparing their equipment types.
    */
   public int compareTo(CheckRideScript cs2) {
	   return _eqType.compareTo(cs2._eqType);
   }
}