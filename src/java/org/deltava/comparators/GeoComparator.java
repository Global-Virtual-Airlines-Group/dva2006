// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.comparators;

import java.util.Comparator;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A comparator to sort geographic locations by their distance from a fixed point.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GeoComparator implements Comparator {
   
   private GeoPosition _point;

   /**
    * Creates a new GeoComparator, comparing distance from 0,0.
    */
   public GeoComparator() {
      super();
      _point = new GeoPosition(0, 0);
   }

   /**
    * Creates a new GeoComparator, comparing distance from an arbitrary location.
    * @param loc the location
    */
   public GeoComparator(GeoLocation loc) {
      super();
      _point = (loc == null) ? new GeoPosition(0, 0) : new GeoPosition(loc);
   }

   /**
    * Compares two GeoLocations by comparing their distance from a common point.
    * @see Comparator#compare(Object, Object)
    */
   public int compare(Object o1, Object o2) {
      
      // Convert to GeoLocations
      GeoLocation l1 = (GeoLocation) o1;
      GeoLocation l2 = (GeoLocation) o2;

      // Get the distances
      int d1 = _point.distanceTo(l1);
      int d2 = _point.distanceTo(l2);
      
      // Compare the distances
      return new Integer(d1).compareTo(new Integer(d2));
   }
}