// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.schedule.Airport;

/**
 * A bean to store SID/STAR data.
 * @author Luke
 * @version 2.0
 * @since 1.0
 */

public class TerminalRoute extends Airway {
   
   public static final int SID = 0;
   public static final int STAR = 1;
   
   private static final String[] TYPES = {"SID", "STAR"};
   
   private String _airport;
   private int _type;
   
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
      super(name);
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
   
   /**
    * Updates the Route name.
    * @param name the name
    * @throws NullPointerException if name is null
    */
   public void setName(String name) {
      _name = name.toUpperCase();
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
	   setCode(_name + "." + _transition);
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
}