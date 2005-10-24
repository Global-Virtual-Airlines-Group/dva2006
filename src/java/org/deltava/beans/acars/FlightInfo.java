// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.ViewEntry;

import org.deltava.beans.schedule.Airport;

/**
 * A bean to store ACARS Flight Information records.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightInfo extends DatabaseBean implements Comparable, ACARSLogEntry, ViewEntry {

   private long _conID;
   private int _pilotID;
   
   private Date _startTime;
   private Date _endTime;
   
   private String _flightCode;
   private String _eqType;
   private String _alt;
   
   private Airport _airportD;
   private Airport _airportA;
   
   private String _route;
   private String _remarks;
   
   private int _fsVersion;
   private boolean _offline;
   private boolean _hasPIREP;
   private boolean _archived;
   
   /**
    * Creates a new Flight Information record.
    * @param id the flight ID
    * @param conID the connection ID
    * @throws IllegalArgumentException if id or conID are zero or negative
    */
   public FlightInfo(int id, long conID) {
      super();
      setID(id);
      setConnectionID(conID);
   }
   
   /**
    * Returns the flight's ACARS connection ID.
    * @return the connection ID
    * @see FlightInfo#setConnectionID(int)
    * @see FlightInfo#getPilotID()
    */
   public long getConnectionID() {
      return _conID;
   }
   
   /**
    * Returns the flight's pilot ID.
    * @return the database ID of the pilot flying this flight
    * @see FlightInfo#setPilotID(int)
    * @see FlightInfo#getConnectionID()
    */
   public int getPilotID() {
      return _pilotID;
   }
   
   /**
    * Returns the flight's start time.
    * @return the date/time the flight started
    * @see FlightInfo#setStartTime(Date)
    * @see FlightInfo#getEndTime()
    */
   public Date getStartTime() {
      return _startTime;
   }
   
   /**
    * Returns the flight's end time. This may be null.
    * @return the date/time the flight ended
    * @see FlightInfo#setEndTime(Date)
    * @see FlightInfo#getStartTime()
    */
   public Date getEndTime() {
      return _endTime;
   }
   
   /**
    * Returns the flight code for this flight.
    * @return the flight code (eg. DVA123)
    * @see FlightInfo#setFlightCode(String)
    */
   public String getFlightCode() {
      return _flightCode;
   }
   
   /**
    * Returns the aircraft type for this flight.
    * @return the equipment code
    * @see FlightInfo#setEquipmentType(String)
    */
   public String getEquipmentType() {
      return _eqType;
   }
   
   /**
    * Returns the filed altitude for this flight.
    * @return the altitude in feet or a flight level
    * @see FlightInfo#setAltitude(String)
    */
   public String getAltitude() {
      return _alt;
   }
   
   /**
    * Returns the destination Airport for this flight.
    * @return the destination Airport bean
    * @see FlightInfo#setAirportA(Airport)
    * @see FlightInfo#getAirportD()
    */
   public Airport getAirportA() {
      return _airportA;
   }
   
   /**
    * Returns the origin Airport for this flight.
    * @return the origin Airport bean
    * @see FlightInfo#setAirportD(Airport)
    * @see FlightInfo#getAirportA()
    */
   public Airport getAirportD() {
      return _airportD;
   }
   
   /**
    * Returns the filed route for this flight.
    * @return the route
    * @see FlightInfo#setRoute(String)
    */
   public String getRoute() {
      return _route;
   }
   
   /**
    * Returns the pilot's remarks for this flight.
    * @return the remarks
    * @see FlightInfo#setRemarks(String)
    */
   public String getRemarks() {
      return _remarks;
   }
   
   /**
    * Returns the version of Flight Simulator used in this flight.
    * @return the flight simulator version code
    * @see FlightInfo#setFSVersion(int)
    */
   public int getFSVersion() {
      return _fsVersion;
   }
   
   /**
    * Returns if this flight was flown disconnected from the ACARS server.
    * @return TRUE if the flight was flown offline, otherwise FALSE
    * @see FlightInfo#setOffline(boolean)
    */
   public boolean getOffline() {
      return _offline;
   }
   
   /**
    * Returns if this flight has an associated Flight Report.
    * @return TRUE if a Flight Report was filed, otherwise FALSE
    * @see FlightInfo#setHasPIREP(boolean)
    */
   public boolean getHasPIREP() {
	   return _hasPIREP;
   }
   
   /**
    * Returns if this flight's position data is stored in the archive.
    * @return TRUE if the Position data is in the archive, otherwise FALSE
    * @see FlightInfo#setArchived(boolean)
    */
   public boolean getArchived() {
      return _archived;
   }
   
   /**
    * Updates the ACARS Connection ID used for this flight.
    * @param id the connection ID
    * @throws IllegalArgumentException if id is zero or negative
    * @see FlightInfo#getConnectionID()
    */
   public void setConnectionID(long id) {
      if (id < 0)
         throw new IllegalArgumentException("Invalid connection ID - " + id);
      
      _conID = id;
   }
   
   /**
    * Updates the Pilot ID for the flight.
    * @param id the database ID of the pilot flying this flight
    * @throws IllegalArgumentException if id is negative
    * @see FlightInfo#getPilotID()
    */
   public void setPilotID(int id) {
      if (id < 0)
         throw new IllegalArgumentException("Invalid pilot ID - " + id);
      
      _pilotID = id;
   }
   
   /**
    * Updates wether this flight was flown disconnected from the ACARS server.
    * @param offline TRUE if the flight was flown offline, otherwise FALSE
    * @see FlightInfo#getOffline()
    */
   public void setOffline(boolean offline) {
      _offline = offline;
   }
   
   /**
    * Updates wether this flight has an associated Flight Report.
    * @param hasPIREP TRUE if a Flight Report was filed, otherwise FALSE
    * @see FlightInfo#getHasPIREP()
    */
   public void setHasPIREP(boolean hasPIREP) {
	   _hasPIREP = hasPIREP;
   }
   
   /**
    * Marks this Flight's position data as archived.
    * @param archived TRUE if the position data is in the archive, otherwise FALSE
    * @see FlightInfo#getArchived()
    */
   public void setArchived(boolean archived) {
      _archived = archived;
   }
   
   /**
    * Updates the start date/time for this flight.
    * @param dt the date/time the flight started
    * @see FlightInfo#getStartTime()
    * @see FlightInfo#setEndTime(Date)
    */
   public void setStartTime(Date dt) {
      _startTime = dt;
   }
   
   /**
    * Updates the end date/time for this flight.
    * @param dt the date/time the flight ended
    * @see FlightInfo#getEndTime()
    * @see FlightInfo#setStartTime(Date)
    */
   public void setEndTime(Date dt) {
      _endTime = ((dt != null) && (dt.before(_startTime))) ? _startTime : dt;
   }
   
   /**
    * Updates the destination Airport for this flight.
    * @param a an Airport bean
    * @see FlightInfo#getAirportA()
    * @see FlightInfo#setAirportD(Airport)
    */
   public void setAirportA(Airport a) {
      _airportA = a;
   }
   
   /**
    * Updates the origination Airport for this flight.
    * @param a an Airport bean
    * @see FlightInfo#getAirportD()
    * @see FlightInfo#setAirportA(Airport)
    */
   public void setAirportD(Airport a) {
      _airportD = a;
   }
   
   /**
    * Updates the filed altitude for this flight. 
    * @param alt the altitude in feet or as a flight level
    * @see FlightInfo#getAltitude()
    */
   public void setAltitude(String alt) {
      _alt = alt;
   }
   
   /**
    * Updates the aircraft used on this flight.
    * @param eq the equipment type
    * @see FlightInfo#getEquipmentType()
    */
   public void setEquipmentType(String eq) {
      _eqType = eq;
   }
   
   /**
    * Updates the Flight Simulator version used in this flight.
    * @param ver the Flight Simulator version
    * @throws IllegalArgumentException if ver is negative or > 2006
    * @see FlightInfo#getFSVersion()
    */
   public void setFSVersion(int ver) {
      if ((ver < 0) || (ver > 2006))
         throw new IllegalArgumentException("Invalid FS Version - " + ver);
      
      _fsVersion = ver;
   }
   
   /**
    * Updates the flight number for this flight.
    * @param code the flight code
    * @throws NullPointerException if code is null
    * @see FlightInfo#getFlightCode()
    */
   public void setFlightCode(String code) {
      _flightCode = code.toUpperCase();
   }
   
   /**
    * Updates the filed route for this flight. 
    * @param route the filed route
    * @see FlightInfo#getRoute()
    */
   public void setRoute(String route) {
      _route = route.replaceAll("[.]+", " ");
   }
   
   /**
    * Updates the pilot's remarks for this flight. 
    * @param remarks the pilot's remarks
    * @see FlightInfo#getRemarks()
    */
   public void setRemarks(String remarks) {
      _remarks = remarks;
   }
   
   /**
    * Compares two flights by comparing their start date/times.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      FlightInfo i2 = (FlightInfo) o2;
      return _startTime.compareTo(i2.getStartTime());
   }
   
   /**
    * Displays the CSS class name for this table row.
    * @return the CSS class name
    */
   public String getRowClassName() {
	   return ((_endTime != null) && !_hasPIREP) ? "warn" : null;
   }
}