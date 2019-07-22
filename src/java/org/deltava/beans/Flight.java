// Copyright 2004, 2005, 2006, 2008, 2009, 2011, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.text.*;
import java.time.Duration;

import org.deltava.beans.schedule.*;

/**
 * A class to store Flight information.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public abstract class Flight extends DatabaseBean implements RoutePair, FlightNumber {

    private final NumberFormat df = new DecimalFormat("#000");

    private Airline _airline;
    private int _flightNumber;
    private int _leg;
    private String _eqType;

    private Airport _airportD;
    private Airport _airportA;

    /**
     * Creates the bean.
     * @param a the Airline
     * @param fNumber the flight number
     * @param leg the leg
     */
    protected Flight(Airline a, int fNumber, int leg) {
        super();
        setAirline(a);
        setFlightNumber(fNumber);
        setLeg((leg == 0) ? 1 : leg);
    }

    /**
     * A method to return the length of the flight in hours <i>multiplied by 10</i>.
     * @return the length of the flight
     */
    public abstract int getLength();

    /**
     * A method to return the exact length of the flight.
     * @return a Duration
     */
    public abstract Duration getDuration();

    @Override
    public Airline getAirline() {
        return _airline;
    }

    /**
     * Returns a text representation of the Flight in the format "CODE### Leg #".
     * @return the flight code
     * @see Flight#getShortCode()
     * @see Flight#toString()
     */
    public String getFlightCode() {
        StringBuilder buf = new StringBuilder(_airline.getCode());
        buf.append(df.format(_flightNumber));
        buf.append(" Leg ");
        buf.append(String.valueOf(_leg));
        return buf.toString();
    }
    
    /**
     * Returns the Flight code without the leg.
     * @return the flight code
     * @see Flight#getFlightCode()
     */
    public String getShortCode() {
    	StringBuilder buf = new StringBuilder(_airline.getCode());
        buf.append(df.format(_flightNumber));
        return buf.toString();
    }

    @Override
    public int getFlightNumber() {
        return _flightNumber;
    }

    @Override
    public int getLeg() {
        return _leg;
    }

    /**
     * Returns the Equipment type for this flight
     * @return the equipment code
     * @see Flight#setEquipmentType(String)
     */
    public String getEquipmentType() {
        return _eqType;
    }

    @Override
    public Airport getAirportA() {
        return _airportA;
    }

    @Override
    public Airport getAirportD() {
        return _airportD;
    }
    
    /**
     * Set the Airline for this flight.
     * @param a the Airline
     * @see Flight#getAirline()
     */
    public void setAirline(Airline a) {
        _airline= a;
    }

    /**
     * Set the Flight Number for this flight
     * @param fNumber the Flight Number
     * @see Flight#getFlightNumber()
     */
    public void setFlightNumber(int fNumber) {
        _flightNumber = Math.max(0, fNumber);
    }

    /**
     * Sets the Flight leg for this flight
     * @param leg the Flight leg
     * @throws IllegalArgumentException if the flight leg is negative or > 8
     * @see Flight#getLeg()
     */
    public void setLeg(int leg) {
        if ((leg < 0) || (leg > 8))
            throw new IllegalArgumentException("Flight Leg cannot be < 0 || > 8");

        _leg = leg;
    }

    /**
     * Sets the equipment type for this flight.
     * @param eqType the aircraft type
     */
    public void setEquipmentType(String eqType) {
        _eqType = eqType;
    }

    /**
     * Sets the Arrival Airport object for this flight.
     * @param a the Arrival Airport object
     */
    public void setAirportA(Airport a) {
        _airportA = a;
    }

    /**
     * Sets the Departure Airport object for this flight.
     * @param a the Departure Airport object
     */
    public void setAirportD(Airport a) {
        _airportD = a;
    }

    /**
     * Compares this Flight to another Flight object by comparing Airline, Flight Number and Leg.
     * @param o2 the object to compare with
     * @throws ClassCastException if o2 is not a FlightNumber object
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(Object o2) {
    	return FlightNumber.compare(this, (FlightNumber) o2);
    }

    /**
     * Tests for equality by comparing the Airline Code, Flight Number and Leg.
     */
    @Override
    public boolean equals(Object o) {
    	return (o instanceof FlightNumber) && (compareTo(o) == 0);
    }

    /**
     * Returns a string representation of the flight - the flight code
     * @see Flight#getFlightCode()
     */
    @Override
    public String toString() {
        return getFlightCode();
    }
}