// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.navdata.*;

/**
 * A JSP function library to store Navigation Data functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NavDataFunctions {

   /**
    * Queries if the Navigation Database entry is an Airport.
    * @param nv the Navigation Data bean
    * @return TRUE if nv is an AirportLocation, otherwise FALSE
    */
   public static boolean isAirport(NavigationDataBean nv) {
      return (nv instanceof AirportLocation);
   }
   
   /**
    * Queries if the Navigation Database entry is a VOR.
    * @param nv the Navigation Data bean
    * @return TRUE if nv is a VOR, otherwise FALSE
    */
   public static boolean isVOR(NavigationDataBean nv) {
      return (nv instanceof VOR);
   }
   
   /**
    * Queries if the Navigation Database entry is an NDB.
    * @param nv the Navigation Data bean
    * @return TRUE if nv is an NDB, otherwise FALSE
    */
   public static boolean isNDB(NavigationDataBean nv) {
      return (nv instanceof NDB);
   }

   /**
    * Queries if the Navigation Database entry is an Intersection.
    * @param nv the Navigation Data bean
    * @return TRUE if nv is an Intersection, otherwise FALSE
    */
   public static boolean isIntersection(NavigationDataBean nv) {
      return (nv instanceof Intersection);
   }
   
   /**
    * Queries if the Navigation Database entry is a Runway.
    * @param nv the Navigation Data bean
    * @return TRUE if nv is a Runway, otherwise FALSE
    */
   public static boolean isRunway(NavigationDataBean nv) {
      return (nv instanceof Runway);
   }
}