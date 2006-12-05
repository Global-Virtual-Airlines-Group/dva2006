// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import java.io.Serializable;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.StringUtils;

/**
 * A bean to store common properties for Navigation Database objects.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class NavigationDataBean implements Comparable, Serializable, GeoLocation, MapEntry {

   /**
    * Object type names.
    */
   public static final String[] NAVTYPE_NAMES = { "Airport", "VOR", "NDB", "Intersection", "Runway"};
   
   public static final int AIRPORT = 0;
   public static final int VOR = 1;
   public static final int NDB = 2;
   public static final int INT = 3;
   public static final int RUNWAY = 4;

   private String _code;
   private String _name;
   private int _type;

   private GeoPosition _gp;

   /**
    * Creates a new Navigation Object.
    * @param type the object type code
    * @param lat the latitude in degrees
    * @param lon the longitude in degrees
    * @see NavigationDataBean#getLatitude()
    * @see NavigationDataBean#getLongitude()
    */
   public NavigationDataBean(int type, double lat, double lon) {
      super();
      setType(type);
      _gp = new GeoPosition(lat, lon);
   }
   
   /**
    * Returns the object's code.
    * @return the navigation object code
    * @see NavigationDataBean#setCode(String)
    */
   public String getCode() {
      return _code;
   }
   
   /**
    * Returns the object's name.
    * @return the name
    * @see NavigationDataBean#setName(String)
    */
   public String getName() {
      return _name;
   }

   /**
    * Returns the object's latitude.
    * @return the latitude in degrees
    * @see NavigationDataBean#getLongitude()
    * @see NavigationDataBean#getPosition()
    */
   public final double getLatitude() {
      return _gp.getLatitude();
   }

   /**
    * Returns the object's longitude.
    * @return the longitude in degrees
    * @see NavigationDataBean#getLatitude()
    * @see NavigationDataBean#getPosition()
    */
   public final double getLongitude() {
      return _gp.getLongitude();
   }

   /**
    * Returns the object's position.
    * @return the GeoPosition of this object
    * @see NavigationDataBean#getLatitude()
    * @see NavigationDataBean#getLongitude()
    */
   public GeoPosition getPosition() {
      return _gp;
   }
   
   /**
    * Returns the object's type.
    * @return the object type code
    * @see NavigationDataBean#getTypeName()
    * @see NavigationDataBean#setType(int)
    */
   public final int getType() {
      return _type;
   }

   /**
    * Returns the object's type name.
    * @return the object type name
    * @see NavigationDataBean#getType()
    * @see NavigationDataBean#setType(int)
    */
   public final String getTypeName() {
      return NAVTYPE_NAMES[getType()];
   }
   
   /**
    * Updates the object's code.
    * @param code the code
    * @throws NullPointerException if code is null
    * @see NavigationDataBean#getCode()
    */
   public void setCode(String code) {
      _code = code.trim().toUpperCase();
   }
   
   /**
    * Updates the object's name.
    * @param name the name
    * @throws NullPointerException if name is null
    * @see NavigationDataBean#setName(String)
    */
   public void setName(String name) {
      _name = name.trim();
   }

   /**
    * Updates the object's type.
    * @param type the object type code
    * @throws IllegalArgumentException if type is negative or invalid
    * @see NavigationDataBean#getType()
    * @see NavigationDataBean#getTypeName()
    */
   public final void setType(int type) {
      if ((type < 0) || (type >= NAVTYPE_NAMES.length))
            throw new IllegalArgumentException("Invalid object type - " + type);

      _type = type;
   }
   
   /**
    * Compares two objects by comparing their codes. If the codes are equal, then their distances from point 0,0
    * are compared.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      NavigationDataBean nb2 = (NavigationDataBean) o2;
      int tmpResult = _code.compareTo(nb2.getCode());
      if (tmpResult == 0) {
         GeoPosition gp = new GeoPosition(0, 0);
         int d1 = gp.distanceTo(this);
         int d2 = gp.distanceTo(nb2);
         tmpResult = new Integer(d1).compareTo(new Integer(d2));
      }
      
      return tmpResult;
   }
   
   /**
    * Helper method to return the item type and code for HTML infoboxes.
    * @return the HTML-formatted code and description
    * @see MapEntry#getInfoBox()
    */
   protected String getHTMLTitle() {
      StringBuilder buf = new StringBuilder("<b>");
      buf.append(getCode());
      buf.append("</b> (");
      buf.append(getTypeName());
      buf.append(")<br /><br />");
      return buf.toString();
   }
   
   /**
    * Helper method to return the Latitude/Longitude for HTML infoboxes.
    * @return the HTML-formatted latitude/longitude
    * @see MapEntry#getInfoBox()
    */
   protected String getHTMLPosition() {
      StringBuilder buf = new StringBuilder("Latitude: ");
      buf.append(StringUtils.format(_gp, true, GeoLocation.LATITUDE));
      buf.append("<br />Longitude: ");
      buf.append(StringUtils.format(_gp, true, GeoLocation.LONGITUDE));
      buf.append("<br />");
      return buf.toString();
   }
   
   /**
    * Returns the code's hash code.
    */
   public int hashCode() {
      return _code.hashCode();
   }
   
   /**
    * Checks for equality by comparing the codes. 
    */
   public boolean equals(Object o2) {
      return (o2 instanceof NavigationDataBean) ? (compareTo(o2) == 0) : false;
   }
   
   /**
    * Returns the object's code to use as a cache key.
    */
   public Object cacheKey() {
      return getCode();
   }
}