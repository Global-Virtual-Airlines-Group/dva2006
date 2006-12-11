// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

import org.deltava.beans.schedule.Airline;

/**
 * A class for storing ACARS-submitted Flight Reports.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSFlightReport extends FlightReport {

    private Map<String, Date> _stateChangeTimes;
    
    private int _taxiWeight;
    private int _taxiFuel;
    
    private int _takeoffDistance;
    private int _takeoffSpeed;
    private double _takeoffN1;
    private int _takeoffWeight;
    private int _takeoffFuel;
    
    private int _landingDistance;
    private int _landingSpeed;
    private int _landingVspeed;
    private double _landingN1;
    private int _landingWeight;
    private int _landingFuel;
    
    private int _gateWeight;
    private int _gateFuel;
    
    private Map<Long, Integer> _time;
    
    /**
     * Creates a new ACARS Flight Report object with a given flight.
     * @param a the Airline
     * @param flightNumber the Flight Number
     * @param leg the Leg Number
     * @throws NullPointerException if the Airline Code is null
     * @throws IllegalArgumentException if the Flight Report is zero or negative
     * @throws IllegalArgumentException if the Leg is less than 1 or greater than 5
     * @see Flight#setAirline(Airline)
     * @see Flight#setFlightNumber(int)
     * @see Flight#setLeg(int)
     */
    public ACARSFlightReport(Airline a, int flightNumber, int leg) {
        super(a, flightNumber, leg);
        _stateChangeTimes = new HashMap<String, Date>();
        _time = new HashMap<Long, Integer>();
    }
    
    /**
     * Returns the start time of this flight.
     * @return the start date/time
     * @see ACARSFlightReport#setStartTime(Date)
     */
    public Date getStartTime() {
        return _stateChangeTimes.get("START_TIME");
    }
    
    /**
     * Returns the date/time of pushback.
     * @return the date/time the aircraft was pushed back
     * @see ACARSFlightReport#setTaxiTime(Date)
     */
    public Date getTaxiTime() {
        return _stateChangeTimes.get("TAXI_TIME");
    }
    
    /**
     * Returns the aircraft weight at pushback.
     * @return the weight in pounds
     * @see ACARSFlightReport#setTaxiWeight(int)
     */
    public int getTaxiWeight() {
        return _taxiWeight;
    }
    
    /**
     * Returns the amount of fuel at pushback.
     * @return the amount of fuel in pounds
     * @see ACARSFlightReport#setTaxiFuel(int)
     */
    public int getTaxiFuel() {
        return _taxiFuel;
    }
    
    /**
     * Returns the date/time of takeoff.
     * @return the date/time the aircraft left the ground
     * @see ACARSFlightReport#setTakeoffTime(Date)
     */
    public Date getTakeoffTime() {
        return _stateChangeTimes.get("TAKEOFF_TIME");
    }
    
    /**
     * Returns the distance from the origin airport at takeoff. This is used to validate the provided airports.
     * @return the distance from the origin airport, in miles
     * @see ACARSFlightReport#setTakeoffDistance(int)
     * @see ACARSFlightReport#getLandingDistance()
     * @see org.deltava.beans.schedule.GeoPosition#distanceTo(org.deltava.beans.GeoLocation)
     */
    public int getTakeoffDistance() {
        return _takeoffDistance;
    }
    
    /**
     * Returns the aircraft airspeed at takeoff.
     * @return the airspeed in knots
     * @see ACARSFlightReport#setTakeoffSpeed(int)
     */
    public int getTakeoffSpeed() {
        return _takeoffSpeed;
    }
    
    /**
     * Returns the average N1 speed of the engines at takeoff.
     * @return the average N1 percentage, mulitiplied by 100
     * @see ACARSFlightReport#setTakeoffN1(double)
     * @see ACARSFlightReport#getLandingN1()
     */
    public double getTakeoffN1() {
        return _takeoffN1;
    }
    
    /**
     * Returns the weight of the aircraft at takeoff.
     * @return the weight in pounds
     * @see ACARSFlightReport#setTakeoffWeight(int)
     */
    public int getTakeoffWeight() {
        return _takeoffWeight;
    }
    
    /**
     * Returns the amount of fuel at takeoff.
     * @return the amount of fuel in pounds
     * @see ACARSFlightReport#setTakeoffFuel(int)
     */
    public int getTakeoffFuel() {
        return _takeoffFuel;
    }
    
    /**
     * Returns the date/time that the aircraft touched down.
     * @return the date/time of touchdown
     * @see ACARSFlightReport#setLandingTime(Date)
     */
    public Date getLandingTime() {
        return _stateChangeTimes.get("LAND_TIME");
    }
    
    /**
     * Returns the distance from the destination airport at landing. This is used to validate the provided airports.
     * @return the distance from the destination airport, in miles
     * @see ACARSFlightReport#setLandingDistance(int)
     * @see ACARSFlightReport#getTakeoffDistance()
     * @see org.deltava.beans.schedule.GeoPosition#distanceTo(org.deltava.beans.GeoLocation)
     */
    public int getLandingDistance() {
        return _landingDistance;
    }
    
    /**
     * Returns the aircraft airspeed at touchdown.
     * @return the airspeed in knots
     * @see ACARSFlightReport#setLandingSpeed(int)
     */
    public int getLandingSpeed() {
        return _landingSpeed;
    }

    /**
     * Returns the vertical speed of the aircraft at touchdown.
     * @return the vertical speed, in feet per minute
     * @see ACARSFlightReport#setLandingVSpeed(int)
     */
    public int getLandingVSpeed() {
        return _landingVspeed;
    }
    
    /**
     * Returns the average N1 speed of the engines at touchdown.
     * @return the average N1 percentage, multiplied by 100
     * @see ACARSFlightReport#setLandingN1(double)
     * @see ACARSFlightReport#getTakeoffN1()
     */
    public double getLandingN1() {
        return _landingN1;
    }
    
    /**
     * Returns the weight of the aircraft at touchdown.
     * @return the weight in pounds
     * @see ACARSFlightReport#setLandingWeight(int)
     */
    public int getLandingWeight() {
        return _landingWeight;
    }
    
    /**
     * Returns the amount of fuel at touchdown.
     * @return the amount of fuel in pounds
     * @see ACARSFlightReport#setLandingFuel(int)
     */
    public int getLandingFuel() {
        return _landingFuel;
    }

    /**
     * Returns the end date/time of the flight.
     * @return the date/time the flight ended at the gate
     */
    public Date getEndTime() {
        return _stateChangeTimes.get("END_TIME");
    }

    /**
     * Returns the weight of the aircraft at the end of the flight.
     * @return the weight in pounds
     * @see ACARSFlightReport#setGateWeight(int)
     */
    public int getGateWeight() {
        return _gateWeight;
    }
    
    /**
     * Returns the amount of fuel at the end of the flight.
     * @return the amount of fuel in pounds
     * @see ACARSFlightReport#setGateFuel(int)
     */
    public int getGateFuel() {
        return _gateFuel;
    }

    /**
     * Returns the time that the aircraft was airborne for this flight.
     * @return the time the aircraft was airborne, in milliseconds
     * @throws IllegalStateException if either the landing or takeoff time are not set
     * @see ACARSFlightReport#setTakeoffTime(Date)
     * @see ACARSFlightReport#setLandingTime(Date)
     */
    public Date getAirborneTime() {
        try {
            return new Date(getLandingTime().getTime() - getTakeoffTime().getTime()); 
        } catch (NullPointerException npe) {
            throw new IllegalStateException("Landing or Takeoff time not set");
        }
    }
    
    /**
     * Returns the total time of the flight.
     * @return the total time, in milliseconds
     * @throws IllegalStateException if either the start or end time are not set
     * @see ACARSFlightReport#setStartTime(Date)
     * @see ACARSFlightReport#setEndTime(Date)
     */
    public Date getBlockTime() {
        try {
            return new Date(getEndTime().getTime() - getStartTime().getTime()); 
        } catch (NullPointerException npe) {
            throw new IllegalStateException("End or Start time not set");
        }
    }
    
    /**
     * Returns the amount of time at a given time acceleration rate.
     * @param rate the acceleration rate
     * @return the amount of time in seconds at that acceleration rate
     * @see ACARSFlightReport#setTime(int, int)
     * @see ACARSFlightReport#getTimes()
     */
    public int getTime(int rate) {
    	Integer time = _time.get(new Long(rate));
    	return (time == null) ? 0 : time.intValue();
    }
    
    /**
     * Returns a Map of times and acceleration rates.
     * @return a sorted Map of times, keyed by acceleration rate
     * @see ACARSFlightReport#getTime(int)
     */
    public Map<Long, Integer> getTimes() {
    	return new TreeMap<Long, Integer>(_time);
    }
    
    /**
     * Returns the length of the fllight <i>in hours multiplied by ten</i>. This is done to avoid rounding errors when
     * using a floating point number. ACARS Flight Reports will use the time at 1x, 2x, and 4x acceleration to calculate
     * the flight length if the length field is not already populated. This assumes that the ACARS client has performed
     * the proper multiplication before submitting the data. 
     * @return the length of the flight <i>in hours multiplied by ten</i>
     * @see FlightReport#getLength()
     * @see ACARSFlightReport#getTime(int)
     */
    public final int getLength() {
    	return (super.getLength() != 0) ? super.getLength() : (getTime(1) + getTime(2) + getTime(4)) / 360;
    }
    
    /**
     * Updates the start time of the flight.
     * @param dt the date/time the flight started
     * @see ACARSFlightReport#getEndTime()
     */
    public void setStartTime(Date dt) {
        _stateChangeTimes.put("START_TIME", dt);
    }
    
    /**
     * Updates the time the aircraft was pushed back.
     * @param dt the date/time of pushback
     * @see ACARSFlightReport#getTaxiTime()
     */
    public void setTaxiTime(Date dt) {
        _stateChangeTimes.put("TAXI_TIME", dt);
    }

    /**
     * Updates the weight of the aircraft at pushback.
     * @param w the weight in pounds
     * @throws IllegalArgumentException if w is zero or negative
     * @see ACARSFlightReport#getTaxiWeight()
     */
    public void setTaxiWeight(int w) {
        if (w <= 0)
            throw new IllegalArgumentException("Weight cannot be negative or negative");
        
        _taxiWeight = w;
    }
    
    /**
     * Updates the fuel amount at pushback.
     * @param f the amount of fuel in pounds
     * @throws IllegalArgumentException if f is negative
     * @see ACARSFlightReport#getTaxiFuel()
     */
    public void setTaxiFuel(int f) {
        if (f < 0)
            throw new IllegalArgumentException("Fuel cannot be negative");
        
        _taxiFuel = f;
    }
    
    /**
     * Updates the takeoff date/time.
     * @param dt the date/time at takeoff
     * @see ACARSFlightReport#getTakeoffTime()
     */
    public void setTakeoffTime(Date dt) {
        _stateChangeTimes.put("TAKEOFF_TIME", dt);
    }

    /**
     * Updates the distance from the origin airport where takeoff occured.
     * @param d the distance in miles
     * @see ACARSFlightReport#getTakeoffDistance()
     */
    public void setTakeoffDistance(int d) {
        if (d < 0)
            throw new IllegalArgumentException("Takeoff Distance cannot be negative");
        
        _takeoffDistance = d;
    }
    
    /**
     * Updates the airspeed at takeoff.
     * @param s the airspeed in knots
     * @see ACARSFlightReport#getTakeoffSpeed()
     */
    public void setTakeoffSpeed(int s) {
        if (s < 0)
            throw new IllegalArgumentException("Speed cannot be negative");
        
        _takeoffSpeed = s;
    }
    
    /**
     * Updates the average N1 of the engines at takeoff.
     * @param n1 the average N1, multiplied by 100
     * @throws IllegalArgumentException if N1 &lt; 0 or N1 &gt; 140
     * @see ACARSFlightReport#getTakeoffN1()
     */
    public void setTakeoffN1(double n1) {
        if ((n1 < 0) || (n1 > 140))
            throw new IllegalArgumentException("Takeoff N1% cannot be negative");
        
        _takeoffN1 = n1;
    }
    
    /**
     * Updates the aircraft weight at takeoff.
     * @param w the weight in pounds
     * @throws IllegalArgumentException if w is zero or negative
     * @see ACARSFlightReport#getTakeoffWeight()
     */
    public void setTakeoffWeight(int w) {
        if (w <= 0)
            throw new IllegalArgumentException("Weight cannot be zero or negative - " + w);
        
        _takeoffWeight = w;
    }
    
    /**
     * Updates the amount of fuel at takeoff. 
     * @param f the amount of fuel in pounds
     * @throws IllegalArgumentException if f is negative
     * @see ACARSFlightReport#getTakeoffFuel()
     */
    public void setTakeoffFuel(int f) {
        if (f < 0)
            throw new IllegalArgumentException("Fuel cannot be negative");
        
        _takeoffFuel = f;
    }

    /**
     * Updates the landing date/time.
     * @param dt the date/time the aircraft touched down
     * @see ACARSFlightReport#getLandingTime()
     */
    public void setLandingTime(Date dt) {
        _stateChangeTimes.put("LAND_TIME", dt);
    }
    
    /**
     * Updates the distance from the destination airport where touchdown occured.
     * @param d the distance in miles
     * @throws IllegalArgumentException if d is negative
     * @see ACARSFlightReport#getLandingDistance()
     */
    public void setLandingDistance(int d) {
        if (d < 0)
            throw new IllegalArgumentException("Landing Distance cannot be negative");
        
        _landingDistance = d;
    }
    
    /**
     * Updates the airspeed at touchdown.
     * @param s the airpseed in knots
     * @throws IllegalArgumentException if s is negative
     * @see ACARSFlightReport#getLandingSpeed()
     */
    public void setLandingSpeed(int s) {
        if (s < 0)
            throw new IllegalArgumentException("Speed cannot be negative");
        
        _landingSpeed = s;
    }

    /**
     * Updates the vertical speed at touchdown.
     * @param s the vertical speed in feet per minute
     * @see ACARSFlightReport#getLandingVSpeed()
     */
    public void setLandingVSpeed(int s) {
        _landingVspeed = (s > 0) ? s * -1 : s;
    }
    
    /**
     * Updates the average N1 speed of the engines at touchdown.
     * @param n1 the average N1 speed, multiplied by 100
     * @throws IllegalArgumentException if n1 &lt; 0 or n1 &gt; 140
     * @see ACARSFlightReport#getLandingN1()
     */
    public void setLandingN1(double n1) {
        if ((n1 < 0) || (n1 > 140))
            throw new IllegalArgumentException("Landing N1% cannot be negative");
        
        _landingN1 = n1;
    }
    
    /**
     * Updates the weight of the aircraft at touchdown.
     * @param w the weight in pounds
     * @throws IllegalArgumentException if w is zero or negative
     * @see ACARSFlightReport#getLandingWeight()
     */
    public void setLandingWeight(int w) {
        if (w < 0)
            throw new IllegalArgumentException("Weight cannot be zero or negative - " + w);
        
        _landingWeight = w;
    }
    
    /**
     * Updates the amount of fuel at touchdown.
     * @param f the amount of fuel in pounds
     * @throws IllegalArgumentException if f is negative
     * @see ACARSFlightReport#getLandingFuel()
     */
    public void setLandingFuel(int f) {
        if (f < 0)
            throw new IllegalArgumentException("Fuel cannot be negative");
        
        _landingFuel = f;
    }
    
    /**
     * Updates the end time of the flight.
     * @param dt the date/time the flight ended
     * @see ACARSFlightReport#getEndTime()
     */
    public void setEndTime(Date dt) {
        _stateChangeTimes.put("END_TIME", dt);
    }
    
    /**
     * Updates the weight of the aircraft at the end of the flight. 
     * @param w the weight in pounds
     * @throws IllegalArgumentException if w is negative
     * @see ACARSFlightReport#getGateWeight()
     */
    public void setGateWeight(int w) {
        if (w < 0)
            throw new IllegalArgumentException("Weight cannot be zero or negative");
        
        _gateWeight = w;
    }
    
    /**
     * Updates the amount of fuel at the end of the flight.
     * @param f the amount of fuel in pounds
     * @throws IllegalArgumentException if f is negative
     * @see ACARSFlightReport#getGateFuel()
     */
    public void setGateFuel(int f) {
        if (f < 0)
            throw new IllegalArgumentException("Fuel cannot be negative");
        
        _gateFuel = f;
    }
    
    /**
     * Updates the amount of time at a particular time acceleration rate.
     * @param rate the acceleration rate
     * @param secs the amount of time in seconds
     * @throws IllegalArgumentException if rate is not 0, 1, 2, 4
     * @see ACARSFlightReport#getTime(int)
     */
    public void setTime(int rate, int secs) {
    	if (secs < 0)
    		secs = 0;
    	else if ((rate < 0) || (rate == 3) || (rate > 4))
    		throw new IllegalArgumentException("Rate must be 0, 1 2 or 4 - " + rate);
    	
    	_time.put(new Long(rate), new Integer(secs));
    }
}