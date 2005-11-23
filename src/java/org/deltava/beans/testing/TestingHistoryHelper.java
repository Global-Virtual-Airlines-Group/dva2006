// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A helper class to extract information from a user's examination/check ride history.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TestingHistoryHelper {
   
   // Arbitrary max exam stage used for Chief Pilots and Assistants
   private static final int CP_STAGE = 4; 
   private static final String[] CAPT_RANKS = {Ranks.RANK_C, Ranks.RANK_SC};

	private Pilot _usr;
	private EquipmentType _myEQ;
	private boolean _isCaptain;

	private Collection<Test> _tests;
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
		_tests = (tests == null) ? new HashSet<Test>() : tests;
		_pireps = pireps;
		
		// Check if we're a captain
		_isCaptain = (StringUtils.arrayIndexOf(CAPT_RANKS, _usr.getRank()) != -1);
	}

	/**
	 * Adds an Examination to the Pilot's test history. 
	 * @param ex the Examiantion
	 */
	public void addExam(Examination ex) {
		_tests.add(ex);
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
			if ((t instanceof Examination) && (!Examination.QUESTIONNAIRE_NAME.equals(t.getName())) && (t.getPassFail()))
				maxStage = Math.max(maxStage, t.getStage());
		}

		return maxStage;
	}

	/**
	 * Returns the highest stage Check Ride this user has passed.
	 * @return the stage number of the highest check ride, or 1 if none passed
	 * @see Test#getStage()
	 */
	public int getMaxCheckRideStage() {

		int maxStage = 1;
		for (Iterator<Test> i = _tests.iterator(); i.hasNext();) {
			Test t = i.next();
			if ((t instanceof CheckRide) && t.getPassFail())
				maxStage = Math.max(maxStage, t.getStage());
		}

		return maxStage;
	}
	
	/**
	 * Returns the number of flight legs counted towards promotion in a particular Equipment Program. If no
	 * Equipment Program is specified, this returns the total number of approved flight legs.
	 * @param eq the Equipment Program
	 * @return the number of legs
	 */
	public int getFlightLegs(EquipmentType eq) {
	   int result = 0;
	   for (Iterator<FlightReport> i = _pireps.iterator(); i.hasNext(); ) {
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
		if (!ep.getActive())
			return false;
		
		// If it's the Initial Questionnaire, uh uh
		if (Examination.QUESTIONNAIRE_NAME.equals(ep.getName()))
			return false;

		// Check if we've passed or submitted the exam
		if (hasPassed(ep.getName()) || hasSubmitted(ep.getName()))
			return false;

		// Check if we are in the proper equipment program
		if (!StringUtils.isEmpty(ep.getEquipmentType())) {
			if (!ep.getEquipmentType().equals(_usr.getEquipmentType()))
				return false;
		}

		// Check if we've reached the proper minimum stage
		if (ep.getMinStage() > getMaxExamStage())
			return false;
		
		// Check if we have at least 5 approved flights
		return (getFlightLegs(_myEQ) >= 5);
	}

	/**
	 * Returns if a user has met all the requirements for switching to a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user can switch to the equipment program, otherwise FALSE
	 */
	public boolean canSwitchTo(EquipmentType eq) {

		// Make sure we're a captain if the stage is higher than our own
		if ((eq.getStage() > _myEQ.getStage()) && (!_isCaptain))
			return false;

		// Check if we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName()))
			return false;

		// If it's stage one, then assume yes
		if (eq.getStage() == 1)
			return true;
		
		// Check if we've passed the FO exam for that program
		if (!hasPassed(eq.getExamName(Ranks.RANK_FO)))
			return false;
		
		// Check if we have a checkride in that equipment's stage
		return (eq.getStage() <= getMaxCheckRideStage());
	}

	/**
	 * Returns if a user can request a Check Ride to move to a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user can request a check ride, otherwise FALSE
	 */
	public boolean canRequestCheckRide(EquipmentType eq) {

		// Make sure we're a captain if the stage is higher than our own
		if ((eq.getStage() > _myEQ.getStage()) && (!_isCaptain))
			return false;
		
		// Make sure the new stage isn't the same or lower than our current stage
		if ((eq.getStage() <= _myEQ.getStage()))
		   return false;

		// Check if we've passed the FO exam for that program
		if (!hasPassed(eq.getExamName(Ranks.RANK_FO)))
			return false;

		// Make sure we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName()))
			return false;
		
		// Check if we don't have a checkride in that equipment's stage
		return (eq.getStage() > getMaxCheckRideStage());
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

		// If we got this far, we didn't pass
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

		// If we got this far, we didn't submit
		return false;
	}
}