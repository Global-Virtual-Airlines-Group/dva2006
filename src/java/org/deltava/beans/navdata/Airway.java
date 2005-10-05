// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

/**
 * A bean to store Airway names and waypoints.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Airway implements java.io.Serializable, Comparable {

   private String _code;
   private List _waypoints;
   
   /**
    * Creates a new Airway bean.
    * @param code the airway code
    * @throws NullPointerException if code is null
    */
   public Airway(String code) {
      super();
      _code = code.trim().toUpperCase();
      _waypoints = new ArrayList();
   }
   
   /**
    * Creates a new Airway bean with a route.
    * @param code the airway code
    * @param route a space-delimited list of waypoints
    * @throws NullPointerException if code or route are null
    * @see Airway#setRoute(String)
    */
   public Airway(String code, String route) {
      this(code);
      setRoute(route);
   }
   
   /**
    * Returns the airway code.
    * @return the code
    */
   public String getCode() {
      return _code;
   }
   
   /**
    * Returns the number of waypoints in the airway.
    * @return the number of waypoints
    */
   public int getSize() {
      return _waypoints.size();
   }
   
   /**
    * Returns the waypoints for this Airway. <i>The returned collection is mutable.</i>
    * @return a List of waypoint codes
    * @see Airway#getRoute()
    * @see Airway#addWaypoint(String)
    * @see Airway#setRoute(String)
    */
   public Collection getWaypoints() {
      return new ArrayList(_waypoints);
   }
   
   
   public String getRoute() {
      StringBuffer buf = new StringBuffer();
      for (Iterator i = _waypoints.iterator(); i.hasNext(); ) {
         buf.append((String) i.next());
         if (i.hasNext())
            buf.append(' ');
      }
      
      return buf.toString();
   }
   
   /**
    * Adds a waypoint to the Airway route.
    * @param code the waypoint code
    * @see Airway#getWaypoints()
    */
   public void addWaypoint(String code) {
      if (code == null)
         return;
      
      code = code.trim().toUpperCase();
      if (!_waypoints.contains(code))
         _waypoints.add(code);
   }
   
   /**
    * Updates the Airway route. <i>The existing route will be cleared</i>.
    * @param route a space-delimited list of waypoint codes 
    */
   public void setRoute(String route) {
      _waypoints.clear();
      StringTokenizer tkns = new StringTokenizer(route, " ");
      while (tkns.hasMoreTokens())
         addWaypoint(tkns.nextToken());
   }

   /**
    * Compares two airways by comparing their names.
    */
   public int compareTo(Object o) {
      Airway a2 = (Airway) o;
      return _code.compareTo(a2.getCode());
   }

   /**
    * Returns the airway code.
    */
   public String toString() {
      return getCode();
   }
}