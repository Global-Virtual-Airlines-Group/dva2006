// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.*;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store Flight Academy certification data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Certification implements java.io.Serializable, ViewEntry, Comparable {
	
	public static final int REQ_ANY = 0;
	public static final int REQ_ANYPRIOR = 1;
	public static final int REQ_ALLPRIOR = 2;
	
	public static final String[] REQ_NAMES = {"No Pre-Requisite", "Any Prior Stage Certification", "All Prior Stage Certifications"};

	private String _name;
	private int _stage;
	private int _preReqs;
	private boolean _active;
	
	private Collection<CertificationRequirement> _reqs = new TreeSet<CertificationRequirement>();
	private Collection<String> _examNames = new HashSet<String>();
	
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
	 * Returns wether the certification is active.
	 * @return TRUE if it is active, otherwise FALSE
	 * @see Certification#setActive(boolean)
	 */
	public boolean getActive() {
		return _active;
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
	 * Returns the requirements for this Certification.
	 * @return a Collection of requirement beans
	 * @see Certification#addRequirement(CertificationRequirement)
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
	 */
	public void addRequirement(CertificationRequirement req) {
		if (req.getID() == 0)
			req.setID(_reqs.size() + 1);
		
		_reqs.add(req);
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
		_examNames.addAll(exams);
	}
	
	/**
	 * Updates the stage number.
	 * @param stage the stage
	 * @throws IllegalArgumentException if stage is negative
	 * @see Certification#getStage()
	 */
	public void setStage(int stage) {
		if (stage < 0)
			throw new IllegalArgumentException("Invalid Stage - " + stage);
		
		_stage = stage;
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
		
		_preReqs = reqCode;
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
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		Certification c2 = (Certification) o;
		int tmpResult = new Integer(_stage).compareTo(new Integer(c2._stage));
		return (tmpResult == 0) ? _name.compareTo(c2._name) : tmpResult;
	}
	
	/**
	 * Returns the Certification name.
	 */
	public String toString() {
		return _name;
	}
}