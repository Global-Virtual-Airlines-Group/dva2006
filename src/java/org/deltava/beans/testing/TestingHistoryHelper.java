// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A helper class to extract information from a user's examination/check ride history.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

@Helper(Test.class)
public final class TestingHistoryHelper {
	
	private static final Logger log = Logger.getLogger(TestingHistoryHelper.class);

	// Arbitrary max exam stage used for Chief Pilots and Assistants
	private static final int CP_STAGE = 6;
	
	private final String _qName = SystemData.get("airline.code") + " " + Examination.QUESTIONNAIRE_NAME;

	private final Pilot _usr;
	private final EquipmentType _myEQ;
	private final SortedSet<Test> _tests = new TreeSet<Test>(new HistoryTestComparator());
	private final Collection<FlightReport> _pireps = new ArrayList<FlightReport>();
	private final Collection<EquipmentType> _allEQ = new TreeSet<EquipmentType>();
	
	/**
	 * Utility class to compare Examinations by checking the ID, then the scoring date if the ID is zero and they are unsaved waivers.
	 */
	static class HistoryTestComparator implements Comparator<Test> {

		@Override
		public int compare(Test t1, Test t2) {
			Instant d1 = (t1.getScoredOn() == null) ? t1.getDate() : t1.getScoredOn();
			Instant d2 = (t2.getScoredOn() == null) ? t2.getDate() : t2.getScoredOn();
			int tmpResult = d1.compareTo(d2);
			return (tmpResult == 0) ? Integer.compare(t1.getID(), t2.getID()) : tmpResult;
		}
	}

	/**
	 * Utilty class to compare check rides by equipment program only, used to limit Sets to a single ride per program. 
	 */
	static class ExpiringRideComparator implements Comparator<CheckRide> {

		@Override
		public int compare(CheckRide cr1, CheckRide cr2) {
			return cr1.getEquipmentType().compareTo(cr2.getEquipmentType());
		}
	}

	/**
	 * Utility class to sort check rides by expiration date.
	 */
	static class RideExpireComparator implements Comparator<CheckRide> {

		@Override
		public int compare(CheckRide cr1, CheckRide cr2) {
			Instant e1 = (cr1.getExpirationDate() ==  null) ? Instant.MAX : cr1.getExpirationDate();
			Instant e2 = (cr2.getExpirationDate() ==  null) ? Instant.MAX : cr2.getExpirationDate();
			int tmpResult = e1.compareTo(e2);
			return (tmpResult == 0) ? cr1.compareTo(cr2) : tmpResult;
		}
	}
	
	static class PromotionIneligibilityException extends IneligibilityException {
		PromotionIneligibilityException(String msg) {
			super(msg);
		}
	}
	
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
	 * Adds an Examination or Check Ride to the Pilot's test history.
	 * @param t the Test
	 */
	public void add(Test t) {
		_tests.add(t);
	}

	/**
	 * Initializes the collection of Equipment programs.
	 * @param eqTypes a Collection of EquipmentType beans
	 */
	public void setEquipmentTypes(Collection<EquipmentType> eqTypes) {
		_allEQ.addAll(eqTypes);
	}

	/**
	 * Returns the Pilot's equipment program.
	 * @return the EquipmentType bean
	 */
	public EquipmentType getEquipmentType() {
		return _myEQ;
	}

	/**
	 * Returns the Pilot's examinations and CheckRides.
	 * @return a Collection of Test beans
	 */
	public Collection<Test> getExams() {
		return _tests;
	}
	
