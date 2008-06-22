// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A bean to store VOR information.
 * @author Luke
 * @version 2.2
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
   
	/**
	 * Returns the Google Earth palette code.
	 * @return 4
	 */
	public int getPaletteCode() {
		return 4;
	}
	
	/**
	 * Returns the Google Earth icon code.
	 * @return 48
	 */
	public int getIconCode() {
		return 48;
	}
}