package org.deltava.beans.schedule;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.deltava.beans.DateTime;
import org.deltava.beans.Flight;

/**
 * A class to store Schedule Entry information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleEntry extends Flight {

    private static final SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
    
    private DateTime _timeD;
    private DateTime _timeA;

    private boolean _historic;
    private boolean _purge;

    /**
     * Creates a new Schedule Entry object with a given flight.
     * @param a the Airline
     * @param fNumber the Flight Number
     * @param leg the Leg Number
     * @throws NullPointerException if the Airline Code is null
     * @throws IllegalArgumentException if the Flight Report is zero or negative
     * @throws IllegalArgumentException if the Leg is less than 1 or greater than 5
     * @see Flight#setAirline(Airline)
     * @see Flight#setFlightNumber(int)
     * @see Flight#setLeg(int)
     */
    public ScheduleEntry(Airline a, int fNumber, int leg) {
        super(a, fNumber, leg);
    }

    /**
     * Returns the length of the flight, in hours <i>multiplied by 10</i>.
     * @return the length of the flight
     * @throws IllegalStateException if departure or arrival times are not set
     * @see DateTime#difference(DateTime)
     */
    public final int getLength() {
        if ((_timeA == null) || (_timeD == null))
            throw new IllegalStateException("Arrival and Departure Times are not set");

        // Calculate flight time in seconds, and then divide by 3600 and multiply by 10
        long lengthS = _timeA.difference(_timeD);
        return (int) (lengthS / 360);
    }

    /**
     * Returns the departure time of the flight, with full timezone information. The date component
     * of this value can be ignored.
     * @return the full departure time of the flight
     * @see ScheduleEntry#getDateTimeA()
     */
    public DateTime getDateTimeD() {
        return _timeD;
    }

    /**
     * Returns the arrival time of the flight, with full timezone information. The date component
     * of this value can be ignored.
     * @return the full arrival time of the flight
     * @see ScheduleEntry#getDateTimeD()
     */
    public DateTime getDateTimeA() {
        return _timeA;
    }

    /**
     * Returns the departure time for this flght. <i>This time is in local time.</i> The date and timezone portions
     * of this Date should be ignored.
     * @return the departure time for this flight.
     * @see ScheduleEntry#setTimeD(Date)
     * @see ScheduleEntry#getTimeA()
     * @see ScheduleEntry#getDateTimeD()
     */
    public Date getTimeD() {
        return _timeD.getDate();
    }

    /**
     * Returns the arrival time for this flght. <i>This time is in local time.</i> The date and timezone portions
     * of this Date should be ignored.
     * @return the arrival time for this flight.
     * @see ScheduleEntry#setTimeA(Date)
     * @see ScheduleEntry#getTimeD()
     * @see ScheduleEntry#getDateTimeA()
     */
    public Date getTimeA() {
        return _timeA.getDate();
    }

    /**
     * Returns the "historic flight" flag value for this flight.
     * @return TRUE if this is a historic flight, FALSE otherwise
     * @see ScheduleEntry#setHistoric(boolean)
     */
    public boolean isHistoric() {
        return _historic;
    }

    /**
     * Returns if this flight can be purged from the schedule database before an automated import
     * @return TRUE if the flight can be automatically purged from the database, otherwise FALSE
     * @see ScheduleEntry#setPurge(boolean)
     */
    public boolean canPurge() {
        return _purge;
    }

    /**
     * Sets the departure time for this flight.
     * @param dt the departure time of the flight <i>in local time </i>. The date and time zone are ignored.
     * @throws NullPointerException if the departure airport is not set
     * @see ScheduleEntry#setTimeA(Date)
     * @see ScheduleEntry#getTimeD()
     * @see ScheduleEntry#getDateTimeD()
     */
    public void setTimeD(Date dt) {
        _timeD = new DateTime(dt, getAirportD().getTZ());
    }

    /**
     * Sets the departure time for this flight from a string in HH:mm format.
     * @param ts the departure time
     * @throws IllegalArgumentException if the date cannot be parsed
     * @throws NullPointerException if the departure airport is not set
     * @see ScheduleEntry#setTimeD(Date)
     * @see ScheduleEntry#setTimeA(String)
     * @see ScheduleEntry#getTimeD()
     * @see ScheduleEntry#getDateTimeD()
     */
    public void setTimeD(String ts) {
        try {
            setTimeD(tf.parse(ts));
        } catch (ParseException pe) {
            throw new IllegalArgumentException("Invalid departure time - " + ts);
        }
    }

    /**		
     * Sets the arrival time for this flight.
     * @param dt the arrival time of the flight <i>in local time </i>. The date and time zone are ignored.
     * @throws NullPointerException if the arrival airport is not set
     * @see ScheduleEntry#setTimeA(String)
     * @see ScheduleEntry#setTimeD(Date)
     * @see ScheduleEntry#getTimeA()
     * @see ScheduleEntry#getDateTimeA()
     */
    public void setTimeA(Date dt) {
        _timeA = new DateTime(dt, getAirportA().getTZ());
    }

    /**
     * Sets the arrival time for this flight from a string in HH:mm format.
     * @param ts the arrival time
     * @throws IllegalArgumentException if the time cannot be parsed
     * @throws NullPointerException if the arrival airport is not set
     * @see ScheduleEntry#setTimeA(Date)
     * @see ScheduleEntry#setTimeD(Date)
     * @see ScheduleEntry#getTimeD()
     * @see ScheduleEntry#getDateTimeD()
     */
    public void setTimeA(String ts) {
        try {
            setTimeA(tf.parse(ts));
        } catch (ParseException pe) {
            throw new IllegalArgumentException("Invalid arrival time - " + ts);
        }
    }

    /**
     * Updates this Schedule entry's "historic flight" flag.
     * @param historic the new "historic flight" flag value
     * @see ScheduleEntry#isHistoric()
     */
    public void setHistoric(boolean historic) {
        _historic = historic;
    }

    /**
     * Updates this Schedule entry's "no purge" flag. This typically is set on historic flights.
     * @param purge the new "no purge" flag value
     * @see ScheduleEntry#canPurge()
     * @see ScheduleEntry#isHistoric()
     */
    public void setPurge(boolean purge) {
        _purge = purge;
    }
}