	/**
	 * Returns the Pilots Check Rides.
	 * @param expirationDays the number of days in the future each check ride will expire
	 * @return a Collection of CheckRide beans
	 */
	public Collection<CheckRide> getCheckRides(int expirationDays) {
		List<CheckRide> results = _tests.stream().filter(CheckRide.class::isInstance).map(CheckRide.class::cast).collect(Collectors.toList());
		if (expirationDays == 0)
			return results;
		
		// Flter by expiration
		Collections.reverse(results);
		Instant expDate = Instant.now().plus(expirationDays, ChronoUnit.DAYS);
		Collection<CheckRide> expResults = new TreeSet<CheckRide>(new ExpiringRideComparator());
		results.stream().filter(cr -> (!cr.getAcademy() && ((cr.getStatus() == TestStatus.SCORED) && expDate.isAfter(cr.getExpirationDate())))).forEach(expResults::add);
		return CollectionUtils.sort(expResults, new RideExpireComparator());
	}
	
	/**
	 * Applies expiration dates to non-Currency check rides.
	 * @param days the expiration in days
	 */
	public void applyExpiration(int days) {
		getCheckRides(0).stream().filter(cr -> (!cr.getAcademy() && (cr.getStatus() == TestStatus.SCORED) && (cr.getExpirationDate() == null))).forEach(cr -> cr.setExpirationDate(cr.getScoredOn().plus(days, ChronoUnit.DAYS)));
	}
	
	/**
	 * Clears expiration dates from non-Currency check rides.
	 */
	public void clearExpiration() {
		getCheckRides(0).stream().filter(cr -> (cr.getType() != RideType.CURRENCY)).forEach(cr -> cr.setExpirationDate(null));
	}

	/**
	 * Returns whether a Pilot qualifies for Captain's rank in a particular stage.
	 * @param stage the stage number
	 * @return TRUE if the Pilot has passed the Captain's exam and flown the necessary legs
	 * in <i>ANY</i> equipment program in a particular stage.
	 */
	public boolean isCaptainInStage(int stage) {
		
		// Check for staff member
		if (_usr.getRank().isCP())
			return true;

		// Check if we're already a captain
		if ((_usr.getRank() != Rank.FO) && (stage == _myEQ.getStage()))
			return true;

		// Iterate through the equipment types in a stage
		for (EquipmentType eq : _allEQ) {
			if ((eq.getStage() == stage) && promotionEligible(eq))
				return true;
		}

		return false;
	}

