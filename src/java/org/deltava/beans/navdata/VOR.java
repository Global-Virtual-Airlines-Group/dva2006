// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A bean to store VOR information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class VOR extends NavigationDataBean {
   
   private String _freq;

   /**
    * Creates a new VOR object.
    * @param lat the latitude in degrees
    * @param lon the longitude in degrees
    */
   public VOR(double lat, double lon) {
      super(VOR, lat, lon);
   }
   
   /**
    * Returns the VOR's frequency.
    * @return the frequency
    */
   public String getFrequency() {
      return _freq;
   }

   /**
    * Updates the VOR's frequency.
    * @param freq the frequency
    */
   public void setFrequency(String freq) {
      _freq = freq;
   }
   
   /**
    * Return the default Google Maps icon color.
    * @return org.deltava.beans.MapEntry.BLUE
    */
   public String getIconColor() {
      return BLUE;
   }
   
   /**
    * Returns the default Google Maps infobox text.
    * @return an HTML String
    */
   public String getInfoBox() {
      StringBuffer buf = new StringBuffer(getHTMLTitle());
      buf.append("Frequency: ");
      buf.append(_freq);
      buf.append("<br />");
      buf.append(getHTMLPosition());
      return buf.toString();
   }
}