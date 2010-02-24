// Copyright 2005, 2006, 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A helper class to extract information from a user's examination/check ride history.
 * @author Luke
 * @version 3.0
 * @since 1.0
 */

public class TestingHistoryHelper {
	
	private static final Logger log = Logger.getLogger(TestingHistoryHelper.class);

	// Arbitrary max exam stage used for Chief Pilots and Assistants
	private static final int CP_STAGE = 5;
	private static final List<String> CAPT_RANKS = Arrays.asList(Ranks.RANK_C, Ranks.RANK_SC);
	
	private final String _qName = SystemData.get("airline.code") + " " + Examination.QUESTIONNAIRE_NAME;

	private Pilot _usr;
	private EquipmentType _myEQ;
	private final SortedSet<Test> _tests = new TreeSet<Test>();
	private final Collection<FlightReport> _pireps = new ArrayList<FlightReport>();
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
		if (pireps != null)
			_pireps.addAll(pireps);
		if (tests != null)
			_tests.addAll(tests);
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
		for (EquipmentType eq : _allEQ) {
			if (eq.getStage() == stage)
				results.add(eq);
		}

		return results;
	}

	/**
	 * Returns whether a Pilot qualifies for Captain's rank in a particular stage.
	 * @return TRUE if the Pilot has passed the Captain's exam and flown the necessary legs
	 * in <i>ANY</i> equipment program in a particular stage.
	 */
	public boolean isCaptainInStage(int stage) {
		
		// Check for staff member
		if (Ranks.RANK_ACP.equals(_usr.getRank()) || Ranks.RANK_CP.equals(_usr.getRank()))
			return true;

		// Check if we're already a captain
		if ((CAPT_RANKS.contains(_usr.getRank())) && (stage == _myEQ.getStage()))
			return true;

		// Iterate through the equipment types in a stage
		for (EquipmentType eq : getTypes(stage)) {
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
		for (Test t : _tests) {
			if ((t instanceof Examination) && (!_qName.equals(t.getName())) && t.getPassFail()
					&& SystemData.get("airline.code").equals(t.getOwner().getCode()))
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
		for (Test t : _tests) {
			if ((t instanceof CheckRide) && t.getPassFail() && SystemData.get("airline.code").equals(t.getOwner().getCode()))
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
		for (FlightReport fr : _pireps) {
			if (fr.getStatus() == FlightReport.OK) {
				if ((eq == null) || (fr.getCaptEQType().contains(eq.getName())))
					result++;
			}
		}

		// Return results
		return result;
	}

	/**
	 * Checks if the user is eligible to take a particular Examination.
	 * @param ep the Examination to take
	 * @throws IneligibilityException if one is not eligible
	 */
	public void canWrite(ExamProfile ep) throws IneligibilityException {
		// If the exam isn't active, we cannot write it
		if (!ep.getActive())
			throw new IneligibilityException(ep.getName() + " inactive");

		// If it's the Initial Questionnaire, uh uh
		if (_qName.equals(ep.getName()))
			throw new IneligibilityException(ep.getName() + " is the Questionnaire");

		// If it's part of the Flight Academy, no
		if (ep.getAcademy())
			throw new IneligibilityException(ep.getName() + " is a Flight Academy examination");

		// Check if we've passed or submitted the exam
		if (hasPassed(Collections.singleton(ep.getName())) || hasSubmitted(ep.getName()))
			throw new IneligibilityException(ep.getName() + " is passed / submitted");

		// Check if it's the FO exam for the current program
		if (_myEQ.getExamNames(Ranks.RANK_FO).contains(ep.getName()))
			throw new IneligibilityException(ep.getName() + " is FO exam for " + _myEQ.getName());

		// Check if we are in the proper equipment program
		if (!StringUtils.isEmpty(ep.getEquipmentType())) {
			if (!ep.getEquipmentType().equals(_usr.getEquipmentType()))
				throw new IneligibilityException(ep.getName() + " eqType=" + ep.getEquipmentType() + ", our eqType=" + _usr.getEquipmentType());

			// If the exam is limited to a specific equipment program, require 1/2 the legs required for promotion
			if (getFlightLegs(_myEQ) < (_myEQ.getPromotionLegs()))
				throw new IneligibilityException(ep.getName() + " Our Flight Legs=" + getFlightLegs(_myEQ));
		}

		// Check if we've reached the proper minimum stage
		if (ep.getMinStage() > getMaxCheckRideStage())
			throw new IneligibilityException(ep.getName() + " minStage=" + ep.getMinStage() + ", our maxCheckRideStage=" + getMaxCheckRideStage());

		// If the exam is a higher stage than us, require Captan's rank in the stage below
		if ((ep.getStage() > getMaxCheckRideStage()) && !isCaptainInStage(ep.getStage() - 1))
			throw new IneligibilityException(ep.getName() + " stage=" + ep.getStage() + ", our Stage=" + getMaxCheckRideStage()
					+ ", not Captain in stage " + (ep.getStage() - 1));

		// Check if we've been locked out of exams
		if (_usr.getNoExams())
			throw new IneligibilityException("Testing Center locked out");
	}

	/**
	 * Returns if a user has met all the requirements for switching to a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @throws IneligibilityException if the user cannot switch to the program
	 */
	public void canSwitchTo(EquipmentType eq) throws IneligibilityException {

		// Check if we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName()))
			throw new IneligibilityException("Already in " + eq.getName() + " program");
		
		// If it's not in our airline, don't allow it
		if (!SystemData.get("airline.code").equals(eq.getOwner().getCode()))
			throw new IneligibilityException(eq.getName() + " is a " + eq.getOwner().getName() + " program");

		// Check if we've passed the FO exam for that program
		if (!hasPassed(eq.getExamNames(Ranks.RANK_FO)))
			throw new IneligibilityException("Haven't passed " + eq.getExamNames(Ranks.RANK_FO));

		// Check if we have a checkride in that equipment
		if (!hasCheckRide(eq))
			throw new IneligibilityException("Haven't passed " + eq.getName() + " check ride");
	}

	/**
	 * Returns if the Pilot has completed enough flight legs in their current program to request a switch or additional
	 * ratings.
	 * @return TRUE if the Pilot has completed one half of the legs required for promotion to Captain, otherwise FALSE
	 */
	public boolean canRequestSwitch() {
		return (getFlightLegs(_myEQ) >= (_myEQ.getPromotionLegs() / 2));
	}

	/**
	 * Returns if a user can request a Check Ride to move to a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @throws IneligibilityException if the user cannot request a check ride
	 */
	public void canRequestCheckRide(EquipmentType eq) throws IneligibilityException {

		// Make sure we're a captain in the previous stage if the stage is higher than our own
		if ((eq.getStage() > getMaxCheckRideStage()) && (!isCaptainInStage(eq.getStage() - 1)))
			throw new IneligibilityException("Must be Captain in Stage " + (eq.getStage() - 1));

		// Check if we've passed the FO/CAPT exam for that program
		if (!hasPassed(eq.getExamNames(Ranks.RANK_FO)) && !hasPassed(eq.getExamNames(Ranks.RANK_C)))
			throw new IneligibilityException("Has not passed FO/Captain Examination");

		// Make sure we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName()))
			throw new IneligibilityException("Already in " + eq.getName() + " program");

		// Check if we don't have a checkride in that equipment program
		if (hasCheckRide(eq))
			throw new IneligibilityException("Has already passed Check Ride");

		// If we require a checkride, ensure we have a minimum number of legs
		if (!canRequestSwitch())
			throw new IneligibilityException("Has not completed " + (_myEQ.getPromotionLegs() / 2) + " legs for promotion");
	}
	
	/**
	 * Returns if a Pilot can request additional ratings in an Equipment Type. This checks if the
	 * equipment type has any ratings that the Pilot does not current have.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user is missing any ratings, otherwise FALSE
	 */
	public boolean canRequestRatings(EquipmentType eq) {
		Collection<String> extraRatings = CollectionUtils.getDelta(eq.getRatings(), _usr.getRatings());
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
		return (getFlightLegs(eq) >= eq.getPromotionLegs());
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
		for (Test t : _tests) {
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
		for (Test t : _tests) {
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
		log.info("Exam Lockout: interval = " + timeInterval + "s, period = " + (lockoutHours * 3600) + "s");
		return (timeInterval < (lockoutHours * 3600));
	}
}