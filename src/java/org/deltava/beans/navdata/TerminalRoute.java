// Copyright 2005, 2007, 2008, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.StringUtils;

/**
 * A bean to store SID/STAR data.
 * @author Luke
 * @version 5.1
 * @since 1.0
 */

public class TerminalRoute extends Airway implements ComboAlias {
   
	/**
	 * Terminal Route types.
	 */
	public enum Type {
		SID, STAR
	}
	
   private final String _airport;
   private final Type _type;
   private boolean _canPurge;
   
   private String _name;
   private String _transition;
   private String _runway;
   
   /**
    * Checks if a route waypoint is really a SID/STAR name.
    * @param name the waypoint code
    * @return TRUE if a SID/STAR, otherwise FALSE
    */
   public static boolean isNameValid(String name) {
	   if (StringUtils.isEmpty(name) || (name.length() < 4))
		   return false;

	   char sLast = name.charAt(name.length() - 2);
	   char last = name.charAt(name.length() - 1);
	   return Character.isDigit(last) || (Character.isDigit(sLast) && Character.isLetter(last));
   }
   
   /**
    * Utlity method to convert the SID/STAR name to a generic name using a percent sign
    * so that mySQL can select any revision of the procedure.
    * @param name the SID/STAR name
    * @return a genericized name with the digit replaced by a percent sign
    */
   public static String makeGeneric(String name) {
	   if (!isNameValid(name))
		   return name;
	   
	   StringBuilder buf = new StringBuilder();
	   for (int x = 0; x < name.length(); x++) {
		   char c = name.charAt(x);
		   buf.append(Character.isDigit(c) ? '%' : c);
	   }
	   
	   return buf.toString();
   }
   
   /**
    * Creates a new Terminal Route.
    * @param icao the Airport ICAO code
    * @param name the route name
    * @param type the SID/STAR type
    * @throws NullPointerException if icao, name or transition are null
    */
   public TerminalRoute(String icao, String name, Type type) {
      super(name, 0);
      _airport = icao.trim().toUpperCase();
      _type = type;
      setName(name);
   }
   
   /**
    * Creates a new Terminal Route.
    * @param a the Airport
    * @param name the route name
    * @param type the type
    */
   public TerminalRoute(Airport a, String name, Type type) {
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
   public Type getType() {
      return _type;
   }
   
   @Override
   public String getComboName() {
	   return getCode();
   }
   
   @Override
   public String getComboAlias() {
	   return getCode();
   }
   
   /**
    * Returns the waypoints to a partial transition point.
    * @param transition the transition waypoint code
    * @return a Collection of NavigationDataBeans
    */
   public Collection<NavigationDataBean> getWaypoints(String transition) {
	   if (_type == Type.SID)
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
    * Updates the internal ID used to map Terminal Routes to waypoints.
    * @param seq the sequence ID
    */
   public void setSequence(int seq) {
	   _awseq = Math.max(0, seq);
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
    * Compares two terminal routes by comparing their names and transition waypoints.
    * @see Comparable#compareTo(Object)
    */
   @Override
   public final int compareTo(Airway a2) {
      int tmpResult = super.compareTo(a2);
      if (tmpResult == 0) {
    	  TerminalRoute tr2 = (TerminalRoute) a2;
    	  tmpResult = _transition.compareTo(tr2.getTransition());
    	  if (tmpResult == 0)
    		  tmpResult = _runway.compareTo(tr2._runway);
      }
      
      return tmpResult;
   }
   
   /**
    * Checks for equality by comparing names.
    */
   @Override
   public boolean equals(Object o) {
      return (o instanceof TerminalRoute) ? (compareTo((TerminalRoute) o) == 0) : false;
   }
   
   /**
    * Returns the hash code.
    */
   @Override
   public int hashCode() {
	   return toString().hashCode();
   }
   
   /**
    * Returns the name, transition and runway.
    */
   @Override
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