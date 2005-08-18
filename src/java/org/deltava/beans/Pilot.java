package org.deltava.beans;

import java.util.*;
import java.text.DecimalFormat;

import org.deltava.comparators.FlightReportComparator;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.Cacheable;

/**
 * A class for storing Pilot entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Pilot extends Person implements Cacheable, ComboAlias {
    
    public static final int ACTIVE = 0;
    public static final int INACTIVE = 1;
    public static final int RETIRED = 2;
    public static final int TRANSFERRED = 3;
    public static final int SUSPENDED = 4;
    public static final int ON_LEAVE = 5;
    
    /**
     * Valid pilot statuses.
     */
    public static final String[] STATUS = {"Active", "Inactive", "Retired", "Transferred", "Suspended", "On Leave"};
    
    /**
     * Valid route mapping types.
     */
    public static final String[] MAP_TYPES = {"Google Maps", "Falling Rain"};
    
    public static final int MAP_GOOGLE = 0;
    public static final int MAP_FALLINGRAIN = 1;
    
    private static final String[] ROW_CLASSES = {null, "opt2", "opt3", "opt1", "err", "warn"};

    private String _pCodePrefix;
    private int _pCodeId;

    private Set _ratings = new TreeSet();
    private Set _roles = new TreeSet();

    private SortedSet _flights;
    private long _miles;
    private Date _lastFlight;

    private int _legs;
    private int _onlineLegs;
    private double _hours;
    private double _onlineHours;

    private boolean _showSigs;
    private boolean _showSSThreads;
    private boolean _hasSignature;
    
    private int _mapType;

    private static final DecimalFormat _df = new DecimalFormat("##000");

    /**
     * Creates a Pilot object with a given first and last name, converted to "proper case".
     * @param firstName the Pilot's first name.
     * @param lastName the Pilot's last name.
     * @throws NullPointerException if either name is null
     * @see org.deltava.util.StringUtils#properCase(String)
     */
    public Pilot(String firstName, String lastName) {
        super(firstName, lastName);
        FlightReportComparator frc = new FlightReportComparator(FlightReportComparator.DATE);
        frc.setReverseSort(true);
        _flights = new TreeSet(frc);
        _roles.add("Pilot");
    }

    /**
     * Returns the list of security roles this Pilot belongs to.
     * @return a sorted List of role Names for this Pilot
     */
    public Collection getRoles() {
        return new HashSet(_roles);
    }
    
    /**
     * Returns the airline code for this Pilot.
     * @return the airline code
     */
    public String getAirlineCode() {
    	return _pCodePrefix;
    }

    /**
     * Return the Pilot Code.
     * @return the pilot code eg. DVA043. If the pilot has not logged any flights this will return an empty
     * stirng ("").
     * @see Pilot#setPilotCode(CharSequence)
     */
    public String getPilotCode() {
        return (_pCodeId == 0) ? "" : _pCodePrefix + String.valueOf(_df.format(_pCodeId));
    }

    /**
     * Return the pilot number.
     * @return the pilot number eg. 43. If the pilot has not logged any flights this will return 0.
     * @see Pilot#setPilotCode(CharSequence)
     */
    public int getPilotNumber() {
        return _pCodeId;
    }
    
    public String getComboName() {
        return getName();
    }
    
    public String getComboAlias() {
        return String.valueOf(getID());
    }

    /**
     * Returns wether the pilot will see Water Cooler signatures.
     * @return TRUE if the Pilot views signature images, otherwise FALSE
     * @see Pilot#setShowSignatures(boolean)
     */
    public boolean getShowSignatures() {
        return _showSigs;
    }
    
    /**
     * Returns wether the pilot will see Water Cooler screen shot message threads.
     * @return TRUE if the Pilot sees screen shot threads, otherwise FALSE
     * @see Pilot#setShowSSThreads(boolean)
     */
    public boolean getShowSSThreads() {
        return _showSSThreads;
    }

    /**
     * Returns the Pilot's status text for a JSP.
     * @return the status name
     * @see Person#getStatus()
     * @see Pilot#setStatus(int)
     * @see Pilot#setStatus(String)
     */
    public String getStatusName() {
        return Pilot.STATUS[getStatus()];
    }
    
    /**
     * Returns the Pilot's preferred route map type.
     * @return the map type code.
     * @see Pilot#getMapTypeName()
     * @see Pilot#setMapType(int)
     * @see Pilot#setMapType(String)
     */
    public int getMapType() {
       return _mapType;
    }
    
    /**
     * Returns the Pilot's preferred route map type name.
     * @return the map type name
     * @see Pilot#getMapType()
     * @see Pilot#setMapType(int)
     * @see Pilot#setMapType(String)
     */
    public String getMapTypeName() {
       return Pilot.MAP_TYPES[getMapType()];
    }
    
    /**
     * Return the Pilot's rated equipment Types
     * @return a sorted list of the equipment types the Pilot is rated to fly.
     * @see Pilot#addRating(String)
     * @see Pilot#addRatings(Collection)
     * @see Pilot#removeRatings(Collection)
     */
    public List getRatings() {
        return new ArrayList(_ratings);
    }

    /**
     * Return the Pilot's Flight Reports.
     * @return a sorted list of FlightReport objects, or null if not loaded from the database or empty.
     * @see Pilot#addFlight(FlightReport)
     */
    public List getFlights() {
        return new ArrayList(_flights);
    }

    /**
     * Return the number of legs flown by this Pilot.
     * @return the number of legs flown, either from the List of FlightReport beans or directly from
     * setFlights()
     * @see Pilot#getFlights()
     * @see Pilot#setLegs(int)
     */
    public int getLegs() {
        return (_flights.size() == 0) ? _legs : _flights.size();
    }

    /**
     * Returns the number of miles flown by this Pilot
     * @return the number of miles flown, either from the List of FlightReport beans or directly from
     * setMiles()
     * @see Pilot#setMiles(long)
     * @see Pilot#getFlights()
     */
    public long getMiles() {
        if (!isPopulated())
            return _miles;

        // Iterate through PIREPs
        long miles = 0;
        for (Iterator i = _flights.iterator(); i.hasNext();) {
            FlightReport fr = (FlightReport) i.next();
            miles += fr.getDistance();
        }

        return miles;
    }

    /**
     * Return the number of flight hours logged by this Pilot.
     * @return the number of hours flown, either from the List of FlightReport beans or directly from
     * {@link Pilot#setHours(double) }
     * @see Pilot#getFlights()
     * @see Pilot#getOnlineHours()
     * @see Person#getLegacyHours()
     * @see Pilot#setHours(double)
     */
    public double getHours() {
        if (!isPopulated())
            return _hours;

        // Iterate through the PIREPs
        int hours = 0;
        for (Iterator i = _flights.iterator(); i.hasNext();) {
            FlightReport fr = (FlightReport) i.next();
            if (fr.getStatus() == FlightReport.OK)
                hours += fr.getLength();
        }

        return (hours / 10.0);
    }
    
    /**
     * Return the number of online flight legs logged by this Pilot.
     * @return the number of hours flown, either from the Set of FlightReport beans or directly from
     * {@link Pilot#setOnlineLegs(int) }
     * @see Pilot#getFlights()
     * @see Pilot#getHours()
     * @see Person#getLegacyHours()
     * @see Pilot#setOnlineLegs(int)
     */
    public int getOnlineLegs() {
        if (!isPopulated())
            return _onlineLegs;
        
        // Iterate through the PIREPs
        int legs = 0;
        for (Iterator i = _flights.iterator(); i.hasNext();) {
            FlightReport fr = (FlightReport) i.next();
            if ((fr.getStatus() == FlightReport.OK) && (fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK)))
                legs++;
        }
        
        return legs;
    }

    /**
     * Return the number of online flight hours logged by this Pilot.
     * @return the number of hours flown, either from the Set of FlightReport beans or directly from
     * {@link Pilot#setOnlineHours(double) }
     * @see Pilot#getFlights()
     * @see Pilot#getHours()
     * @see Pilot#getLastFlight()
     * @see Person#getLegacyHours()
     * @see Pilot#getOnlineLegs()
     * @see Pilot#setOnlineHours(double)
     */
    public double getOnlineHours() {
        if (!isPopulated())
            return _onlineHours;

        // Iterate through the PIREPs
        int hours = 0;
        for (Iterator i = _flights.iterator(); i.hasNext();) {
            FlightReport fr = (FlightReport) i.next();
            if ((fr.getStatus() == FlightReport.OK) && (fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK)))
                hours += fr.getLength();
        }

        return (hours / 10.0);
    }
    
    /**
     * Returns the date of the Pilot's last flight.
     * @return the date of the latest Flight Report, either from the Set of FlightReport beans or directly
     * from {@link Pilot#setLastFlight(Date) }
     * @see Pilot#getFlights()
     * @see Pilot#getHours()
     * @see Pilot#getOnlineHours()
     * @see Pilot#getOnlineLegs()
     * @see Pilot#setLastFlight(Date)
     */
    public Date getLastFlight() {
        if (!isPopulated())
    		return _lastFlight;
    	
    	FlightReport lastFlight = (FlightReport) _flights.last();
    	return lastFlight.getDate();
    }

    /**
     * Queries wether this Pilot is rated in a particular equipment type.
     * @param eqType the name of the equipment type
     * @return TRUE if the Pilot is rated for this equipment type, otherwise FALSE
     */
    public boolean hasRating(String eqType) {
        return _ratings.contains(eqType);
    }
    
    /**
     * Queries if the Pilot has a signature image.
     * @return TRUE if the pilot has an image, otherwise FALSE
     * @see Pilot#setHasSignature(boolean)
     */
    public boolean getHasSignature() {
    	return _hasSignature;
    }

    /**
     * Queries wether this Pilot is a member of a particular security role.
     * @param roleName the name of the role
     * @return TRUE if the Pilot is a member of this role, otherwise FALSE
     */
    public boolean isInRole(String roleName) {
        return ("*".equals(roleName) || _roles.contains(roleName) || _roles.contains("Admin"));
    }
    
    /**
     * Queries if the PIREP beans have already been loaded
     * @return TRUE if the PIREPs have been loaded, otherwise FALSE
     */
    public boolean isPopulated() {
        return (_flights.size() != 0);
    }

    /**
     * Update this Pilot's status.
     * @param status the new Status code for the Pilot. Use PilotStatus constants if possible
     * @throws IllegalArgumentException if the new status is not contained within PilotStatus, or is negative
     * @see Person#setStatus(int)
     */
    public final void setStatus(int status) {
        if (status >= Pilot.STATUS.length)
            throw new IllegalArgumentException("Invalid Pilot Status - " + status);

        super.setStatus(status);
    }

    /**
     * Updates this Pilot's "Show Water Cooler signatures" flag.
     * @param showSigs the new flag value
     * @see Pilot#getShowSignatures()
     */
    public void setShowSignatures(boolean showSigs) {
        _showSigs = showSigs;
    }
    
    /**
     * Updates wether this Pilot will see Water Cooler screen shot threads.
     * @param showThreads TRUE if screen show threads will be displayed, otherwise FALSE
     * @see Pilot#getShowSSThreads()
     */
    public void setShowSSThreads(boolean showThreads) {
        _showSSThreads = showThreads;
    }
    
    /**
     * Sets if this Pilot has a signature image available.
     * @param hasSig TRUE if a signature image is available, otherwise FALSE
     * @see Pilot#getHasSignature()
     */
    public void setHasSignature(boolean hasSig) {
    	_hasSignature = hasSig;
    }
    
    /**
     * Sets the Pilot's preferred Map type.
     * @param mapType the Map Type code
     * @throws IllegalArgumentException if mapType is negative or invalid
     * @see Pilot#setMapType(String)
     * @see Pilot#getMapType()
     * @see Pilot#getMapTypeName()
     */
    public void setMapType(int mapType) {
       if ((mapType < 0) || (mapType >= MAP_TYPES.length))
          throw new IllegalArgumentException("Invalid Map Type - " + mapType);
       
       _mapType = mapType;
    }
    
    /**
     * Sets the Pilot's preferred Map type.
     * @param mapType the map Type name
     * @throws IllegalArgumentException if mapType is invalid
     * @see Pilot#setMapType(int)
     * @see Pilot#getMapType()
     * @see Pilot#getMapTypeName()
     */
    public void setMapType(String mapType) {
       setMapType(StringUtils.arrayIndexOf(Pilot.MAP_TYPES, mapType));
    }

    /**
     * Update this Pilot's status.
     * @param status the new Status description for the Pilot. Use PilotStatus constants if possible
     * @throws IllegalArgumentException if the new status is not contained within PilotStatus, or is negative
     * @see Person#setStatus(int)
     * @see Person#getStatus()
     */
    public final void setStatus(String status) {
       setStatus(StringUtils.arrayIndexOf(Pilot.STATUS, status));
    }

    /**
     * Update this Pilot's flights from a collection.
     * @param flights a Collection of FlightReports.
     */
    public void setFlights(Collection flights) {
        _flights.clear();
        if (flights != null)
        	_flights.addAll(flights);
    }

    /**
     * Update this Pilot's logged hours. This method will typically only be called from a DAO where we are
     * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
     * <B>SUM(PIREPS.HOURS)</B>.
     * @param hours the number of hours logged by this Pilot.
     * @throws IllegalStateException if at least one PIREP has already been added via Pilot.addFlight()
     * @throws IllegalArgumentException if hours is negative
     * @see Pilot#addFlight(FlightReport)
     * @see Pilot#getHours()
     */
    public void setHours(double hours) {
        if (isPopulated()) {
            throw new IllegalStateException("PIREP beans already loaded");
        } else if (hours < 0) {
            throw new IllegalArgumentException("Hours cannot be negative");
        }

        _hours = hours;
    }

    /**
     * Update this Pilot's logged flight legs. This method will typically only be called from a DAO where we
     * are querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
     * <B>COUNT(PIREPS.HOURS)</B>.
     * @param legs the number of legs logged by this Pilot
     * @throws IllegalStateException if at least one PIREP has already been added via Pilot.addFlight()
     * @throws IllegalArgumentException if legs is negative
     * @see Pilot#addFlight(FlightReport)
     * @see Pilot#setFlights(Collection)
     * @see Pilot#setHours(double)
     * @see Pilot#setOnlineHours(double)
     * @see Pilot#getLegs()
     */
    public void setLegs(int legs) {
        if (isPopulated()) {
            throw new IllegalStateException("PIREP beans already loaded");
        } else if (legs < 0) {
            throw new IllegalArgumentException("Legs cannot be negative");
        }

        _legs = legs;
    }
    
    /**
     * Update this Pilot's logged onlne flight legs. This method will typically only be called from a DAO where we
     * are querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
     * <B>COUNT(PIREPS.HOURS) WHERE ((PIREPS.ATTR & 0x0D) != 0)</B>.
     * @param legs the number of legs logged by this Pilot
     * @throws IllegalStateException if at least one PIREP has already been added via Pilot.addFlight()
     * @throws IllegalArgumentException if legs is negative
     * @see Pilot#addFlight(FlightReport)
     * @see Pilot#setFlights(Collection)
     * @see Pilot#setHours(double)
     * @see Pilot#setOnlineHours(double)
     * @see Pilot#getLegs()
     */
    public void setOnlineLegs(int legs) {
        if (isPopulated()) {
            throw new IllegalStateException("PIREP beans already loaded");
        } else if (legs < 0) {
            throw new IllegalArgumentException("Legs cannot be negative");
        }

        _onlineLegs = legs;
    }
    
    /**
     * Updates this Pilot's last flight report date. This method will typically only be called from a DAO where we
     * are querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
     * <B>MAX(PIREPS.DATE)</B>.
     * @param dt the date of the last flight.
     * @throws IllegalStateException if at least one PIREP has already been added via Pilot.addFlight()
     * @see Pilot#getLastFlight()
     * @see Pilot#addFlight(FlightReport)
     * @see Pilot#setFlights(Collection)
     * @see Pilot#setHours(double)
     * @see Pilot#setOnlineHours(double)
     */
    public void setLastFlight(Date dt) {
        if (isPopulated())
            throw new IllegalStateException("PIREP beans already loaded");
    	
    	_lastFlight = dt;
    }
    
    /**
     * Updates this Pilot's logged online hours. This method will typically only be called from a DAO where we are
     * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
     * <B>SUM(PIREPS.HOURS) WHERE ((PIREPS.ATTRS & 0x0D) != 0)</B>. 
     * @param hours the online hours logged by this Pilot
     * @throws IllegalStateException if at least one PIREP has already been added via Pilot.addFlight()
     * @throws IllegalArgumentException if hours is negative
     * @see Pilot#addFlight(FlightReport)
     * @see Pilot#setFlights(Collection)
     * @see Pilot#setHours(double)
     * @see Pilot#setLegs(int)
     * @see Pilot#getOnlineHours()
     */
    public void setOnlineHours(double hours) {
        if (isPopulated()) {
            throw new IllegalStateException("PIREP beans already loaded");
        } else if (hours < 0) {
            throw new IllegalArgumentException("Online hours cannot be negative");
        }
        
        _onlineHours = hours;
    }

    /**
     * Update this pilot's logged miles. This method will typically only be called from a DAO where we are
     * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
     * <B>SUM(PIREPS.DISTANCE)</B>.
     * @param miles the number of miles logged by this pilot
     * @throws IllegalStateException if at least one PIREP has already been added via Pilot.addFlight()
     * @throws IllegalArgumentException if miles is negative
     * @see Pilot#addFlight(FlightReport)
     * @see Pilot#setFlights(Collection)
     * @see Pilot#getMiles()
     */
    public void setMiles(long miles) {
        if (isPopulated()) {
            throw new IllegalStateException("PIREP beans already loaded");
        } else if (miles < 0) {
            throw new IllegalArgumentException("Miles cannot be negative");
        }

        _miles = miles;
    }

    /**
     * Add a rated equipment type for this Pilot.
     * @param rating the aircraft type to add to this Pilot's ratings.
     * @throws NullPointerException if the rating is null
     * @see Pilot#addRatings(Collection)
     * @see Pilot#removeRatings(Collection)
     * @see Pilot#getRatings()
     */
    public void addRating(String rating) {
        _ratings.add(rating);
    }

    /**
     * Add a number of equipment type ratings for this Pilot. 
     * @param ratings a Collection of ratings to add to this Pilot's ratings
     * @throws NullPointerException if ratings is null
     * @see Pilot#addRating(String)
     * @see Pilot#removeRatings(Collection)
     * @see Pilot#getRatings()
     */
    public void addRatings(Collection ratings) {
       _ratings.addAll(ratings);
    }
    
    /**
     * Remove a number of equipment type ratings for this Pilot.
     * @param ratings a Collection of ratings to remove from this Pilot's ratings
     * @throws NullPointerException if ratings is null
     * @see Pilot#addRating(String)
     * @see Pilot#addRatings(Collection)
     * @see Pilot#getRatings()
     */
    public void removeRatings(Collection ratings) {
       _ratings.removeAll(ratings);
    }
    
    /**
     * Add membership in a security role to this Pilot.
     * @param roleName the name of the role
     */
    public void addRole(String roleName) {
        _roles.add(roleName);
    }
    
    /**
     * Adds membership in a group of security roles to this Pilot.
     * @param roles a Collection of role names
     * @see Pilot#addRole(String)
     * @see Pilot#getRoles()
     * @see Pilot#removeRoles(Collection)
     */
    public void addRoles(Collection roles) {
    	_roles.addAll(roles);
    }
    
    /**
     * Removes this Pilot's membership in a group fo security roles.
     * @param roles a Collection of role names
     * @see Pilot#addRole(String)
     * @see Pilot#addRoles(Collection)
     * @see Pilot#getRoles()
     */
    public void removeRoles(Collection roles) {
    	_roles.removeAll(roles);
    	_roles.add("Pilot");
    }

    /**
     * Add a PIREP object to this Pilot. Calling this will cause Pilot.setLegs(int) and Pilot.setHours(double)
     * to fail.
     * @param fr the PIREP object to add
     * @see Pilot#setLegs(int)
     * @see Pilot#setHours(double)
     */
    public void addFlight(FlightReport fr) {
        _flights.add(fr);
    }

    /**
     * Set the pilot code for this Pilot.
     * @param code the pilot code eg. DVA043.
     * @throws NullPointerException if code is null
     * @throws IllegalArgumentException if the code does not start with the prefix, or the remainder of the
     * code cannot be parsed to a number via DecimalFormat.parse("##000");
     * @see Pilot#getPilotCode()
     * @see Pilot#getPilotNumber()
     * @see DecimalFormat#parse(java.lang.String)
     */
    public void setPilotCode(CharSequence code) {
        if (code == null)
            return;

        StringBuffer pBuf = new StringBuffer();
        StringBuffer cBuf = new StringBuffer();
        for (int x = 0; x < code.length(); x++) {
            char c = Character.toUpperCase(code.charAt(x));
            if (Character.isDigit(c)) {
                cBuf.append(c);
            } else if (Character.isLetter(c)) {
                pBuf.append(c);
            }
        }

        // Save the prefix and the code
        _pCodePrefix = pBuf.toString();
        try {
            _pCodeId = Integer.parseInt(cBuf.toString());
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid Pilot Code - " + code);
        }
    }

    /**
     * Tests equality by comparing the pilot ID. <i>This is used by the GetPilot DAO for cache lookups.</i>
     */
    public Object cacheKey() {
        return new Integer(getID());
    }
    
    /**
     * Selects a table row class based upon the Pilot's status.
     * @return the row CSS class name
     */
    public String getRowClassName() {
    	return ROW_CLASSES[getStatus()];
    }
    
    /**
     * Shallow-clone a Pilot by copying everything except FlightReport/StatusUpdate beans.
     * @return a copy of the current Pilot bean
     */
    public final Object clone() {
       Pilot p2 = new Pilot(getFirstName(), getLastName());
       p2.setDN(getDN());
       p2.setAirportCodeType(getAirportCodeType());
       p2.setCreatedOn(getCreatedOn());
       p2.setDateFormat(getDateFormat());
       p2.setEmail(getEmail());
       p2.setEmailAccess(getEmailAccess());
       p2.setEquipmentType(getEquipmentType());
       p2.setHomeAirport(getHomeAirport());
       p2.setIMHandle(getIMHandle());
       p2.setLastLogin(getLastLogin());
       p2.setLastLogoff(getLastLogoff());
       p2.setLocation(getLocation());
       p2.setLoginCount(getLoginCount());
       p2.setLoginHost(getLoginHost());
       p2.setNumberFormat(getNumberFormat());
       p2.setPassword(getPassword());
       p2.setRank(getRank());
       p2.setStatus(getStatus());
       p2.setTimeFormat(getTimeFormat());
       p2.setTZ(getTZ());
       p2.setUIScheme(getUIScheme());
       p2.addRoles(getRoles());
       p2.addRatings(getRatings());
       p2.setPilotCode(getPilotCode());
       p2.setHasSignature(getHasSignature());
       p2.setHours(getHours());
       p2.setLastFlight(getLastFlight());
       p2.setLegs(getLegs());
       p2.setMapType(getMapType());
       p2.setMiles(getMiles());
       p2.setOnlineHours(getOnlineHours());
       p2.setOnlineLegs(getOnlineLegs());
       p2.setShowSignatures(getShowSignatures());
       p2.setShowSSThreads(getShowSSThreads());
       return p2;
    }
}