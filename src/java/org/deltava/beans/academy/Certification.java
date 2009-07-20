// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.*;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store Flight Academy certification data.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class Certification implements java.io.Serializable, ViewEntry, Comparable<Certification> {
	
	public static final int REQ_ANY = 0;
	public static final int REQ_ANYPRIOR = 1;
	public static final int REQ_ALLPRIOR = 2;
	
	public static final String[] REQ_NAMES = {"No Pre-Requisite", "Any Prior Stage Certification", "All Prior Stage Certifications"};

	private String _name;
	private String _code;
	private int _stage;
	private int _preReqs;
	private int _reqCount;
	
	private boolean _active;
	private boolean _autoEnroll;
	
	private final Collection<CertificationRequirement> _reqs = new TreeSet<CertificationRequirement>();
	private final Collection<String> _examNames = new HashSet<String>();
	
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
	 * Returns the certification code.
	 * @return the code
	 * @see Certification#setCode(String)
	 */
	public String getCode() {
		return _code;
	}
	
	/**
	 * Returns wether the certification is active.
	 * @return TRUE if it is active, otherwise FALSE
	 * @see Certification#setActive(boolean)
	 */
	public boolean getActive() {
		return _active;
	}
	
	/**
	 * Returnes wether students are automatically enrolled in this Course.
	 * @return TRUE if students are automatically enrolled, otherwise FALSE
	 * @see Certification#setAutoEnroll(boolean)
	 */
	public boolean getAutoEnroll() {
		return _autoEnroll;
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
	 * Returns the number of Requirements for this Certification.
	 * @return the number of requirements
	 * @see Certification#setReqCount(int)
	 */
	public int getReqCount() {
		return _reqs.isEmpty() ? _reqCount : _reqs.size();
	}
	
	/**
	 * Returns the requirements for this Certification.
	 * @return a Collection of requirement beans
	 * @see Certification#addRequirement(CertificationRequirement)
	 * @see Certification#setRequirements(Collection)
	 */
	public Collection<CertificationRequirement> getRequirements() {
		return _reqs;
	}
	
	/**
	 * Returns the Examinations linked with this Certification.
	 * @return a Collection of Examination names
	 * @see Certification#addExamName(String)
	 */
	public Collection<String> getExamNames() {
		return _examNames;
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
		for (Iterator<CertificationRequirement> i = reqs.iterator(); i.hasNext(); )
			addRequirement(i.next());
	}
	
	/**
	 * Updates the number of requirements for this Certification.
	 * @param count the number of requirements
	 * @throws IllegalStateException if requirement text already loaded
	 * @throws IllegalArgumentException if count is negative
	 * @see Certification#getReqCount()
	 * @see Certification#getRequirements()
	 * @see Certification#addRequirement(CertificationRequirement)
	 */
	public void setReqCount(int count) {
		if (!_reqs.isEmpty())
			throw new IllegalStateException("Requirements already loaded");
		else if (count < 0)
			throw new IllegalArgumentException("Invalid Requirement count - " + count);
			
		_reqCount = count;
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
	 * Updates the stage number.
	 * @param stage the stage
	 * @see Certification#getStage()
	 */
	public void setStage(int stage) {
		_stage = Math.max(0, stage);
	}
	
	/**
	 * Updates wether the certification is active.
	 * @param isActive TRUE if it is active, otherwise FALSE
	 * @see Certification#getActive()
	 */
	public void setActive(boolean isActive) {
		_active = isActive;
	}
	
	/**
	 * Updates wether students are automatically enrolled in this Course.
	 * @param autoEnroll TRUE if students are automatically enrolled, otherwise FALSE
	 * @see Certification#getAutoEnroll()
	 */
	public void setAutoEnroll(boolean autoEnroll) {
		_autoEnroll = autoEnroll;
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
	 * Updates  the pre-requisite code.
	 * @param reqCode the code
	 * @throws IllegalArgumentException if the code is invalid
	 * @see Certification#getReqs()
	 */
	public void setReqs(int reqCode) {
		if ((reqCode < 0) || (reqCode >= REQ_NAMES.length))
			throw new IllegalArgumentException("Invalid Requirement code - " + reqCode);
		
		if (_stage > 1)
			_preReqs = reqCode;
		else
			_preReqs = Certification.REQ_ANY;
	}
	
	/**
	 * Returns the CSS class name if displayed in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return _active ? null : "opt1";
	}

	/**
	 * Compares two certifications by comparing their stage and name.
	 */
	public int compareTo(Certification c2) {
		int tmpResult = Integer.valueOf(_stage).compareTo(Integer.valueOf(c2._stage));
		return (tmpResult == 0) ? _name.compareTo(c2._name) : tmpResult;
	}
	
	/**
	 * Returns the Certification name.
	 */
	public String toString() {
		return _name;
	}
}