	/**
	 * Returns the highest stage Examination this user has passed. This will return 5 if the user
	 * is a Chief Pilot or an Assistant Chief Pilot.
	 * @return the stage number of the highest examination, or 1 if none passed
	 * @see Test#getStage()
	 */
	public int getMaxExamStage() {

		// Check for staff member
		if (_usr.getRank().isCP())
			return CP_STAGE;

		int maxStage = 1;
		for (Test t : _tests) {
			if ((t instanceof Examination) && (!_qName.equals(t.getName())) && t.getPassFail() && SystemData.get("airline.code").equals(t.getOwner().getCode()))
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
		int maxStage = _myEQ.getStage(); Instant now = Instant.now();
		for (Test t : _tests) {
			if (t.getAcademy() || !t.getPassFail() || (!(t instanceof CheckRide)))
				continue;
			if (!SystemData.get("airline.code").equals(t.getOwner().getCode()))
				continue;
			
			CheckRide cr = (CheckRide) t;
			if ((cr.getExpirationDate() == null) || (now.isBefore(cr.getExpirationDate())))
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
			throw new PromotionIneligibilityException(ep.getName() + " inactive");

		// If it's the Initial Questionnaire, uh uh
		if (_qName.equals(ep.getName()))
			throw new PromotionIneligibilityException(ep.getName() + " is the Questionnaire");

		// If it's part of the Flight Academy, no
		if (ep.getAcademy())
			throw new PromotionIneligibilityException(ep.getName() + " is a Flight Academy examination");

		// Check if we've passed or submitted the exam
		if (hasPassed(Collections.singleton(ep.getName())) || hasSubmitted(ep.getName()))
			throw new PromotionIneligibilityException(ep.getName() + " is passed / submitted");

		// Check if it's the FO exam for the current program
		if (_myEQ.getExamNames(Rank.FO).contains(ep.getName()))
			throw new PromotionIneligibilityException(ep.getName() + " is FO exam for " + _myEQ.getName());

		// Check if we are in the proper equipment program
		if (!StringUtils.isEmpty(ep.getEquipmentType())) {
			if (!ep.getEquipmentType().equals(_usr.getEquipmentType()))
				throw new PromotionIneligibilityException(ep.getName() + " eqType=" + ep.getEquipmentType() + ", our eqType=" + _usr.getEquipmentType());

			// If the exam is limited to a specific equipment program, require 1/2 the legs required for promotion
			if (getFlightLegs(_myEQ) < (_myEQ.getPromotionLegs()))
				throw new PromotionIneligibilityException(ep.getName() + " Our Flight Legs=" + getFlightLegs(_myEQ));
		}

		// Check if we've reached the proper minimum stage
		if (ep.getMinStage() > getMaxCheckRideStage())
			throw new PromotionIneligibilityException(ep.getName() + " minStage=" + ep.getMinStage() + ", our maxCheckRideStage=" + getMaxCheckRideStage());

		// If the exam is a higher stage than us, require Captan's rank in the stage below
		if ((ep.getStage() > getMaxCheckRideStage()) && !isCaptainInStage(ep.getStage() - 1))
			throw new PromotionIneligibilityException(ep.getName() + " stage=" + ep.getStage() + ", our Stage=" + getMaxCheckRideStage()
					+ ", not Captain in stage " + (ep.getStage() - 1));

		// Check if we've been locked out of exams
		if (_usr.getNoExams())
			throw new PromotionIneligibilityException("Testing Center locked out");
	}

	/**
	 * Returns if a user has met all the requirements for switching to a particular equipment program.
	 * @param eq the EquipmentType bean
	 * @throws IneligibilityException if the user cannot switch to the program
	 */
	public void canSwitchTo(EquipmentType eq) throws IneligibilityException {

		// Check if we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName())) {
			boolean reqRatings = canRequestRatings(eq);
			if (!reqRatings)
				throw new PromotionIneligibilityException("Already in " + eq.getName() + " program, no ratings to get");
		}
		
		// If it's not in our airline, don't allow it
		if (!SystemData.get("airline.code").equals(eq.getOwner().getCode()))
			throw new PromotionIneligibilityException(eq.getName() + " is a " + eq.getOwner().getName() + " program");

		// Check if we've passed the FO exam for that program
		boolean initialHire = hasCheckRide(eq, RideType.HIRE); 
		if (!initialHire && !hasPassed(eq.getExamNames(Rank.FO)))
			throw new PromotionIneligibilityException("Haven't passed " + eq.getExamNames(Rank.FO));

		// Check if we have a checkride in that equipment
		if (!initialHire && !hasCheckRide(eq))
			throw new PromotionIneligibilityException("Haven't passed " + eq.getName() + " check ride");
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
	 * @return the RideType to request
	 * @throws IneligibilityException if the user cannot request a check ride
	 */
	public RideType canRequestCheckRide(EquipmentType eq) throws IneligibilityException {

		// Make sure we're a captain in the previous stage if the stage is higher than our own
		if ((eq.getStage() > getMaxCheckRideStage()) && (!isCaptainInStage(eq.getStage() - 1)))
			throw new PromotionIneligibilityException("Must be Captain in Stage " + (eq.getStage() - 1));

		// Check if we've passed the FO/CAPT exam for that program
		boolean initialHire = hasCheckRide(eq, RideType.HIRE); 
		if (!initialHire && !hasPassed(eq.getExamNames(Rank.FO)) && !hasPassed(eq.getExamNames(Rank.C)))
			throw new PromotionIneligibilityException("Has not passed FO/Captain Examination");

		// Make sure we're not already in that program
		if (_usr.getEquipmentType().equals(eq.getName()))
			throw new PromotionIneligibilityException("Already in " + eq.getName() + " program");

		// Check if we don't have a checkride in that equipment program
		if (initialHire || hasCheckRide(eq))
			throw new PromotionIneligibilityException("Has already passed Check Ride");

		// If we require a checkride, ensure we have a minimum number of legs
		if (!canRequestSwitch())
			throw new PromotionIneligibilityException("Has not completed " + (_myEQ.getPromotionLegs() / 2) + " legs for promotion");
		
		if (_usr.getProficiencyCheckRides() && hasCheckRide(eq, RideType.CHECKRIDE))
			return RideType.CURRENCY;
		
		return RideType.CHECKRIDE;
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
		if (_usr.getRank() != Rank.FO)
			return false;

		// Check if we're otherwise eligible
		return promotionEligible(eq);
	}

	/**
	 * Returns whether we could have promoted the user to Captain within an equipment program.
	 * @param eq the EquipmentType bean
	 * @return TRUE if the user is eligible to be promoted to captain, otherwise FALSE
	 */
	public boolean promotionEligible(EquipmentType eq) {

		// Check if we've passed the examinations
		if (!hasPassed(eq.getExamNames(Rank.C)))
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
			if ((t.getStatus() == TestStatus.SUBMITTED) && (t.getName().equals(examName)))
				return true;
		}

		return false;
	}

	/**
	 * Returns if a user has passed any check ride for a particular equipment type.
	 * @param eq the Equipment Type bean
	 * @return TRUE if the user passed the check ride, otherwise FALSE
	 */
	public boolean hasCheckRide(EquipmentType eq) {
		return hasCheckRide(eq, null);
	}

	/**
	 * Returns if a user has passed a Check Ride for a particular equipment type.
	 * @param eq the Equipment Type bean
	 * @param rt the Check Ride type, or null for any
	 * @return TRUE if the user passed the check ride, otherwise FALSE
	 */
	public boolean hasCheckRide(EquipmentType eq, RideType rt) {
		Instant now = Instant.now();
		for (Test t : _tests) {
			if ((t instanceof CheckRide) && t.getPassFail() && !t.getAcademy()) {
				CheckRide cr = (CheckRide) t;
				if (!cr.getEquipmentType().equals(eq.getName()))
					continue;
				if ((cr.getExpirationDate() == null) || (now.isBefore(cr.getExpirationDate())))
					return (rt == null) || (cr.getType() == rt);
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

		// If the test is not scored or passed, then forget it
		if ((t.getStatus() != TestStatus.SCORED) || t.getPassFail())
			return false;

		// Check the time from the scoring
		long timeInterval = (System.currentTimeMillis() - t.getScoredOn().toEpochMilli()) / 1000;
		log.info("Exam Lockout: interval = " + timeInterval + "s, period = " + (lockoutHours * 3600) + "s");
		return (timeInterval < (lockoutHours * 3600L));
	}
	
	/**
	 * Returns all Equipment programs that the Pilot is fully rated for.
	 * @return a Collection of EquipmentTypes, sorted by stage and name
	 */
	public SortedSet<EquipmentType> getQualifiedPrograms() {
		SortedSet<EquipmentType> results = new TreeSet<EquipmentType>();
		for (EquipmentType eq : _allEQ) {
			try {
				canSwitchTo(eq);
				results.add(eq);
			} catch (IneligibilityException ie) {
				// empty
			}
		}
		
		// Check my program
		boolean initialHire = hasCheckRide(_myEQ, RideType.HIRE);
		if (initialHire || (hasPassed(_myEQ.getExamNames(Rank.FO)) && hasCheckRide(_myEQ)))
			results.add(_myEQ);
		
		return results;
	}

	/**
	 * Returns all aircraft types that the Pilot is fully rated for.
	 * @return a Collection of Aircraft types
	 */
	public Collection<String> getQualifiedRatings() {
		Collection<String> results = new TreeSet<String>();
		getQualifiedPrograms().stream().map(EquipmentType::getRatings).forEach(results::addAll);
		return results;
	}
}