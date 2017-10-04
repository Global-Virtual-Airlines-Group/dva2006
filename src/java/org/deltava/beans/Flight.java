// Copyright 2004, 2005, 2006, 2008, 2009, 2011, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.text.*;
import java.time.Duration;

import org.deltava.beans.schedule.*;

/**
 * A class to store Flight information.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public abstract class Flight extends DatabaseBean implements RoutePair {

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

    /**
     * Returns the Airline for the Flight.
     * @return the Airline
     * @see Flight#setAirline(Airline)
     */
    public Airline getAirline() {
        return _airline;
    }

    /**
     * Returns a text representation of the Flight in the format "CODE### Leg #".
     * @return the flight code
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
     * Returns the Flight Number for the Flight
     * @return the Flight Number
     * @see Flight#setFlightNumber(int)
     */
    public int getFlightNumber() {
        return _flightNumber;
    }

    /**
     * Returns the Leg Number for the Flight
     * @return the Leg Number
     * @see Flight#setLeg(int)
     */
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

    /**
     * Returns the distance of this flight, between the two airports's positions.
     * @return the distance in nautical miles between the two airports
     * @throws IllegalStateException if either airport is not set
     * @see GeoPosition#distanceTo(GeoLocation)
     */
    @Override
    public int getDistance() {
        if ((_airportA == null) || (_airportD == null))
            throw new IllegalStateException("Both Airports are not set");

        return _airportA.getPosition().distanceTo(_airportD.getPosition());
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
     * Returns whether this Flight's route matches a particular Route Pair.
     * @param rp a RoutePair
     * @return TRUE if the departure and arrival Airports are the same, otherwise FALSE
     */
    public boolean matches(RoutePair rp) {
    	return (_airportD.equals(rp.getAirportD()) && _airportA.equals(rp.getAirportA()));
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
     * @throws ClassCastException if o2 is not a Flight object
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(Object o2) {
        Flight f2 = (Flight) o2;

        // Compare the airline code
        int tmpResult = _airline.compareTo(f2._airline);
        if (tmpResult != 0)
            return tmpResult;

        // Compare the flight number and leg
        tmpResult = Integer.compare(_flightNumber, f2._flightNumber);
        return (tmpResult != 0) ? tmpResult : Integer.compare(_leg, f2._leg);
    }

    /**
     * Tests for equality by comparing the Airline Code, Flight Number and Leg.
     */
    @Override
    public boolean equals(Object o) {
    	return (o instanceof Flight) && (compareTo(o) == 0);
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