// Copyright 2006, 2009, 2010, 2011, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

/**
 * A bean to store Flight Academy certification data.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class Certification implements java.io.Serializable, ComboAlias, ViewEntry, Comparable<Certification> {
	
	public static final int REQ_ANY = 0;
	public static final int REQ_ANYPRIOR = 1;
	public static final int REQ_ALLPRIOR = 2;
	public static final int REQ_SPECIFIC = 3;
	public static final int REQ_FLIGHTS = 4;
	public static final int REQ_HOURS = 5;
	
	public static final String[] REQ_NAMES = {"No Pre-Requisite", "Any Prior Stage Certification", "All Prior Stage Certifications", "Specific Certification", "Flight Legs", "Flight Hours"};

	private String _name;
	private String _code;
	private int _stage;
	private int _preReqs;
	private int _reqCount;
	private int _rideCount;
	
	private boolean _active;
	private boolean _visible;
	private boolean _autoEnroll;
	
	private String _eqProgram;
	private int _flightCount;
	
	private OnlineNetwork _network;
	private String _ratingCode;
	
	private String _desc;
	private final Collection<AirlineInformation> _airlines = new HashSet<AirlineInformation>();
	
	private String _certReq;
	private final Collection<CertificationRequirement> _reqs = new TreeSet<CertificationRequirement>();
	private final Collection<String> _examNames = new TreeSet<String>();
	private final Collection<String> _enrollRoles = new TreeSet<String>();
	private final Collection<String> _checkRideEQ = new TreeSet<String>();
	
	/**
	 * Creates a new Certification bean.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 */
	public Certification(String name) {
		super();
		setName(name);
	}
	
	/**
	 * Returns the certification name.
	 * @return the name
	 * @see Certification#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the Certification code.
	 * @return the code
	 * @see Certification#setCode(String)
	 */
	public String getCode() {
		return _code;
	}
	
	/**
	 * Returns whether the Certification is active.
	 * @return TRUE if it is active, otherwise FALSE
	 * @see Certification#setActive(boolean)
	 */
	public boolean getActive() {
		return _active;
	}

	/**
	 * Returns whether the achievement of this Certification is publicly visible.
	 * @return TRUE if visible, otherwise FALSE
	 * @see Certification#setVisible(boolean)
	 */
	public boolean getVisible() {
		return _visible;
	}
	
	/**
	 * Returns whether students are automatically enrolled in this Course.
	 * @return TRUE if students are automatically enrolled, otherwise FALSE
	 * @see Certification#setAutoEnroll(boolean)
	 */
	public boolean getAutoEnroll() {
		return _autoEnroll;
	}
	
	/**
	 * Returns how many Check Rides are required for this Certification.
	 * @return the number of Check Rides required
	 * @see Certification#setRideCount(int)
	 */
	public int getRideCount() {
		return _rideCount;
	}
	
	/**
	 * Returns the certification stage.
	 * @return the stage number
	 * @see Certification#setStage(int)
	 */
	public int getStage() {
		return _stage;
	}
	
	/**
	 * Returns the pre-requisite code.
	 * @return the pre-requisite code
	 * @see Certification#setReqs(int)
	 * @see Certification#getReqName()
	 */
	public int getReqs() {
		return _preReqs;
	}
	
	/**
	 * Returns the pre-requisite description.
	 * @return the description
	 * @see Certification#getReqs()
	 * @see Certification#setReqs(int)
	 */
	public String getReqName() {
		return REQ_NAMES[_preReqs];
	}
	
	/**
	 * Returns the specific Certification pre-requisite.
	 * @return the Certification code, or null if none
	 * @see Certification#setReqCert(String)
	 */
	public String getReqCert() {
		return _certReq;
	}
	
	/**
	 * Returns the number of Requirements for this Certification.
	 * @return the number of requirements
	 * @see Certification#setReqCount(int)
	 */
	public int getReqCount() {
		return _reqs.isEmpty() ? _reqCount : _reqs.size();
	}
	
	/**
	 * Returns the requirements for this Certification.
	 * @return a Collection of CertificationRequirement beans
	 * @see Certification#addRequirement(CertificationRequirement)
	 * @see Certification#setRequirements(Collection)
	 */
	public Collection<CertificationRequirement> getRequirements() {
		return _reqs;
	}
	
	/**
	 * Returns the eligible virtual airlines for this Certification.
	 * @return a Collection of AirlineInformation beans
	 * @see Certification#addAirline(AirlineInformation)
	 * @see Certification#setAirlines(Collection)
	 */
	public Collection<AirlineInformation> getAirlines() {
		return _airlines;
	}
	
	/**
	 * Returns the Examinations linked with this Certification.
	 * @return a Collection of Examination names
	 * @see Certification#addExamName(String)
	 * @see Certification#setExams(Collection)
	 */
	public Collection<String> getExamNames() {
		return _examNames;
	}
	
	/**
	 * Returns the aircraft type suitable for the Check Ride.
	 * @return a Collection of equipment types, or empty for any
	 * @see Certification#addRideEQ(String)
	 * @see Certification#setRideEQ(Collection)
	 */
	public Collection<String> getRideEQ() {
		return _checkRideEQ;
	}
	
	/**
	 * Returns the security roles required to enroll for this Certification.
	 * @return a Collection of role names
	 * @see Certification#addRole(String)
	 * @see Certification#setRoles(Collection)
	 */
	public Collection<String> getRoles() {
		return _enrollRoles;
	}
	
	/**
	 * Returns the equipment program required for pre-requisite flight legs or hours.
	 * @return the equipment type, or null if none or any
	 * @see Certification#setEquipmentProgram(String)
	 */
	public String getEquipmentProgram() {
		return _eqProgram;
	}
	
	/**
	 * Returns the number of pre-requisite flight legs or hours required to enroll for this Certification.
	 * @return the number of legs or hours
	 * @see Certification#setFlightCount(int)
	 */
	public int getFlightCount() {
		return _flightCount;
	}
	
	/**
	 * Returns the Certification description and instructions.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the Online network that grants a pilot rating when this Certification is completed.
	 * @return the OnlineNetwork, or null if none
	 */
	public OnlineNetwork getNetwork() {
		return _network;
	}
	
	/**
	 * Returns the Online network's pilot rating code.
	 * @return the code
	 */
	public String getNetworkRatingCode() {
		return _ratingCode;
	}
	
	/**
	 * Adds an eligible virtual airline to this Certification.
	 * @param ai an AirlineInformation bean
	 * @see Certification#getAirlines()
	 * @see Certification#setAirlines(Collection)
	 */
	public void addAirline(AirlineInformation ai) {
		_airlines.add(ai);
	}
	
	/**
	 * Assigns eligible virtual airlines to this Certification.
	 * @param airlines a Collection of AirlineInformation beans
	 * @see Certification#getAirlines()
	 * @see Certification#addAirline(AirlineInformation)
	 */
	public void setAirlines(Collection<AirlineInformation> airlines) {
		_airlines.clear();
		_airlines.addAll(airlines);
	}
	
	/**
	 * Adds a requirement for this Certification.
	 * @param req the requirement bean
	 * @see Certification#getRequirements()
	 * @see Certification#setRequirements(Collection)
	 */
	public void addRequirement(CertificationRequirement req) {
		if (req.getID() == 0)
			req.setID(_reqs.size() + 1);
		
		_reqs.add(req);
	}
	
	/**
	 * Clears and updates the requirements for this Certification.
	 * @param reqs a Collection of CertificationRequirement beans
	 * @see Certification#addRequirement(CertificationRequirement)
	 * @see Certification#getRequirements()
	 */
	public void setRequirements(Collection<CertificationRequirement> reqs) {
		_reqs.clear();
		reqs.forEach(r -> addRequirement(r));
	}
	
	/**
	 * Updates the number of requirements for this Certification.
	 * @param count the number of requirements
	 * @throws IllegalStateException if requirement text already loaded
	 * @see Certification#getReqCount()
	 * @see Certification#getRequirements()
	 * @see Certification#addRequirement(CertificationRequirement)
	 */
	public void setReqCount(int count) {
		if (!_reqs.isEmpty())
			throw new IllegalStateException("Requirements already loaded");
			
		_reqCount = Math.max(0, count);
	}
	
	/**
	 * Updates the Certification code.
	 * @param code the code
	 * @throws NullPointerException if code is null
	 * @see Certification#getCode()
	 */
	public void setCode(String code) {
		_code = code.trim().toUpperCase();
	}
	
	/**
	 * Adds an associated Examination name. 
	 * @param name the Examination name
	 * @see Certification#getExamNames()
	 * @see Certification#setExams(Collection)
	 */
	public void addExamName(String name) {
		_examNames.add(name.trim());
	}
	
	/**
	 * Clears and Updates the list of requirement Examinations.
	 * @param exams a Collecton of Examination names
	 * @see Certification#addExamName(String)
	 * @see Certification#getExamNames()
	 */
	public void setExams(Collection<String> exams) {
		_examNames.clear();
		if (exams != null)
			_examNames.addAll(exams);
	}
	
	/**
	 * Adds an eligible Check Ride equipment type.
	 * @param eqType the equipment type
	 * @see Certification#setRideEQ(Collection)
	 * @see Certification#getRideEQ()
	 */
	public void addRideEQ(String eqType) {
		_checkRideEQ.add(eqType);
	}
	
	/**
	 * Clears and sets the Check Ride equipment types.
	 * @param eqTypes a Collection of equipment types
	 * @see Certification#addRideEQ(String)
	 * @see Certification#getRideEQ()
	 */
	public void setRideEQ(Collection<String> eqTypes) {
		_checkRideEQ.clear();
		if (eqTypes != null)
			_checkRideEQ.addAll(eqTypes);
	}
	
	/**
	 * Adds a security role allowed to enroll for this Certification.
	 * @param roleName a security role name
	 * @see Certification#getRoles()
	 * @see Certification#setRoles(Collection)
	 * @throws NullPointerException if roleName is null
	 */
	public void addRole(String roleName) {
		_enrollRoles.add(roleName.trim());
		_enrollRoles.remove("Pilot");
	}
	
	/**
	 * Clears and Updates the list of roles required to enroll.
	 * @param roleNames a Collecton of security role names
	 * @see Certification#addRole(String)
	 * @see Certification#getRoles()
	 */
	public void setRoles(Collection<String> roleNames) {
		_enrollRoles.clear();
		if (roleNames != null) {
			_enrollRoles.addAll(roleNames);
			_enrollRoles.remove("Pilot");
		}
	}
	
	/**
	 * Updates the stage number.
	 * @param stage the stage
	 * @see Certification#getStage()
	 */
	public void setStage(int stage) {
		_stage = Math.max(1, stage);
	}
	
	/**
	 * Updates whether the certification is active.
	 * @param isActive TRUE if it is active, otherwise FALSE
	 * @see Certification#getActive()
	 */
	public void setActive(boolean isActive) {
		_active = isActive;
	}
	
	/**
	 * Updates whether this achievement of this certification is publicly visible.
	 * @param isVisible TRUE if visible, otherwise FALSE
	 * @see Certification#getVisible()
	 */
	public void setVisible(boolean isVisible) {
		_visible = isVisible;
	}
	
	/**
	 * Updates whether students are automatically enrolled in this Course.
	 * @param autoEnroll TRUE if students are automatically enrolled, otherwise FALSE
	 * @see Certification#getAutoEnroll()
	 */
	public void setAutoEnroll(boolean autoEnroll) {
		_autoEnroll = autoEnroll;
	}
	
	/**
	 * Updates the number of Check Rides required for this Certification.
	 * @param cr the number of Check Rides required
	 * @see Certification#getRideCount()
	 */
	public void setRideCount(int cr) {
		_rideCount = Math.max(0, cr);
	}

	/**
	 * Updates the Certification name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see Certification#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the pre-requisite code.
	 * @param reqCode the code
	 * @throws IllegalArgumentException if the code is invalid
	 * @see Certification#getReqs()
	 */
	public void setReqs(int reqCode) {
		if ((reqCode < 0) || (reqCode >= REQ_NAMES.length))
			throw new IllegalArgumentException("Invalid Requirement code - " + reqCode);
		
		_preReqs = (_stage > 1) ? reqCode : REQ_ANY;
	}
	
	/**
	 * Sets the pre-requisite certification (if any).
	 * @param certCode the pre-requisite Certification code
	 * @throws IllegalStateException if getReqs() != REQ_SPECIFIC
	 */
	public void setReqCert(String certCode) {
		if ((_preReqs != REQ_SPECIFIC) && (certCode != null))
			throw new IllegalStateException("Specific Certification pre-requisite not set");
		
		_certReq = (certCode == null) ? null : certCode.toUpperCase();
	}
	
	/**
	 * Updates the equipment type program required for pre-requisite flights.
	 * @param eqProgram the equipment program name, or null for none or any
	 * @see Certification#getEquipmentProgram()
	 */
	public void setEquipmentProgram(String eqProgram) {
		_eqProgram = eqProgram;
	}
	
	/**
	 * Updates the number of flight legs or hours required to enroll for this Certification.
	 * @param cnt the number of legs or hours
	 * @see Certification#getFlightCount()
	 */
	public void setFlightCount(int cnt) {
		_flightCount = Math.max(0, cnt); 
	}
	
	/**
	 * Updates the certification description/instructions.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Updates the online network that this Ceritification will grant a Pilot Rating for.
	 * @param net the OnlineNetwork, or null if none
	 */
	public void setNetwork(OnlineNetwork net) {
		_network = net;
	}
	
	/**
	 * Updates the rating code used by the online network.
	 * @param code the rating code
	 */
	public void setNetworkRatingCode(String code) {
		_ratingCode = code;
	}
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return _code;
	}
	
	/**
	 * Returns the CSS class name if displayed in a view table.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		return _active ? null : "opt1";
	}

	/**
	 * Compares two certifications by comparing their stage and name.
	 */
	@Override
	public int compareTo(Certification c2) {
		int tmpResult = Integer.valueOf(_stage).compareTo(Integer.valueOf(c2._stage));
		return (tmpResult == 0) ? _name.compareTo(c2._name) : tmpResult;
	}
	
	@Override
	public int hashCode() {
		return _code.hashCode();
	}
	
	/**
	 * Returns the Certification name.
	 */
	@Override
	public String toString() {
		return _name;
	}
}