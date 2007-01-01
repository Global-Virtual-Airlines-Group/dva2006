// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A bean to store VOR information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class VOR extends NavigationFrequencyBean {
   
   /**
    * Creates a new VOR object.
    * @param lat the latitude in degrees
    * @param lon the longitude in degrees
    */
   public VOR(double lat, double lon) {
      super(VOR, lat, lon);
   }
   
   /**
    * Return the default Google Maps icon color.
    * @return org.deltava.beans.MapEntry.BLUE
    */
   public String getIconColor() {
      return BLUE;
   }
}