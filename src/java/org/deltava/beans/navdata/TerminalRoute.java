// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.schedule.Airport;

/**
 * A bean to store SID/STAR data.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class TerminalRoute extends Airway implements ComboAlias {
   
   public static final int SID = 0;
   public static final int STAR = 1;
   
   public static final String[] TYPES = {"SID", "STAR"};
   
   private String _airport;
   private int _type;
   private boolean _canPurge;
   
   private String _name;
   private String _transition;
   private String _runway;
   
   /**
    * Creates a new Terminal Route.
    * @param icao the Airport ICAO code
    * @param name the route name
    * @param type the SID/STAR type
    * @throws NullPointerException if icao, name or transition are null
    * @throws IllegalArgumentException if type is invalid
    * @see TerminalRoute#setType(int)
    */
   public TerminalRoute(String icao, String name, int type) {
      super(name, 0);
      _airport = icao.trim().toUpperCase();
      setName(name);
      setType(type);
   }
   
   /**
    * Creates a new Terminal Route.
    * @param a the Airport
    * @param name the route name
    * @param type the type
    */
   public TerminalRoute(Airport a, String name, int type) {
	   this(a.getICAO(), name, type);
   }
   
   /**
    * Returns the Airport's ICAO code.
    * @return the ICAO code
    */
   public String getICAO() {
      return _airport;
   }
   
   /**
    * Returns the SID/STAR name.
    * @return the name
    */
   public String getName() {
      return _name;
   }
   
   /**
    * Returns whether this route can be purged from the database.
    * @return TRUE if it can be purged, otherwise FALSE
    */
   public boolean getCanPurge() {
	   return _canPurge;
   }
   
   /**
    * Returns the name of the transition waypoint.
    * @return the transition waypoint
    */
   public String getTransition() {
      return _transition;
   }
   
   /**
    * Returns the runway for this Terminal Route.
    * @return the runway name, or ALL
    */
   public String getRunway() {
      return _runway;
   }
   
   /**
    * Returns the route type (SID/STAR).
    * @return the route type
    */
   public int getType() {
      return _type;
   }
   
   /**
    * Returns the route type name.
    * @return the route type name
    */
   public String getTypeName() {
      return TYPES[_type];
   }
   
   public String getComboName() {
	   return getCode();
   }
   
   public String getComboAlias() {
	   return getCode();
   }
   
   /**
    * Returns the waypoints to a partial transition point.
    * @param transition the transition waypoint code
    * @return a Collection of NavigationDataBeans
    */
   public Collection<NavigationDataBean> getWaypoints(String transition) {
	   if (_type == SID)
		   return getWaypoints(null, transition);
	   
	   return getWaypoints(transition, null);
   }
   
   /**
    * Returns the SID/STAR code.
    */
   @Override
   public String getCode() {
	   StringBuilder buf = new StringBuilder(_name);
	   buf.append('.');
	   buf.append(_transition);
	   if (!"ALL".equals(_runway)) {
		   buf.append('.');
		   buf.append(_runway);
	   }

	   return buf.toString();
   }
   
   /**
    * Updates the Route name.
    * @param name the name
    * @throws NullPointerException if name is null
    */
   public void setName(String name) {
      _name = name.toUpperCase();
   }
   
   /**
    * Updates whether this route can be purged from the database.
    * @param canPurge TRUE if the route can be purged, otherwise FALSE
    */
   public void setCanPurge(boolean canPurge) {
	   _canPurge = canPurge;
   }
   
   /**
    * Updates the runway associated with this Terminal Route.
    * @param runway the runway name
    */
   public void setRunway(String runway) {
      _runway = (runway == null) ? "ALL" : runway.trim().toUpperCase();
   }
   
   /**
    * Updates the transition waypoint for this Terminal Route.
    * @param waypoint the waypoint code
    * @throws NullPointerException if waypoint is null
    */
   public void setTransition(String waypoint) {
	   _transition = waypoint.trim().toUpperCase();
   }
   
   /**
    * Updates the Terminal Route type.
    * @param type the route type code
    * @throws IllegalArgumentException if type is negative or invalid
    */
   public void setType(int type) {
      if ((type < 0) || (type >= TYPES.length))
         throw new IllegalArgumentException("Invalid Terminal Route type - " + type);
      
      _type = type;
   }
   
   /**
    * Compares two terminal routes by comparing their names and transition waypoints.
    * @see Comparable#compareTo(Object)
    */
   public final int compareTo(Airway a2) {
      int tmpResult = super.compareTo(a2);
      if (tmpResult == 0) {
    	  TerminalRoute tr2 = (TerminalRoute) a2;
         tmpResult = _transition.compareTo(tr2.getTransition());
      }
      
      return tmpResult;
   }
   
   /**
    * Checks for equality by comparing names.
    */
   public boolean equals(Object o) {
      return (o instanceof TerminalRoute) ? (compareTo((TerminalRoute) o) == 0) : false;
   }
   
   /**
    * Returns the hash code.
    */
   public int hashCode() {
	   return toString().hashCode();
   }
   
   /**
    * Returns the name, transition and runway.
    */
   public String toString() {
	   StringBuilder buf = new StringBuilder(_airport);
	   buf.append('.');
	   buf.append(_name);
	   buf.append('.');
	   buf.append(_transition);
	   buf.append('.');
	   buf.append(_runway);
	   return buf.toString();
   }
}