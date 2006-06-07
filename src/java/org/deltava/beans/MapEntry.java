// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to define icon formatting for entries that can be displayed in Google Maps.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface MapEntry extends GeoLocation {
   
   /**
    * Blue Google Maps icon, displayed as $static/img/maps/point_blue.png
    */
   public static final String BLUE = "blue";
   
   /**
    * Green Google Maps icon, displayed as $static/img/maps/point_green.png
    */
   public static final String GREEN = "green";
   
   /**
    * Orange Google Maps icon, displayed as $static/img/maps/point_orange.png
    */
   public static final String ORANGE = "orange";
   
   /**
    * Purple Google Maps icon, displayed as $static/img/maps/point_purple.png
    */
   public static final String PURPLE = "purple";
   
   /**
    * Red Google Maps icon, displayed as $static/img/maps/point_red.png
    */
   public static final String RED = "red";
   
   /**
    * White Google Maps icon, displayed as $static/img/maps/point_white.png
    */
   public static final String WHITE = "white";
   
   /**
    * Yellow Google Maps icon, displayed as $static/img/maps/point_yellow.png
    */
   public static final String YELLOW = "yellow";

   /**
    * Returns the icon color for this entry if displayed in a Google Map.
    * @return the icon color
    */
   public String getIconColor();
   
   /**
    * Returns the text to display in this marker's infobox if displayed in a Google Map.
    * @return the infobox HTML text, or null if no infobox to be displayed
    */
   public String getInfoBox();
   
   /**
    * All Google Maps icon colors.
    */
   public static final String[] COLORS = {BLUE, GREEN, ORANGE, PURPLE, RED, WHITE, YELLOW}; 
}