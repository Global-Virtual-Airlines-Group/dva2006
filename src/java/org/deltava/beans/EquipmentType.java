// Copyright 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.cache.Cacheable;

/**
 * A class for storing equipment program information.
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public class EquipmentType implements Cacheable, Auditable, Comparable<EquipmentType>, ComboAlias, ViewEntry {
	
	public enum Rating {
		SECONDARY, PRIMARY
	}
	
    private String _name;
    private int _stage = 1;
    private boolean _active = true;
    private boolean _default;
    private boolean _acarsPromotion;
    private boolean _newHires;
    
    private int _cpID;
    private int _size;
    
    private int _promotionLegs;
    private int _promotionHours;
    private int _promotionMinLength;
    
    private int _promotionSwitchLength;
    private int _min1X;
    private int _max2X;

    private final Collection<Rank> _ranks = new TreeSet<Rank>();
    private final Collection<String> _primaryRatings = new TreeSet<String>();
    private final Collection<String> _secondaryRatings = new TreeSet<String>();
    
    private final Map<Rank, Collection<String>> _examNames = new HashMap<Rank, Collection<String>>();
    
    private AirlineInformation _owner;
    private final Collection<AirlineInformation> _airlines = new TreeSet<AirlineInformation>();
    
    /**
     * Create a new EquipmentType object for a given aircraft type
     * @param eqName The name of the equipment type program
     * @throws NullPointerException if the name is null
     * @see EquipmentType#getName() 
     */
    public EquipmentType(String eqName) {
        super();
        setName(eqName);
    }
    
    /**
     * Returns the name of the equipment type program.
     * @return The equipment type name
     * @see EquipmentType#setName(String)
     */
    public String getName() {
        return _name;
    }
    
    /**
	 * Return the database row ID of this equipment program's chief pilot. <i>This typically will only be called by a DAO</i>
	 * @return The primary key of the entry in the <b>PILOTS</b> table in the database that corresponds
	 * to this Equipment Type's chief pilot.
	 * @see EquipmentType#setCPID(int)
	 */
    public int getCPID() {
        return _cpID;
    }

    /**
     * Returns the equipment type stage.
     * @return The stage number of the equipment type
     * @see EquipmentType#setStage(int)
     */
    public int getStage() {
        return _stage;
    }
    
    /**
     * Returns the number of active pilots in the Equipment Program.
     * @return the number of active pilots.
     * @see EquipmentType#setSize(int)
     */
    public int getSize() {
    	return _size;
    }
    
    /**
     * Return the list of available ranks in this program.
     * @return An <i>unsorted</i> list of available ranks
     * @see EquipmentType#addRank(Rank)
     * @see EquipmentType#addRanks(String, String)
     */
    public Collection<Rank> getRanks() {
        return _ranks;
    }
    
    /**
     * Return the list of aircraft types that are considered "primary ratings".
     * @return a sorted list of aircraft types
     * @see EquipmentType#addPrimaryRating(String)
     * @see EquipmentType#getSecondaryRatings()
     * @see EquipmentType#getRatings()
     */
    public Collection<String> getPrimaryRatings() {
        return _primaryRatings;
    }

    /**
     * Return the list of aircraft types that are considered "secondary ratings".
     * @return a sorted list of aircraft types
     * @see EquipmentType#addSecondaryRating(String)
     * @see EquipmentType#getPrimaryRatings()
     * @see EquipmentType#getRatings()
     */
    public Collection<String> getSecondaryRatings() {
        return _secondaryRatings;
    }
    
    /**
     * Returns all primary and secondary ratings.
     * @return a sorted list of aircraft types
     * @see EquipmentType#getPrimaryRatings()
     * @see EquipmentType#getSecondaryRatings()
     * @see EquipmentType#addPrimaryRating(String)
     * @see EquipmentType#addSecondaryRating(String)
     */
    public Collection<String> getRatings() {
    	final Collection<String> results = new TreeSet<String>(_primaryRatings);
    	results.addAll(_secondaryRatings);
    	return results;
    }
    
    /**
     * Return the name of the examination required for promotion into a rank
     * @param rank The rank to be promoted <i>into</i>. Use Ranks.ENTRY for an entrance exam.
     * @return The names of the examinations
     * @see EquipmentType#getExamNames()
     * @see EquipmentType#setExamNames(Rank, Collection)
     */
    public Collection<String> getExamNames(Rank rank) {
    	Collection<String> names = _examNames.get(rank);
    	return (names == null) ? new HashSet<String>() : names;
    }
    
    /**
     * Returns all exams associated with this equipment program.
     * @return a Collection of exam names
     * @see EquipmentType#getExamNames(Rank)
     * @see EquipmentType#setExamNames(Rank, Collection)
     */
    public Collection<String> getExamNames() {
    	Collection<String> names = new LinkedHashSet<String>();
    	for (Iterator<Collection<String>> i = _examNames.values().iterator(); i.hasNext(); )
    		names.addAll(i.next());
    	
    	return names;
    }
    
    /**
     * Return the number of hours required for promotion to Captain.
     * @return The number of hours required for a promotion <i>out of</i> the specified rank, returns 0 if not set
     * @see EquipmentType#setPromotionHours(int)
     * @see EquipmentType#getPromotionLegs()
     */
    public int getPromotionHours() {
    	return _promotionHours;
    }

    /**
     * Return the number of flight legs required for promotion to Captain.
     * @return The number of legs required for a promotion <i>out of</i> the specified rank, returns 0 if not set
     * @see EquipmentType#setPromotionLegs(int)
     * @see EquipmentType#getPromotionHours()
     */
    public int getPromotionLegs() {
    	return _promotionLegs;
    }
    
    /**
     * Returns the minimum flight leg distance requires for promotion to Captain.
     * @return the minimum leg distance in miles
     * @see EquipmentType#setPromotionMinLength(int)
     */
    public int getPromotionMinLength() {
    	return _promotionMinLength;
    }
    
    /**
     * Returns the flight distance where accelerated time is limted, to where a minimum amount
     * of 1X time is required.
     * @return the flight distance
     * @see EquipmentType#setPromotionSwitchLength(int)
     */
    public int getPromotionSwitchLength() {
    	return _promotionSwitchLength;
    }
    
    /**
     * Returns the minimum amount of 1X time for a flight over the switch length in order to
     * count for promotion to Captain.
     * @return the minimum amount of time in seconds
     * @see EquipmentType#setMinimum1XTime(int)
     */
    public int getMinimum1XTime() {
    	return _min1X;
    }
    
    /**
     * Returns the maximum amount of accelerated time for a flight under the switch length in
     * order to count for promotion to Captain.
     * @return the maximum amount of time in seconds
     * @see EquipmentType#setMaximumAccelTime(int)
     */
    public int getMaximumAccelTime() {
    	return _max2X;
    }
    
    /**
     * Returns whether flights counting towards promotion must be logged using ACARS.
     * @return TRUE if promotion legs must be logged using ACARS, otherwise FALSE
     * @see EquipmentType#setACARSPromotionLegs(boolean)
     */
    public boolean getACARSPromotionLegs() {
    	return _acarsPromotion;
    }
    
    /**
     * Returns whether this equipment program is active.
     * @return TRUE if the program is active, otherwise FALSE
     * @see EquipmentType#getActive()
     */
    public boolean getActive() {
        return _active;
    }
    
    /**
     * Returns whether this is default equipment program for Flight Academy and Applicant
     * hire purposes.
     * @return TRUE if default program, otherwise FALSE
     * @see EquipmentType#setIsDefault(boolean)
     */
    public boolean getIsDefault() {
    	return _default;
    }
    
    /**
     * Returns whether this equipment program accepts new hires.
     * @return TRUE if new hires are accepted, otherwise FALSE
     */
    public boolean getNewHires() {
    	return _newHires;
    }
    
    /**
     * Returns the Airline that owns this equipment program profile.
     * @return an AirlineInformation bean
     * @see EquipmentType#setOwner(AirlineInformation)
     */
    public AirlineInformation getOwner() {
    	return _owner;
    }
    
    /**
     * Returns the Airlines whose pilots can get ratings via this equipment program.
     * @return a Collection of AirlineInformation beans
     * @see EquipmentType#addAirline(AirlineInformation)
     * @see EquipmentType#setAirlines(Collection)
     */
    public Collection<AirlineInformation> getAirlines() {
    	return _airlines;
    }
    
    /**
     * Updates the equipment program name.
     * @param name the name
     * @throws NullPointerException if name is null
     * @see EquipmentType#getName()
     */
    public void setName(String name) {
        _name = name.trim();
    }
    
    /**
     * Add an available rank to this equipment type.
     * @param rank The rank
     * @see EquipmentType#getRanks()
     * @see EquipmentType#addRanks(String, String)
     */
    public void addRank(Rank rank) {
        _ranks.add(rank);
    }
    
    /**
     * Adds a number of ranks to this equipment type.
     * @param ranks A token-delimited string of ranks
     * @param delim A token delimieter for the ranks parameter
     * @throws NullPointerException if the list string is null
     * @see EquipmentType#addRank(Rank)
     * @see EquipmentType#getRanks()
     */
    public void addRanks(String ranks, String delim) {
        StringTokenizer rankTokens = new StringTokenizer(ranks, delim);
        while (rankTokens.hasMoreTokens()) {
        	try {
        		Rank r = Rank.fromName(rankTokens.nextToken());
        		addRank(r);
        	} catch (IllegalArgumentException iae) {
        		// empty
        	}
        }
    }
    
    /**
     * Updates the available ranks for this Equipment Program.
     * @param ranks a Collection of rank names
     */
    public void setRanks(Collection<String> ranks) {
    	_ranks.clear();
    	for (String rank : ranks) {
    		Rank r = Rank.fromName(rank);
    		if (r != null)
    			addRank(r);
    	}
    }

    /**
     * Adds a primary rating to the list and removes it from the secondary rating list.
     * @param rating The aircraft type to add
     * @throws NullPointerException if the rating is null
     * @see EquipmentType#getPrimaryRatings()
     */
    public void addPrimaryRating(String rating) {
        if (_secondaryRatings.contains(rating))
            _secondaryRatings.remove(rating);
        
        _primaryRatings.add(rating);
    }
    
    /**
     * Adds a secondary rating to the list and removes it from the primary rating list.
     * @param rating The aircraft type to add
     * @throws NullPointerException if the rating is null
     * @see EquipmentType#getSecondaryRatings()
     */
    public void addSecondaryRating(String rating) {
        if (_primaryRatings.contains(rating))
            _primaryRatings.remove(rating);
        
        _secondaryRatings.add(rating);
    }
    
    /**
     * Marks this equipment program as active.
     * @param active TRUE if the program is active, otherwise FALSE
     * @see EquipmentType#getActive()
     */
    public void setActive(boolean active) {
        _active = active;
    }
    
    /**
     * Marks this equipment program as the default program for new hires and
     * the Flight Academy.
     * @param isDefault TRUE if the default program, otherwise FALSE
     * @see EquipmentType#getIsDefault()
     */
    public void setIsDefault(boolean isDefault) {
    	_default = isDefault;
    }
    
    /**
     * Updates whether flights counting towards promotion must be logged using ACARS.
     * @param useACARS TRUE if flights must be logged using ACARS, otherwise FALSE
     * @see EquipmentType#setACARSPromotionLegs(boolean)
     */
    public void setACARSPromotionLegs(boolean useACARS) {
    	_acarsPromotion = useACARS;
    }
    
    /**
     * Updates whether this equipment program accepts new hires.
     * @param newHires TRUE if new hires can be placed directly into this program, otherwise FALSE
     * @see EquipmentType#getNewHires()
     */
    public void setNewHires(boolean newHires) {
    	_newHires = newHires;
    }
    
    /**
     * Loads primary/secondary ratings.
     * @param pr a Collection of primary ratings
     * @param sr a Collection of secondary ratings
     */
    public void setRatings(Collection<String> pr, Collection<String> sr) {
    	_primaryRatings.clear();
    	if (pr != null)
    	   _primaryRatings.addAll(pr);
    	
    	_secondaryRatings.clear();
    	if (sr != null)
    	   _secondaryRatings.addAll(sr);
    }
    
	/**
	 * Update the database row ID of this program's Chief Pilot. <i>This typically will only be called by a DAO</i>
	 * @param id The primary key of the entry in the <b>PILOTS</b> table in the database that corresponds
	 * to this Equipment Program's chief pilot.
	 * @throws IllegalArgumentException if the database ID is negative
	 * @see EquipmentType#getCPID()
	 */
    public void setCPID(int id) {
	    if (id < 1)
	        throw new IllegalArgumentException("Database ID cannot be negative");

	    _cpID = id;
    }

    /**
     * Adds an examination required for promotion into a particular rank.
     * @param rank The rank to be promoted <i>into</i>
     * @param examName The name of the examination
     * @see Rank
     * @see EquipmentType#setExamNames(Rank, Collection)
     * @see EquipmentType#getExamNames(Rank)
     */
    public void addExam(Rank rank, String examName) {
    	Collection<String> eNames = _examNames.get(rank);
    	if (eNames == null) {
    		eNames = new LinkedHashSet<String>();
    		_examNames.put(rank, eNames);
    	}
    	
    	eNames.add(examName);
    }
    
    /**
     * Set the examinations required for promotion into a particular rank.
     * @param rank The rank to be promoted <i>into</i>
     * @param examNames The name of the examinations
     * @see Rank
     * @see EquipmentType#addExam(Rank, String)
     * @see EquipmentType#getExamNames(Rank)
     */
    public void setExamNames(Rank rank, Collection<String> examNames) {
    	Collection<String> eNames = _examNames.get(rank);
    	if (eNames == null) {
    		eNames = new LinkedHashSet<String>();
    		_examNames.put(rank, eNames);
    	} else
    		eNames.clear();
    	
    	if (examNames != null)
    		eNames.addAll(examNames);
    }
    
    /**
     * Updates the Airline that owns this equipment program.
     * @param ai an AirlineInformation bean
     * @see EquipmentType#getOwner()
     */
    public void setOwner(AirlineInformation ai) {
    	_owner = ai;
    	_airlines.add(ai);
    }
    
    /**
     * Adds an Airline to the list of applications whose Pilots can get ratings in this equipment program.
     * @param ai an AirlineInformation bean
     * @see EquipmentType#getAirlines()
     * @see EquipmentType#setAirlines(Collection)
     */
    public void addAirline(AirlineInformation ai) {
    	_airlines.add(ai);
    }
    
    /**
     * Updates the list of applications whose Pilots can get ratings in this equipment program.
     * @param airlines a Collection of AirlineInformation beans
     * @see EquipmentType#getAirlines()
     * @see EquipmentType#addAirline(AirlineInformation)
     */
    public void setAirlines(Collection<AirlineInformation> airlines) {
    	_airlines.clear();
    	if (airlines != null)
    		_airlines.addAll(airlines);
    }
    
    /**
     * Sets the stage for this equipment program.
     * @param stage The stage number for this program
     * @see EquipmentType#getStage()
     */
    public void setStage(int stage) {
        _stage = Math.max(1, stage);
    }
    
    /**
     * Sets the number of active Pilots in this equipment program.
     * @param size the number of pilots
     * @see EquipmentType#getSize()
     */
    public void setSize(int size) {
    	_size = Math.max(0, size);
    }
    
    /**
     * Set the number of hours required for promotion to Cpatain.
     * @param hours The number of hours required for promotion
     * @see EquipmentType#getPromotionHours()
     * @see EquipmentType#setPromotionLegs(int)
     */
    public void setPromotionHours(int hours) {
    	_promotionHours = Math.max(0, hours);
    }

    /**
     * Set the number of legs required for promotion to Captain.
     * @param legs The number of legs required for promotion
     * @see EquipmentType#getPromotionLegs()
     * @see EquipmentType#setPromotionHours(int)
     */
    public void setPromotionLegs(int legs) {
    	_promotionLegs = Math.max(0, legs);
    }
    
    /**
     * Sets the minimum length of a leg for promotion to Captain.
     * @param distance the distance in miles
     * @see EquipmentType#getPromotionMinLength()
     */
    public void setPromotionMinLength(int distance) {
    	_promotionMinLength = Math.max(0, distance);
    }
    
    /**
     * Sets the distance at which promotion eligibility switches from a maximum amount of
     * accelerated time to a minimum amount of 1X time.
     * @param distance the leg switch distance
     * @see EquipmentType#getPromotionSwitchLength()
     */
    public void setPromotionSwitchLength(int distance) {
    	_promotionSwitchLength = Math.max(0, distance);
    }
    
    /**
     * Sets the minimum amount of 1X time for a flight over the switch length in order to
     * count for promotion to Captain.
     * @param minTime the minimum amount of time in seconds
     * @see EquipmentType#getMinimum1XTime()
     */
    public void setMinimum1XTime(int minTime) {
    	_min1X = Math.max(0, minTime);
    }
    
    /**
     * Sets the maximum amount of accelerated time for a flight under the switch length in
     * order to count for promotion to Captain.
     * @param maxTime the maximum amount of time in seconds
     * @see EquipmentType#getMaximumAccelTime()
     */
    public void setMaximumAccelTime(int maxTime) {
    	_max2X = Math.max(0, maxTime);
    }
    
    /**
     * Compares programs by comparing stage values, then the name.
     */
    @Override
    public int compareTo(EquipmentType et2) {
        int tmp = Integer.valueOf(_stage).compareTo(Integer.valueOf(et2._stage));
        return (tmp == 0) ? _name.compareTo(et2._name) : tmp;
    }
    
    /**
     * Determine equality by comparing the program names.
     * @see String#equals(Object)
     */
    @Override
    public boolean equals(Object o2) {
       return (o2 != null) ? _name.equals(o2.toString()) : false;
    }

    /**
     * Returns the name's hashcode.
     */
    @Override
    public int hashCode() {
    	return _name.hashCode();
    }
    
    /**
     * When converting to a string, just return the name.
     * @see Object#toString()
     */
    @Override
    public String toString() {
       return getName();
    }

    @Override
    public String getComboAlias() {
        return getName();
    }

    @Override
    public String getComboName() {
        return getName();
    }
    
    @Override
    public Object cacheKey() {
    	return _owner.getDB() + "!!" + _name;
    }
    
    @Override
    public String getRowClassName() {
    	return _active ? null : "opt2";
    }

	@Override
	public String getAuditID() {
		return String.valueOf(cacheKey());
	}
}