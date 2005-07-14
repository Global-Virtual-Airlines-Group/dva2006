// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A bean to store Non-Directional Beacon information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NDB extends NavigationDataBean {
   
   private String _freq;

   /**
    * Creates a new NDB object.
    * @param lat the latitude in degrees
    * @param lon the longitude in degrees
    */
   public NDB(double lat, double lon) {
      super(NDB, lat, lon);
   }
   
   /**
    * Returns the NDB's frequency.
    * @return the frequency
    */
   public String getFrequency() {
      return _freq;
   }

   /**
    * Updates the NDB's frequency.
    * @param freq the frequency
    */
   public void setFrequency(String freq) {
      _freq = freq;
   }
}