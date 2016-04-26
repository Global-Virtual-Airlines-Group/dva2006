// Copyright 2006, 2010, 2011, 2012, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A utility class to extract information from a user's Flight Academy history.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

@Helper(Course.class)
public final class AcademyHistoryHelper {
	
	private static final Logger log = Logger.getLogger(AcademyHistoryHelper.class);

	private final Pilot _p; 
	private boolean _debugLog;
	private boolean _allowInactive;
	
	private final Map<Object, Certification> _certs = new HashMap<Object, Certification>();
	private final Map<Object, Course> _courses = new HashMap<Object, Course>();
	private final SortedSet<Test> _tests = new TreeSet<Test>();

	/**
	 * Initializes the helper.
	 * @param p the Pilot
	 * @param courses a Collection of course objects, representing this Pilot's Flight Academy history
	 * @param allCerts all available Certifications
	 */
	public AcademyHistoryHelper(Pilot p, Collection<Course> courses, Collection<Certification> allCerts) {
		super();
		_p = p;
		_courses.putAll(CollectionUtils.createMap(courses, "name"));
		_certs.putAll(CollectionUtils.createMap(allCerts, "name"));
		_certs.putAll(CollectionUtils.createMap(allCerts, "code"));
	}

	private void log(String msg) {
		if (_debugLog)
			log.warn(msg);
	}

	/**
	 * Toggles the debugging log.
	 * @param isDebug TRUE if the log is active, otherwise FALSE
	 */
	public void setDebug(boolean isDebug) {
		_debugLog = isDebug;
	}
	
	/**
	 * Toggles whether the user can enroll in Inactive Courses.
	 * @param doInactive TRUE if the usre can enroll in Inactive Courses, otherwise FALSE
	 */
	public void setAllowInactive(boolean doInactive) {
		_allowInactive = doInactive;
	}
	
	/**
	 * Returns the Pilot's examinations. This method is useful when we use this class and do not wish to call the
	 * {@link org.deltava.dao.GetExam} DAO a second time.
	 * @return a Collection of Test beans
	 */
	public Collection<Test> getExams() {
		return _tests;
	}
	
	/**
	 * Retrieves a Certification. This method is useful when we use this class and do not wish to call the
	 * {@link org.deltava.dao.GetAcademyCertifications} DAO a second time.
	 * @param code the certification name or code
	 * @return a Certification, or null if not found
	 */
	public Certification getCertification(String code) {
		return _certs.get(code);
	}
	
	/**
	 * Returns all Certifications. This method is useful when we use this class and do not wish to call the
	 * {@link org.deltava.dao.GetAcademyCertifications} DAO a second time.
	 * @return a Collection of Certification beans
	 */
	public Collection<Certification> getCertifications() {
		return _certs.values();
	}
	
	/**
	 * Returns the Pilot's courses. This method is useful when we use this class and do not wish to call the
	 * {@link org.deltava.dao.GetAcademyCourses} DAO a second time.
	 * @return a Collection of Course beans
	 */
	public Collection<Course> getCourses() {
		return _courses.values();
	}
	
	/**
	 * Adds the Pilot's Flight Academy examinations and checkrides. Any others will not be added.
	 * @param tests a Collection of checkride/examination objects, representing this Pilot's exam history
	 */
	public void addExams(Collection<Test> tests) {
		for (Test t : tests) {
			if (!t.getAcademy()) continue;
			_tests.add(t);
			if (t instanceof CheckRide) {
				CheckRide cr = (CheckRide) t;
				Course c = getCourse(cr.getCourseID());
				if (c != null)
					c.addCheckRide(cr);
			}
		}
	}

	/**
	 * Returns whether a Pilot has completed a particular certification.
	 * @param certName the Certification name
	 * @return TRUE if the Certification was passed, otherwise FALSE
	 */
	public boolean hasPassed(String certName) {
		Course c = _courses.get(certName);
		return (c != null) && (c.getStatus() == Status.COMPLETE);
	}
	
	/**
	 * Returns whether a Pilot has started a particular course.
	 * @param certName the Certification name
	 * @return TRUE if a course entry exists and was not pased, otherwise FALSE
	 */
	public boolean isPending(String certName) {
		Course c = _courses.get(certName);
		return (c != null) && (c.getStatus() != Status.COMPLETE);
	}
	
