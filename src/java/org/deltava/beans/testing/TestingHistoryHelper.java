// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.deltava.beans.*;

/**
 * A helper class to extract information from a user's examination/check ride history.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TestingHistoryHelper {

	private Pilot _usr;
	private EquipmentType _myEQ;

	private Collection _tests;
	private Collection _pireps;

	/**
	 * Initializes the helper.
	 * @param p the Pilot bean
	 * @param tests a List of checkride/examination objects, representing this Pilot's exam history
	 */
	public TestingHistoryHelper(Pilot p, EquipmentType myEQ, Collection tests, Collection pireps) {
		super();
		_usr = p;
		_myEQ = myEQ;
		_tests = (tests == null) ? Collections.EMPTY_LIST : tests;
		_pireps = pireps;
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
	public Collection getExams() {
		return _tests;
	}

	/**
	 * Returns the highest stage Examination this user has passed.
	 * @return the stage number of the highest examination, or 1 if none passed
	 * @see Test#getStage()
	 */
	public int getMaxExamStage() {

		int maxStage = 1;
		for (Iterator i = _tests.iterator(); i.hasNext();) {
			Test t = (Test) i.next();
			if (t instanceof Examination)
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
		for (Iterator i = _tests.iterator(); i.hasNext();) {
			Test t = (Test) i.next();
			if ((t instanceof CheckRide) && t.getPassFail())
				maxStage = Math.max(maxStage, t.getStage());
		}

		return maxStage;
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

		// Check if we've passed or submitted the exam
		if (hasPassed(ep.getName()) || hasSubmitted(ep.getName()))
			return false;

		// Check if we are in the proper equipment program
		if (ep.getEquipmentType() != null)
			if (!ep.getEquipmentType().equals(_usr.getEquipmentType()))
				return false;

		// Check if we've reached the proper minimum stage
		if (ep.getMinStage() > getMaxExamStage())
			return false;

		// If we got this far, we can take the exam
		return true;
	}

	/**
	 * Returns if a user has met all the requirements for switching to a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user can switch to the equipment program, otherwise FALSE
	 */
	public boolean canSwitchTo(EquipmentType eq) {

		// Make sure we're a captain if the stage is higher than our own
		if ((eq.getStage() > _myEQ.getStage()) && (!_usr.getRank().equals(Ranks.RANK_C)))
			return false;

		// Check if we've passed the FO exam for that program
		if (!hasPassed(eq.getExamName(Ranks.RANK_FO)))
			return false;

		// Check if we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName()))
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
		if ((eq.getStage() > _myEQ.getStage()) && (!_usr.getRank().equals(Ranks.RANK_C)))
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
	 * Returns if we can promote a user to Captain within the equipment program. <i>The Pilot bean must have its Flight
	 * Reports populated before calling this method. </i>
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
		int cpLegs = 0;
		if (_pireps != null) {
			for (Iterator i = _pireps.iterator(); i.hasNext();) {
				FlightReport fr = (FlightReport) i.next();
				if ((fr.getStatus() == FlightReport.OK) && (fr.getCaptEQType().equals(eq.getName())))
					cpLegs++;
			}
		}

		// Check if we've got the legs
		return (cpLegs >= eq.getPromotionLegs(Ranks.RANK_C));
	}

	/**
	 * Returns if a user has passed a particular Examination.
	 * @param examName the Examination name
	 * @return TRUE if the user has passed this Examination, otherwise FALSE
	 */
	public boolean hasPassed(String examName) {
		for (Iterator i = _tests.iterator(); i.hasNext();) {
			Test t = (Test) i.next();
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
		for (Iterator i = _tests.iterator(); i.hasNext();) {
			Test t = (Test) i.next();
			if ((t.getStatus() == Test.SUBMITTED) && (t.getName().equals(examName)))
				return true;
		}

		// If we got this far, we didn't submit
		return false;
	}
}