// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to define icon formatting for entries that can be displayed in Google Maps.
 * @author Luke
 * @version 2.6
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
    * Grey Google Maps icon, displayed as $static/img/maps/point_grey.png
    */
   public static final String GREY = "grey";
   
   /**
    * Brown Google Maps icon, displayed as $static/img/maps/point_brown.png
    */
   public static final String BROWN = "brown";
   
   /**
    * Returns the text to display in this marker's infobox if displayed in a Google Map.
    * @return the infobox HTML text, or null if no infobox to be displayed
    */
   public String getInfoBox();
   
   /**
    * All Google Maps icon colors.
    */
   public static final String[] COLORS = {BLUE, GREEN, ORANGE, PURPLE, RED, WHITE, YELLOW, GREY, BROWN};
   
   /**
    * Google Maps line colors.
    * @see org.deltava.util.color.GoogleMapsColor#GoogleMapsColor(String)
    */
   public static final String[] LINECOLORS = {"89-89-255", "98-217-98", "242-196-12", "236-82-236", "255-64-80",
	   "255-255-255", "242-242-97"};
}