	/**
	 * Returns if a user has submitted a particular Examination.
	 * @param examName the Examination name
	 * @return TRUE if the user has submitted this Examination, otherwise FALSE
	 */
	public boolean hasSubmitted(String examName) {
		for (Test t : _tests) {
			if ((t.getStatus() == TestStatus.SUBMITTED) && (t.getName().equals(examName)))
				return true;
		}

		return false;
	}
	
	/**
	 * Returns the Course that the Pilot is currently enrolled in
	 * @return a Course bean, or null
	 */
	public Course getCurrentCourse() {
		for (Course c : _courses.values()) {
			if ((c.getStatus() == Status.STARTED) || (c.getStatus() == Status.PENDING))
				return c;
		}
		
		return null;
	}
	
	/**
	 * Retrieves a specific Course from the history.
	 * @param id the Course database ID
	 * @return a Course, or null if not found
	 */
	public Course getCourse(int id) {
		for (Course c : _courses.values()) {
			if (c.getID() == id)
				return c;
		}
		
		return null;
	}
	
	/**
	 * Returns whether a Pilot has completed a certification in a particular stage.
	 * @param stage the stage number
	 * @return TRUE if any Certification was passed, otherwise FALSE
	 */
	public boolean hasAny(int stage) {
		for (Course c : _courses.values()) {
			if ((c.getStage() == stage) && (c.getStatus() == Status.COMPLETE))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns whether a Pilot has passed a particular Examination.
	 * @param examName the Examination name
	 * @return TRUE if the examination has been passed, otherwise FALSE
	 */
	public boolean passedExam(String examName) {
		for (Test t : _tests) {
			if (t.getPassFail() && t.getName().equals(examName))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns whether a Pilot has completed the requirements for a Flight Academy certifcation. 
	 * @param certName the Certification name
	 * @return TRUE if the certification has been granted or can be, otherwise FALSE
	 */
	public boolean hasCompleted(String certName) {
		
		// Check if we're enrolled in the course
		Course cr = _courses.get(certName);
		if ((cr == null) || (cr.getStatus() == Status.ABANDONED)) {
			log(certName +  " not complete, not enrolled or abandoned");
			return false;
		} else if (cr.getStatus() == Status.COMPLETE) {
			log(certName +  " alread completed");
			return true;
		}
		
		// Check if we've passed all of the exams
		Certification cert = _certs.get(certName);
		for (Iterator<String> i = cert.getExamNames().iterator(); i.hasNext(); ) {
			String examName = i.next();
			if (!passedExam(examName)) {
				log(certName +  " not complete, " + examName + " not passed");
				return false;
			}
		}
		
		// Check if we've finished up all of the requirements
		for (Iterator<CourseProgress> i = cr.getProgress().iterator(); i.hasNext(); ) {
			CourseProgress cp = i.next();
			if (!cp.getComplete()) {
				log(certName +  " not complete, requirement #" + cp.getID() + " incomplete");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns whether a Pilot has completed all certifications in a particular stage.
	 * @param stage the stage number
	 * @return TRUE if all Certification were passed, otherwise FALSE
	 */
	public boolean hasAll(int stage) {
		
		// Get all certs in that particular stage
		Collection<Certification> sCerts = new HashSet<Certification>();
		for (Iterator<Certification> i = _certs.values().iterator(); i.hasNext(); ) {
			Certification c = i.next();
			if (c.getStage() == stage)
				sCerts.add(c);
		}
		
		// Check if we've passed them all
		for (Iterator<Certification> i = sCerts.iterator(); i.hasNext(); ) {
			Certification c = i.next();
			if (!hasPassed(c.getName()))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Returns if the user is eligible to take a particular Flight Academy Course.
	 * @param c the Certification to take
	 * @return TRUE if the user can take the Course, otherwise FALSE
	 */
	public boolean canTake(Certification c) {
		
		// Check that the academy is enabled
		if (!SystemData.getBoolean("academy.enabled")) {
			log("Flight Academy disabled");
			return false;
		}
		
		// If it's inactive, and we're not an instructor no
		if (!c.getActive() && !_allowInactive) {
			log(c.getName() + " inactive");
			return false;
		}
		
		// Check that it matches our airline
		AirlineInformation appInfo = SystemData.getApp(_p.getAirlineCode());
		if (!c.getAirlines().contains(appInfo)) {
			log(c.getName() + " not available to " + appInfo.getName() + " pilots");
			return false;
		}
		
		// If we've already passed it or are taking it, then no
		if (hasPassed(c.getName()) || isPending(c.getName())) {
			log("Already passed/enrolled in " + c.getName());
			return false;
		}
		
		// If we are already in another course
		Course cr = getCurrentCourse();
		if (cr != null) {
			log("Cannot take " + c.getName() + ", already enrolled in " + cr.getName());
			return false;
		}
		
		// Check security roles
		if (!_p.isInRole("AcademyAdmin") && !_p.isInRole("AcademyAudit")) {
			if (!RoleUtils.hasAccess(_p.getRoles(), c.getRoles())) {
				log("Cannot take " + c.getName() + ", needs role in " + c.getRoles());
				return false;
			}
		}
		
		// Check the pre-reqs for the Certification
		switch (c.getReqs()) {
			case Certification.REQ_ANYPRIOR :
				if (!hasAny(c.getStage())) {
					log("Has no Stage " + c.getStage() + " certs for " + c.getName());
					return false;
				}
				
				break;
				
			case Certification.REQ_ALLPRIOR :
				if (!hasAll(c.getStage() - 1)) {
					log("Missing Stage " + c.getStage() + " cert for " + c.getName());
					return false;
				}
				
				break;
				
			case Certification.REQ_SPECIFIC:
				Certification prCert = _certs.get(c.getReqCert());
				if (prCert == null)
					log.warn("No Certification called " + c.getReqCert());
				else if (!hasPassed(prCert.getName())) {
					log("Missing pre-requisite " + prCert.getName() + " cert for " + c.getName());
					return false;
				}
				
				break;
				
			case Certification.REQ_ANY:
			default:
				return true;
		}
		
		// If we got this far, we can take it
		return true;
	}
	
	/**
	 * Rerturns if the user is eligible to write a particular examination.
	 * @param ep the ExamProfile bean
	 * @return TRUE if the exam can be taken, othewise FALSE
	 */
	public boolean canWrite(ExamProfile ep) {
		
		// If it's not part of the Flight Academy, then no
		if (!ep.getAcademy())
			return false;
		
		// If we've already passed the exam, then no
		if (passedExam(ep.getName()) || hasSubmitted(ep.getName())) {
			log("Already submitted/passed " + ep.getName());
			return false;
		}
		
		// Determine if we are enrolled anywhere
		Course cr = getCurrentCourse();
		if (cr == null) {
			log("Cannot take " + ep.getName() + " Not enrolled in a course");
			return false;
		} else if (cr.getStatus() == Status.PENDING) {
			log("Cannot take " + ep.getName() + " Course pending");
			return false;
		}
		
		// Get the cert and see if it is included 
		Certification c = _certs.get(cr.getName());
		return ((c != null) && (c.getExamNames().contains(ep.getName())));
	}
	
	/**
	 * Returns if the user is locked out of the Testing Center due to a failed examination.
	 * @param lockoutHours the number of hours to remain locked out, or zero if no lockout
	 * @return TRUE if the user is locked out, otherwise FALSE
	 */
	public boolean isLockedOut(int lockoutHours) {

		// If there is no last examination, or lockout period forget it
		if (_tests.isEmpty() || (lockoutHours < 1))
			return false;

		// If the last test is not an examination, forget it
		Test t = _tests.last();
		if (!(t instanceof Examination))
			return false;

		// If the test is not scored and failed, then forget
		if (t.getStatus() != TestStatus.SCORED)
			return false;
		else if (t.getPassFail())
			return false;

		// Check the time from the scoring
		long timeInterval = (System.currentTimeMillis() - t.getScoredOn().toEpochMilli()) / 1000;
		log.info("Exam Lockout: interval = " + timeInterval + "s, period = " + (lockoutHours * 3600) + "s");
		return (timeInterval < (lockoutHours * 3600L));
	}
}