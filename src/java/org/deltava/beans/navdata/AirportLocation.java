// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A class to store airport location data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportLocation extends NavigationDataBean {
   
   private int _altitude;

   /**
    * Creates a new Airport location object.
    * @param lat
    * @param lon
    */
   public AirportLocation(double lat, double lon) {
      super(AIRPORT, lat, lon);
   }

   /**
    * Returns the Airport's altitude.
    * @return the altitude in feet above mean sea level
    */
   public int getAltitude() {
      return _altitude;
   }
   
   /**
    * Updates the Airport's altitude.
    * @param alt the altitude in feet above mean sea level
    * @throws IllegalArgumentException if alt < -1500 or > 29000
    */
   public void setAltitude(int alt) {
      if ((alt < -1500) || (alt > 29000))
         throw new IllegalArgumentException("Altitude cannot be < -1500 or > 29000");
      
      _altitude = alt;
   }
}