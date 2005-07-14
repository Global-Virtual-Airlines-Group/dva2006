// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.servinfo;

import org.deltava.beans.schedule.Airport;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A bean to store online pilot information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Pilot extends NetworkUser {

    private GeoPosition _position;
    private int _altitude;
    private int _gSpeed;
    private String _eqCode;
    private Airport _airportD;
    private Airport _airportA;
    private String _comments;
    
    private String _rawData;
    
    /**
     * Initializes the bean with a given user ID.
     * @param id the user ID
     */
    public Pilot(int id) {
        super(id);
    }

    /**
     * Returns the destination Airport from the Flight Plan.
     * @return the destination Airport
     * @see Pilot#setAirportA(Airport)
     */
    public Airport getAirportA() {
        return _airportA;
    }
    
    /**
     * Returns the origin Airport from the Flight Plan.
     * @return the origin Airport
     * @see Pilot#setAirportD(Airport)
     */
    public Airport getAirportD() {
        return _airportD;
    }
    
    /**
     * Returns the current altitude.
     * @return the altitude in feet above Mean Sea Level
     * @see Pilot#setAltitude(int)
     * @see Pilot#setAltitude(String)
     */
    public int getAltitude() {
        return _altitude;
    }
    
    /**
     * Returns the User's Flight Plan comments.
     * @return the comments
     * @see Pilot#setComments(String)
     */
    public String getComments() {
        return _comments;
    }
    
    /**
     * Returns the User's equipment code.
     * @return the equipment code
     * @see Pilot#setEquipmentCode(String)
     */
    public String getEquipmentCode() {
        return _eqCode;
    }
    
    /**
     * Returns the User's ground speed.
     * @return the ground speed in knots
     * @see Pilot#setGroundSpeed(int)
     * @see Pilot#setGroundSpeed(String)
     */
    public int getGroundSpeed() {
        return _gSpeed;
    }
    
    /**
     * Returns the raw data from the FSD feed. This is used when aggregating this information into a
     * combined ServInfo data feed.
     * @return the raw data
     * @see Pilot#setRawData(String)
     */
    public String getRawData() {
       return _rawData;
    }
    
    /**
     * Returns the User type.
     * @return NetworkUser.PILOT
     */
    public int getType() {
        return NetworkUser.PILOT;
    }
    
    /**
     * Returns the User's current position.
     * @return a GeoPosition bean containing latitude and longitude
     * @see Pilot#setPosition(double, double)
     * @see Pilot#setPosition(String, String)
     */
    public GeoPosition getPosition() {
        return _position;
    }
    
    /**
     * Updates the Pilot's altitude.
     * @param alt the altitude in feet above Mean Sea Level
     * @throws IllegalArgumentException if alt is &lt; -250 or &gt; 150,000 feet
     * @see Pilot#setAltitude(String)
     * @see Pilot#getAltitude()
     */
    public void setAltitude(int alt) {
        if ((alt < -250) || (alt > 150000))
            throw new IllegalArgumentException("Invalid Altitude - " + alt);
        
        _altitude = alt;
    }
    
    /**
     * Updates the Pilot's altitude.
     * @param alt a String containing the altitude in feet above Mean Sea Level
     * @see Pilot#setAltitude(int)
     * @see Pilot#getAltitude()
     */
    public void setAltitude(String alt) {
        try {
            setAltitude(Integer.parseInt(alt));
        } catch (NumberFormatException nfe) {
            setAltitude(0);
        }
    }
    
    /**
     * Updates the destination Airport.
     * @param aa the destination Airport
     * @see Pilot#getAirportA()
     */
    public void setAirportA(Airport aa) {
        _airportA = aa;
    }
    
    /**
     * Updates the origin Airport.
     * @param ad the origin Airport
     * @see Pilot#getAirportD()
     */
    public void setAirportD(Airport ad) {
        _airportD = ad;
    }
    
    /**
     * Updates the Pilot's flight plan comments.
     * @param comments the flight plan comments
     * @see Pilot#getComments()
     */
    public void setComments(String comments) {
       _comments = comments;
    }
    
    /**
     * Updates the Pilot's equipment code.
     * @param eqCode the equipment code
     * @see Pilot#getEquipmentCode()
     */
    public void setEquipmentCode(String eqCode) {
        _eqCode = eqCode;
    }
    
    /**
     * Updates the Pilot's ground speed.
     * @param gSpeed the ground speed in knots
     * @throws IllegalArgumentException if gSpeed is negative or &gt; 4500
     * @see Pilot#setGroundSpeed(String)
     * @see Pilot#getGroundSpeed()
     */
    public void setGroundSpeed(int gSpeed) {
        if ((gSpeed < 0) || (gSpeed > 4500))
            throw new IllegalArgumentException("Invalid Ground Speed - " + gSpeed);
        
        _gSpeed = gSpeed;
    }
    
    /**
     * Updates the Pilot's ground speed.
     * @param gSpeed a String containing the ground speed in knots
     * @see Pilot#setGroundSpeed(int)
     * @see Pilot#getGroundSpeed()
     */
    public void setGroundSpeed(String gSpeed) {
        try {
            setGroundSpeed(Integer.parseInt(gSpeed));
        } catch (NumberFormatException nfe) {
            setGroundSpeed(0);
        }
    }
    
    /**
     * Updates the Pilot's position.
     * @param lat the latitude in degrees
     * @param lon the longitude in degrees
     * @see Pilot#setPosition(String, String)
     * @see Pilot#getPosition()
     */
    public void setPosition(double lat, double lon) {
        _position = new GeoPosition(lat, lon);
    }
    
    /**
     * Updates the Pilot's position.
     * @param lat a String containing the latitude
     * @param lon a String containing the longitude
     * @see Pilot#setPosition(double, double)
     * @see Pilot#getPosition()
     */
    public void setPosition(String lat, String lon) {
       try {
          _position = new GeoPosition(Double.parseDouble(lat), Double.parseDouble(lon));
       } catch (NumberFormatException nfe) {
          _position = new GeoPosition(0, 0);
       }
    }
    
    /**
     * Saves the raw data from the FSD feed.
     * @param data the raw data
     * @see Pilot#getRawData()
     */
    public void setRawData(String data) {
       _rawData = data;
    }
}