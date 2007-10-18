// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

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
	
	private final String _qName = SystemData.get("airline.code") + " " + Examination.QUESTIONNAIRE_NAME;

	private Pilot _usr;
	private EquipmentType _myEQ;
	private boolean _debugLog;

	private final SortedSet<Test> _tests = new TreeSet<Test>();
	private Collection<FlightReport> _pireps;
	private final Collection<EquipmentType> _allEQ = new TreeSet<EquipmentType>();

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
	 * Initializes the collection of Equipment programs.
	 * @param eqTypes a Collection of EquipmentType beans
	 */
	public void setEquipmentTypes(Collection<EquipmentType> eqTypes) {
		_allEQ.addAll(eqTypes);
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
	 * Returns the Pilot's examinations. This method is useful when we use this class and do not wish to call the
	 * {@link org.deltava.dao.GetExam} DAO a second time.
	 * @return a Collection of Test beans
	 */
	public Collection<Test> getExams() {
		return _tests;
	}

	/**
	 * Helper method to retrieve all equipment types for a given stage.
	 */
	private Collection<EquipmentType> getTypes(int stage) {
		Collection<EquipmentType> results = new HashSet<EquipmentType>();
		for (Iterator<EquipmentType> i = _allEQ.iterator(); i.hasNext();) {
			EquipmentType eq = i.next();
			if (eq.getStage() == stage)
				results.add(eq);
		}

		return results;
	}

	/**
	 * Returns wether a Pilot qualifies for Captain's rank in a particular stage.
	 * @return TRUE if the Pilot has passed the Captain's exam and flown the necessary legs in <i>ANY</i> equipment
	 *         program in a particular stage.
	 */
	public boolean isCaptainInStage(int stage) {
		
		// Check for staff member
		if (Ranks.RANK_ACP.equals(_usr.getRank()) || Ranks.RANK_CP.equals(_usr.getRank()))
			return true;

		// Check if we're already a captain
		if ((StringUtils.arrayIndexOf(CAPT_RANKS, _usr.getRank()) != -1) && (stage == _myEQ.getStage()))
			return true;

		// Iterate through the equipment types in a stage
		Collection<EquipmentType> eqTypes = getTypes(stage);
		for (Iterator<EquipmentType> i = eqTypes.iterator(); i.hasNext();) {
			EquipmentType eq = i.next();
			if (promotionEligible(eq))
				return true;
		}

		return false;
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
			if ((t instanceof Examination) && (!_qName.equals(t.getName())) && (t.getPassFail()))
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
		if (_qName.equals(ep.getName())) {
			log(ep.getName() + " is the Questionnaire");
			return false;
		}

		// If it's part of the Flight Academy, no
		if (ep.getAcademy())
			return false;

		// Check if we've passed or submitted the exam
		if (hasPassed(Collections.singleton(ep.getName())) || hasSubmitted(ep.getName())) {
			log(ep.getName() + " is passed / submitted");
			return false;
		}

		// Check if it's the FO exam for the current program
		if (_myEQ.getExamNames(Ranks.RANK_FO).contains(ep.getName())) {
			log(ep.getName() + " is FO exam for " + _myEQ.getName());
			return false;
		}

		// Check if we are in the proper equipment program
		if (!StringUtils.isEmpty(ep.getEquipmentType())) {
			if (!ep.getEquipmentType().equals(_usr.getEquipmentType())) {
				log(ep.getName() + " eqType=" + ep.getEquipmentType() + ", our eqType=" + _usr.getEquipmentType());
				return false;
			}

			// If the exam is limited to a specific equipment program, require 1/2 the legs required for promotion
			if (getFlightLegs(_myEQ) < (_myEQ.getPromotionLegs(Ranks.RANK_C))) {
				log(ep.getName() + " Our Flight Legs=" + getFlightLegs(_myEQ));
				return false;
			}
		}

		// Check if we've reached the proper minimum stage
		if (ep.getMinStage() > getMaxCheckRideStage()) {
			log(ep.getName() + " minStage=" + ep.getMinStage() + ", our maxCheckRideStage=" + getMaxCheckRideStage());
			return false;
		}

		// If the exam is a higher stage than us, require Captan's rank in the stage below
		if ((ep.getStage() > getMaxCheckRideStage()) && !isCaptainInStage(ep.getStage() - 1)) {
			log(ep.getName() + " stage=" + ep.getStage() + ", our Stage=" + getMaxCheckRideStage()
					+ ", not Captain in stage " + (ep.getStage() - 1));
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
		
		// If it's not in our airline, don't allow it
		if (!SystemData.get("airline.code").equals(eq.getOwner().getCode())) {
			log(eq.getName() + " is a " + eq.getOwner().getName() + " program");
			return false;
		}

		// If it's a stage 1 program, allow the transfer
		if (eq.getStage() == 1)
			return true;

		// Check if we've passed the FO exam for that program
		if (!hasPassed(eq.getExamNames(Ranks.RANK_FO))) {
			log("Haven't passed " + eq.getExamNames(Ranks.RANK_FO));
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
	 * Returns if the Pilot has completed enough flight legs in their current program to request a switch or additional
	 * ratings.
	 * @return TRUE if the Pilot has completed one half of the legs required for promotion to Captain, otherwise FALSE
	 */
	public boolean canRequestSwitch() {
		return (getFlightLegs(_myEQ) >= (_myEQ.getPromotionLegs(Ranks.RANK_C) / 2));
	}

	/**
	 * Returns if a user can request a Check Ride to move to a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user can request a check ride, otherwise FALSE
	 */
	public boolean canRequestCheckRide(EquipmentType eq) {

		// Make sure we're a captain in the previous stage if the stage is higher than our own
		if ((eq.getStage() > getMaxCheckRideStage()) && (!isCaptainInStage(eq.getStage() - 1)))
			return false;

		// Check if we've passed the FO/CAPT exam for that program
		if (!hasPassed(eq.getExamNames(Ranks.RANK_FO)) && !hasPassed(eq.getExamNames(Ranks.RANK_C)))
			return false;

		// Make sure we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName()))
			return false;

		// Check if we don't have a checkride in that equipment program
		if (hasCheckRide(eq))
			return false;

		// If we require a checkride, ensure we have a minimum number of legs
		return canRequestSwitch();
	}
	
	/**
	 * Returns if a Pilot can request additional ratings in an Equipment Type. This checks if the
	 * equipment type has any ratings that the Pilot does not current have.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user is missing any ratings, otherwise FALSE
	 */
	public boolean canRequestRatings(EquipmentType eq) {
		Collection<String> ratings = new HashSet<String>(eq.getPrimaryRatings());
		ratings.addAll(eq.getSecondaryRatings());
		Collection<String> extraRatings = CollectionUtils.getDelta(ratings, _usr.getRatings());
		return !extraRatings.isEmpty();
	}

	/**
	 * Returns if we can promote a user to Captain within the equipment program. <i>This is essentially the same call as
	 * {@link TestingHistoryHelper#promotionEligible(EquipmentType)} except that we also check if we are a First Officer
	 * in the specific program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user is eligible to be promoted to captain, otherwise FALSE
	 * @see TestingHistoryHelper#promotionEligible(EquipmentType)
	 */
	public boolean canPromote(EquipmentType eq) {

		// Check if we're a First Officer
		if (!_usr.getRank().equals(Ranks.RANK_FO))
			return false;

		// Check if we're otherwise eligible
		return promotionEligible(eq);
	}

	/**
	 * Returns wether we could have promoted the user to Captain within an equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user is eligible to be promoted to captain, otherwise FALSE
	 */
	public boolean promotionEligible(EquipmentType eq) {

		// Check if we've passed the examinations
		if (!hasPassed(eq.getExamNames(Ranks.RANK_C)))
			return false;

		// Check if we've got enough flight legs in the primary equipment type
		return (getFlightLegs(eq) >= eq.getPromotionLegs(Ranks.RANK_C));
	}

	/**
	 * Returns if a user has passed particular Examinations.
	 * @param examNames a Collection of Examination names
	 * @return TRUE if the user has passed these Examinations, otherwise FALSE
	 */
	public boolean hasPassed(Collection<String> examNames) {
		Collection<String> names = new HashSet<String>(examNames);
		for (Iterator<Test> i = _tests.iterator(); i.hasNext() && !names.isEmpty(); ) {
			Test t = i.next();
			if (t.getPassFail())
				names.remove(t.getName());
		}

		return names.isEmpty();
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
		if (t.getStatus() != Test.SCORED)
			return false;
		else if (t.getPassFail())
			return false;

		// Check the time from the scoring
		long timeInterval = (System.currentTimeMillis() - t.getScoredOn().getTime()) / 1000;
		log("Exam Lockout: interval = " + timeInterval + "s, period = " + (lockoutHours * 3600) + "s");
		return (timeInterval < (lockoutHours * 3600));
	}
}