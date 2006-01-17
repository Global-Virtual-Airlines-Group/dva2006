// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A helper class to extract information from a user's examination/check ride history.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TestingHistoryHelper {

	private static final Logger log = Logger.getLogger(TestingHistoryHelper.class);

	// Arbitrary max exam stage used for Chief Pilots and Assistants
	private static final int CP_STAGE = 4;
	private static final String[] CAPT_RANKS = { Ranks.RANK_C, Ranks.RANK_SC };

	private Pilot _usr;
	private EquipmentType _myEQ;
	private boolean _isCaptain;
	private boolean _debugLog;

	private Collection<Test> _tests = new TreeSet<Test>();
	private Collection<FlightReport> _pireps;

	/**
	 * Initializes the helper.
	 * @param p the Pilot bean
	 * @param myEQ the Pilot's Equipment program
	 * @param tests a Collection of checkride/examination objects, representing this Pilot's exam history
	 * @param pireps a Collection of FlightReport beans <i>with the CaptEQType property populated</i>
	 */
	public TestingHistoryHelper(Pilot p, EquipmentType myEQ, Collection<Test> tests, Collection<FlightReport> pireps) {
		super();
		_usr = p;
		_myEQ = myEQ;
		_pireps = pireps;
		if (tests != null)
			_tests.addAll(tests);

		// Check if we're a captain
		_isCaptain = (StringUtils.arrayIndexOf(CAPT_RANKS, _usr.getRank()) != -1);
	}

	private void log(String msg) {
		if (_debugLog)
			log.warn(msg);
	}

	/**
	 * Adds an Examination to the Pilot's test history.
	 * @param ex the Examiantion
	 */
	public void addExam(Examination ex) {
		_tests.add(ex);
	}

	/**
	 * Toggles the debugging log.
	 * @param isDebug TRUE if the log is active, otherwise FALSE
	 */
	public void setDebug(boolean isDebug) {
		_debugLog = isDebug;
	}

	/**
	 * Returns the Pilot's equipment program. This method is useful when we use this class and do not wish to call the
	 * {@link org.deltava.dao.GetEquipmentType} DAO a second time.
	 * @return the EquipmentType bean
	 */
	public EquipmentType getEquipmentType() {
		return _myEQ;
	}

	/**
	 * Returns the Pilot's examinations. Returns the Pilot's equipment program. This method is useful when we use this
	 * class and do not wish to call the {@link org.deltava.dao.GetExam} DAO a second time.
	 * @return a List of Test beans
	 */
	public Collection<Test> getExams() {
		return _tests;
	}

	/**
	 * Returns the highest stage Examination this user has passed. This will return 4
	 * @return the stage number of the highest examination, or 1 if none passed
	 * @see Test#getStage()
	 */
	public int getMaxExamStage() {

		// Check for staff member
		if (Ranks.RANK_ACP.equals(_usr.getRank()) || Ranks.RANK_CP.equals(_usr.getRank()))
			return CP_STAGE;

		int maxStage = 1;
		for (Iterator i = _tests.iterator(); i.hasNext();) {
			Test t = (Test) i.next();
			if ((t instanceof Examination) && (!Examination.QUESTIONNAIRE_NAME.equals(t.getName()))
					&& (t.getPassFail()))
				maxStage = Math.max(maxStage, t.getStage());
		}

		return maxStage;
	}

	/**
	 * Returns the highest stage Check Ride this user has passed.
	 * @return the stage number of the highest check ride, or the current stage if none passed
	 * @see Test#getStage()
	 * @see EquipmentType#getStage()
	 */
	public int getMaxCheckRideStage() {

		int maxStage = _myEQ.getStage();
		for (Iterator<Test> i = _tests.iterator(); i.hasNext();) {
			Test t = i.next();
			if ((t instanceof CheckRide) && t.getPassFail())
				maxStage = Math.max(maxStage, t.getStage());
		}

		return maxStage;
	}

	/**
	 * Returns the number of flight legs counted towards promotion in a particular Equipment Program. If no Equipment
	 * Program is specified, this returns the total number of approved flight legs.
	 * @param eq the Equipment Program
	 * @return the number of legs
	 */
	public int getFlightLegs(EquipmentType eq) {
		int result = 0;
		for (Iterator<FlightReport> i = _pireps.iterator(); i.hasNext();) {
			FlightReport fr = i.next();
			if (fr.getStatus() == FlightReport.OK) {
				if ((eq == null) || (fr.getCaptEQType().contains(eq.getName())))
					result++;
			}
		}

		// Return results
		return result;
	}

	/**
	 * Returns if the user is eligible to take a particular Examination.
	 * @param ep the Examination to take
	 * @return TRUE if the user can take the Examination, otherwise FALSE
	 */
	public boolean canWrite(ExamProfile ep) {
		// If the exam isn't active, we cannot write it
		if (!ep.getActive()) {
			log(ep.getName() + " inactive");
			return false;
		}

		// If it's the Initial Questionnaire, uh uh
		if (Examination.QUESTIONNAIRE_NAME.equals(ep.getName())) {
			log(ep.getName() + " is the Initial Questionnaire");
			return false;
		}

		// Check if we've passed or submitted the exam
		if (hasPassed(ep.getName()) || hasSubmitted(ep.getName())) {
			log(ep.getName() + " is passed / submitted");
			return false;
		}

		// Check if we are in the proper equipment program
		if (!StringUtils.isEmpty(ep.getEquipmentType())) {
			if (!ep.getEquipmentType().equals(_usr.getEquipmentType())) {
				log(ep.getName() + " eqType=" + ep.getEquipmentType() + ", our eqType=" + _usr.getEquipmentType());
				return false;
			}

			// If the exam is limited to a specific equipment program, require 1/2 the legs required for promotion
			if (getFlightLegs(_myEQ) < (_myEQ.getPromotionLegs(Ranks.RANK_C) / 2)) {
				log(ep.getName() + " Our Flight Legs=" + getFlightLegs(_myEQ));
				return false;
			}
		}

		// Check if we've reached the proper minimum stage
		if (ep.getMinStage() > getMaxCheckRideStage()) {
			log(ep.getName() + " minStage=" + ep.getMinStage() + ", our maxCheckRideStage=" + getMaxCheckRideStage());
			return false;
		}

		// If the exam is a higher stage than us, require Captan's rank
		if ((ep.getStage() > getMaxCheckRideStage()) && !_isCaptain) {
			log(ep.getName() + " stage=" + ep.getStage() + ", our Stage=" + getMaxCheckRideStage() + ", not Captain");
			return false;
		}

		// Check if we've been locked out of exams
		return !_usr.getNoExams();
	}

	/**
	 * Returns if a user has met all the requirements for switching to a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user can switch to the equipment program, otherwise FALSE
	 */
	public boolean canSwitchTo(EquipmentType eq) {

		// Check if we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName())) {
			log("Already in " + eq.getName() + " program");
			return false;
		}
		
		// If it's a stage 1 program, allow the transfer
		if (eq.getStage() == 1)
			return true;

		// Check if we've passed the FO exam for that program
		if (!hasPassed(eq.getExamName(Ranks.RANK_FO))) {
			log("Haven't passed " + eq.getExamName(Ranks.RANK_FO));
			return false;
		}

		// Check if we have a checkride in that equipment
		if (!hasCheckRide(eq)) {
			log("No " + eq.getName() + " check ride");
			return false;
		}

		return true;
	}

	/**
	 * Returns if a user can request a Check Ride to move to a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user can request a check ride, otherwise FALSE
	 */
	public boolean canRequestCheckRide(EquipmentType eq) {

		// Make sure we're a captain if the stage is higher than our own
		if ((eq.getStage() > getMaxCheckRideStage()) && (!_isCaptain))
			return false;

		// Check if we've passed the FO exam for that program
		if (!hasPassed(eq.getExamName(Ranks.RANK_FO)))
			return false;

		// Make sure we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName()))
			return false;

		// Check if we don't have a checkride in that equipment program
		if (hasCheckRide(eq))
			return false;

		// If we require a checkride, ensure we have a minimum number of legs
		return (getFlightLegs(_myEQ) >= (_myEQ.getPromotionLegs(Ranks.RANK_C) / 2));
	}

	/**
	 * Returns if we can promote a user to Captain within the equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user is eligible to be promoted to captain, otherwise FALSE.
	 */
	public boolean canPromote(EquipmentType eq) {

		// Check if we're a First Officer
		if (!_usr.getRank().equals(Ranks.RANK_FO))
			return false;

		// Check if we've passed the examination
		if (!hasPassed(eq.getExamName(Ranks.RANK_C)))
			return false;

		// Check if we've got enough flight legs in the primary equipment type
		return (getFlightLegs(eq) >= eq.getPromotionLegs(Ranks.RANK_C));
	}

	/**
	 * Returns if a user has passed a particular Examination.
	 * @param examName the Examination name
	 * @return TRUE if the user has passed this Examination, otherwise FALSE
	 */
	public boolean hasPassed(String examName) {
		for (Iterator<Test> i = _tests.iterator(); i.hasNext();) {
			Test t = i.next();
			if (t.getPassFail() && (t.getName().equals(examName)))
				return true;
		}

		return false;
	}

	/**
	 * Returns if a user has submitted a particular Examination.
	 * @param examName the Examination name
	 * @return TRUE if the user has submitted this Examination, otherwise FALSE
	 */
	public boolean hasSubmitted(String examName) {
		for (Iterator<Test> i = _tests.iterator(); i.hasNext();) {
			Test t = i.next();
			if ((t.getStatus() == Test.SUBMITTED) && (t.getName().equals(examName)))
				return true;
		}

		return false;
	}

	/**
	 * Returns if a user has passed a checkride for a particular equipment type.
	 * @param eq the Equipment Type bean
	 * @return TRUE if the user passed the check ride, otherwise FALSE
	 */
	public boolean hasCheckRide(EquipmentType eq) {
		for (Iterator<Test> i = _tests.iterator(); i.hasNext();) {
			Test t = i.next();
			if ((t instanceof CheckRide) && (t.getPassFail())) {
				CheckRide cr = (CheckRide) t;
				if (cr.getEquipmentType().equals(eq.getName()))
					return true;
			}
		}

		return false;
	}
}