// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;

import org.apache.log4j.Logger;
import org.deltava.beans.academy.Course;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.event.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.servinfo.PositionData;
import org.deltava.beans.stats.Tour;
import org.deltava.comparators.GeoComparator;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * This is an ugly class that needs a proper home.
 * 
 * Flight submission is handled by an ACARS Command, a Web Command and two Services, all of which extend different parent classes. This is a poor
 * attempt to encapsulate common Flight Report validation and hydration behavior to avoid code duplication. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

@Helper(FlightReport.class)
public class FlightSubmissionHelper {
	
	private static final Logger log = Logger.getLogger(FlightSubmissionHelper.class);
	
	private final FlightReport _fr;
	private final Pilot _p;
	private FlightInfo _info;
	private int _trackID;
	private final List<PositionData> _otData = new ArrayList<PositionData>();
	private final List<GeospaceLocation> _rte = new ArrayList<GeospaceLocation>();
	
	private final Connection _c;
	private String _db;
	private String _appCode;
	
	private Aircraft _ac;
	private ScheduleEntry _onTimeEntry;
	
	private Course _crs;

	/**
	 * Creates the helper.
	 * @param c the JDBC Connection to use
	 * @param fr the FlightReport
	 * @param p the Pilot 
	 */
	public FlightSubmissionHelper(Connection c, FlightReport fr, Pilot p) {
		super();
		_c = c;
		_fr = fr;
		_p = p;
	}
	
	/**
	 * Updates the ACARS Flight Information for this flight.
	 * @param inf a FlightInfo bean
	 */
	public void setACARSInfo(FlightInfo inf) {
		_info = inf;
	}
	
	/**
	 * Sets airline-dependent context information.
	 * @param appName the virtual airline code
	 * @param dbName the database name
	 */
	public void setAirlineInfo(String appName, String dbName) {
		_appCode = appName;
		_db = dbName;
	}
	
	/**
	 * Returns the ACARS Flight Information.
	 * @return a FlightInfo bean
	 */
	public FlightInfo getACARSInfo() {
		return _info;
	}
	
	/**
	 * Returns whether the flight position data has been populated.
	 * @return TRUE if position data is present, otherwise FALSE
	 */
	public boolean hasPositionData() {
		return (_rte.size() > 0);
	}
	
	/**
	 * Returns whether Online track data has been populated.
	 * @return TRUE if track data is present with a non-zero track ID, otherwise FALSE
	 */
	public boolean hasTrackData() {
		return ((_trackID !=0) && (_otData.size() > 0));
	}
	
	/**
	 * Returns the Online track ID.
	 * @return the ID
	 */
	public int getTrackID() {
		return _trackID;
	}
	
	/**
	 * Returns the Online track data.
	 * @return a Collection of PositionData beans
	 */
	public Collection<PositionData> getTrackData() {
		return _otData;
	}
	
	/**
	 * Returns the Flight Schedule entry for the real-time flight being tracked.
	 * @return a ScheduleEntry, or none if no matching flight found
	 */
	public ScheduleEntry getOnTimeEntry() {
		return _onTimeEntry;
	}
	
	/**
	 * Returns the currently enrolled Flight Academy course for Academy flight.
	 * @return a Course, or null if none
	 */
	public Course getCourse() {
		return _crs;
	}
	
	/**
	 * Adds position/track data to the helper. This usually is, but is not required to be, ACARSRouteEntry beans. Non-ACARS or XACARS flight reports
	 * may subsitiute other beans that are guaranteed to be GeospaceLocation beans, but nothing more.
	 * @param rtEntries a Collection of GeospaceLocation beans
	 */
	public void addPositions(Collection<? extends GeospaceLocation> rtEntries) {
		_rte.addAll(rtEntries);
	}
	
	/**
	 * Checks for existing draft Flight Reports matching this Airport pair, and whether a predetrmined number of flight reports have been held. 
	 * @throws DAOException if a JDBC error occurs
	 */
	public void checkFlightReports() throws DAOException {
		
		// Check for draft flight report 
		GetFlightReports prdao = new GetFlightReports(_c);
		List<FlightReport> dFlights = prdao.getDraftReports(_p.getID(), _fr, _db);
		if (!dFlights.isEmpty()) {
			FlightReport fr = dFlights.get(0);
			_fr.setID(fr.getID());
			_fr.setDatabaseID(DatabaseID.ASSIGN, fr.getDatabaseID(DatabaseID.ASSIGN));
			_fr.setDatabaseID(DatabaseID.EVENT, fr.getDatabaseID(DatabaseID.EVENT));
			_fr.setAttribute(FlightReport.ATTR_CHARTER, fr.hasAttribute(FlightReport.ATTR_CHARTER));
			_fr.setAttribute(FlightReport.ATTR_DIVERT, fr.hasAttribute(FlightReport.ATTR_DIVERT));
			if (!StringUtils.isEmpty(fr.getComments()))
				_fr.setComments(fr.getComments());
			if (!fr.getStatusUpdates().isEmpty()) {
				fr.getStatusUpdates().forEach(_fr::addStatusUpdate);
				_fr.addStatusUpdate(0, HistoryType.UPDATE, String.format("Loaded %d updates from draft Flight Report %d", Integer.valueOf(fr.getStatusUpdates().size()), Integer.valueOf(fr.getID())));
			}
		}
		
		// Check for held flight reports
		int heldPIREPs = prdao.getHeld(_fr.getAuthorID(), _db);
		if (heldPIREPs >= SystemData.getInt("users.pirep.maxHeld", 5)) {
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Automatically Held due to %d held Flight Reports", Integer.valueOf(heldPIREPs)));
			_fr.setStatus(FlightStatus.HOLD);
		}
	}
	
	/**
	 * Checks a Flight Report's Online Network flag. If online track data is found, it is loaded
	 * @throws DAOException if a JDBC error occurs
	 * @see FlightSubmissionHelper#getTrackData()
	 */
	public void checkOnlineNetwork() throws DAOException {
		
		// Check that the user has an online network ID
		OnlineNetwork network = _fr.getNetwork();
		if ((network != null) && (!_p.hasNetworkID(network))) {
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("No %s ID, resetting Online Network flag", network));
			_fr.setNetwork(null);
			_fr.setDatabaseID(DatabaseID.EVENT, 0);
		} else if ((network == null) && (_fr.getDatabaseID(DatabaseID.EVENT) != 0)) {
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, "Filed offline, resetting Online Event flag");
			_fr.setDatabaseID(DatabaseID.EVENT, 0);
		}
		
		// Load track data
		int trackID = 0;
		if (_fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK)) {
			GetOnlineTrack tdao = new GetOnlineTrack(_c); 
			trackID = tdao.getTrackID(_fr.getDatabaseID(DatabaseID.PILOT), _fr.getNetwork(), _fr.getSubmittedOn(), _fr.getAirportD(), _fr.getAirportA());
			if (trackID != 0) {
				_otData.addAll(tdao.getRaw(trackID));
				_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Loaded %s online track data (%d positions)", _fr.getNetwork(), Integer.valueOf(_otData.size())));	
			}
		}
	}
	
	/**
	 * Checks a Flight Report for participation in an Online Event.
	 * @throws DAOException if a JDBC error occurs
	 * @see EventFlightHelper
	 */
	public void checkOnlineEvent() throws DAOException {
		
		// Check if it's an Online Event flight
		GetEvent evdao = new GetEvent(_c);
		EventFlightHelper efr = new EventFlightHelper(_fr);
		if ((_fr.getDatabaseID(DatabaseID.EVENT) == 0) && (_fr.getNetwork() != null)) {
			List<Event> events = evdao.getPossibleEvents(_fr, _db);
			events.removeIf(e -> !efr.matches(e));
			if (!events.isEmpty()) {
				Event e = events.get(0);
				_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Detected participation in %s Online Event", e.getName()));
				_fr.setDatabaseID(DatabaseID.EVENT, e.getID());
			}
		}

		// Check that it was submitted in time
		Event e = evdao.get(_fr.getDatabaseID(DatabaseID.EVENT));
		if ((e != null) && !efr.matches(e)) {
			efr.getMessages().forEach(emsg -> _fr.addStatusUpdate(0, HistoryType.SYSTEM, emsg));
			_fr.setDatabaseID(DatabaseID.EVENT, 0);
		} else if ((e == null) && (_fr.getDatabaseID(DatabaseID.EVENT) != 0)) {
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Unknown Online Event - %d", Integer.valueOf(_fr.getDatabaseID(DatabaseID.EVENT))));
			_fr.setDatabaseID(DatabaseID.EVENT, 0);
		}
	}
	
	/**
	 * Checks whether the Pilot is rated in the Aircraft, and whether the flight counts for promotion to Captain.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void checkRatings() throws DAOException {
		
		// Load the aircraft
		GetAircraft acdao = new GetAircraft(_c);
		_ac = acdao.get(_fr.getEquipmentType());
		if (_ac == null) 
			throw new DAOException(String.format("Invalid equipment type - %s", _fr.getEquipmentType()));
		else if (!_ac.getName().equals(_fr.getEquipmentType()))
			throw new DAOException(String.format("Expected %s, received %s", _fr.getEquipmentType(), _ac.getName()));
		
		// Check ratings
		GetEquipmentType eqdao = new GetEquipmentType(_c);
		EquipmentType eqType = eqdao.get(_p.getEquipmentType(), _db);
		if (!_p.getRatings().contains(_fr.getEquipmentType()) && !eqType.getRatings().contains(_fr.getEquipmentType())) {
			log.info(_p.getName() + " not rated in " + _fr.getEquipmentType() + " ratings = " + _p.getRatings());
			_fr.setAttribute(FlightReport.ATTR_NOTRATED, !_fr.hasAttribute(FlightReport.ATTR_CHECKRIDE));
		}
		
		// Check promotion
		Collection<String> promoEQ = eqdao.getPrimaryTypes(_db, _fr.getEquipmentType());
		if (promoEQ.contains(_p.getEquipmentType())) {
			FlightPromotionHelper helper = new FlightPromotionHelper(_fr);
			for (Iterator<String> i = promoEQ.iterator(); i.hasNext();) {
				String pType = i.next();
				EquipmentType pEQ = eqdao.get(pType, _db);
				if (pEQ == null)
					log.warn("Cannot find " + pType + " in " + _db);
				
				boolean isOK = helper.canPromote(pEQ);
				if (!isOK) {
					i.remove();
					_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Not eligible for promotion: %s", helper.getLastComment()));
				}
			}

			_fr.setCaptEQType(promoEQ);
			if (!promoEQ.isEmpty())
				_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Flight eligible for promotion in %s" , StringUtils.listConcat(promoEQ, ", ")));
		} else if (promoEQ.isEmpty())
			log.warn("No equipment program found for " + _fr.getEquipmentType() + " in " + _db);
	}
	
	/**
	 * Checks the Aircraft used and sets any range/weight warnings. 
	 */
	public void checkAircraft() {
		
		// Check for excessive distance and diversion
		AircraftPolicyOptions opts = _ac.getOptions(_appCode);
		_fr.setAttribute(FlightReport.ATTR_HISTORIC, _ac.getHistoric());
		_fr.setAttribute(FlightReport.ATTR_RANGEWARN, (_fr.getDistance() > opts.getRange()));

		if (!_fr.hasAttribute(FlightReport.ATTR_ACARS)) return;
		ACARSFlightReport afr = (ACARSFlightReport) _fr;
		
		// Check for excessive weight
		if ((_ac.getMaxTakeoffWeight() != 0) && (afr.getTakeoffWeight() > _ac.getMaxTakeoffWeight()))
			_fr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
		else if ((_ac.getMaxLandingWeight() != 0) && (afr.getLandingWeight() > _ac.getMaxLandingWeight()))
			_fr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
	}
	
	/**
	 * Checks this Flight for in-flight refueling.
	 */
	public void checkRefuel() {
		if (!_fr.hasAttribute(FlightReport.ATTR_ACARS) || !hasPositionData()) return;
		ACARSFlightReport afr = (ACARSFlightReport) _fr;
		List<FuelChecker> fuelData = _rte.stream().filter(FuelChecker.class::isInstance).map(FuelChecker.class::cast).collect(Collectors.toList());
		FuelUse use = FuelUse.validate(fuelData);
		afr.setTotalFuel(use.getTotalFuel());
		afr.setAttribute(FlightReport.ATTR_REFUELWARN, use.getRefuel());
		use.getMessages().forEach(fuelMsg -> afr.addStatusUpdate(0, HistoryType.SYSTEM, fuelMsg));
	}
	
	/**
	 * Calculates the departure and arrival Gates used for this flight.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void calculateGates() throws DAOException {
		
		GeoComparator dgc = new GeoComparator(_rte.get(0), true);
		GeoComparator agc = new GeoComparator(_rte.get(_rte.size() - 1), true);
	
		// Get the closest departure gate
		GetGates gdao = new GetGates(_c);
		SortedSet<Gate> dGates = new TreeSet<Gate>(dgc);
		dGates.addAll(gdao.getAllGates(_fr.getAirportD(), _fr.getSimulator()));
		_info.setGateD(dGates.stream().findFirst().orElse(null));
		
		// Get the closest arrival gate
		SortedSet<Gate> aGates = new TreeSet<Gate>(agc);
		aGates.addAll(gdao.getAllGates(_fr.getAirportA(), _fr.getSimulator()));
		_info.setGateA(aGates.stream().findFirst().orElse(null));
	}
	
	/**
	 * Calculates the payload load factor for this Flight.
	 * @param econ an EconomyInfo bean for the virtual airline
	 * @param hasCustomCabin TRUE if the aircraft has a non-standard cabin size, otherwise FALSE
	 */
	public void calculateLoadFactor(EconomyInfo econ, boolean hasCustomCabin) {
		if (econ == null) return;
		if ((_info != null) && (_fr.getLoadFactor() <= 0) && (_info.getLoadFactor() <= 0)) {
			LoadFactor lf = new LoadFactor(econ);
			double loadFactor = lf.generate(_fr.getDate());
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Calculated load factor of %4.3f, was %4.3f for Flight %d", Double.valueOf(loadFactor), Double.valueOf(_fr.getLoadFactor()), Integer.valueOf(_fr.getDatabaseID(DatabaseID.ACARS))));
			_fr.setLoadFactor(loadFactor);
		} else if ((_info != null) && (_info.getLoadFactor() > 0) && !hasCustomCabin) {
			_fr.setLoadFactor(_info.getLoadFactor());
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Using Flight %d data load factor of %4.3f", Integer.valueOf(_fr.getDatabaseID(DatabaseID.ACARS)), Double.valueOf(_info.getLoadFactor())));
		}
		
		AircraftPolicyOptions opts = _ac.getOptions(_appCode);
		if ((opts.getSeats() > 0) && (_fr.getPassengers() == 0)) {
			int paxCount = (int) Math.round(opts.getSeats() * _fr.getLoadFactor());
			_fr.setPassengers(Math.min(opts.getSeats(), paxCount));
			if (paxCount > opts.getSeats())
				_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Invalid passenger count - pax=%d, seats=%d", Integer.valueOf(paxCount), Integer.valueOf(opts.getSeats())));
		}
	}
	
	/**
	 * Checks the Flight Schedule to ensure the flight is valid, and sets any optional diversion/Flight Academy flags.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void checkSchedule() throws DAOException {
		
		GetRawSchedule rsdao = new GetRawSchedule(_c);
		GetScheduleSearch sdao = new GetScheduleSearch(_c);
		sdao.setSources(rsdao.getSources(true, _db));
		ScheduleEntry sEntry = sdao.get(_fr, _db);
		boolean isAcademy = _ac.getAcademyOnly() || ((sEntry != null) && sEntry.getAcademy());
		
		// If we're an Academy flight, check if we have an active course
		if (isAcademy) {
			GetAcademyCourses crsdao = new GetAcademyCourses(_c);
			Collection<Course> courses = crsdao.getByPilot(_fr.getAuthorID());
			_crs = courses.stream().filter(crs -> (crs.getStatus() == org.deltava.beans.academy.Status.STARTED)).findAny().orElse(null);
			boolean isINS = _p.isInRole("Instructor") ||  _p.isInRole("AcademyAdmin") || _p.isInRole("AcademyAudit") || _p.isInRole("Examiner");
			_fr.setAttribute(FlightReport.ATTR_ACADEMY, (_crs != null) || isINS);	
			if (!_fr.hasAttribute(FlightReport.ATTR_ACADEMY))
				_fr.addStatusUpdate(0, HistoryType.SYSTEM, "Removed Flight Academy status - No active Course");
		}
		
		// Check for diversion
		if (_info != null)
			_fr.setAttribute(FlightReport.ATTR_DIVERT, _fr.hasAttribute(FlightReport.ATTR_DIVERT) || !_fr.getAirportA().equals(_info.getAirportA()));

		// Check the schedule database and check the route pair
		boolean isAssignment = (_fr.getDatabaseID(DatabaseID.ASSIGN) != 0);
		boolean isEvent = (_fr.getDatabaseID(DatabaseID.EVENT) != 0);
		boolean isTour = (_fr.getDatabaseID(DatabaseID.TOUR) != 0);
		FlightTime avgHours = sdao.getFlightTime(_fr, _db);
		if ((avgHours.getType() == RoutePairType.UNKNOWN) && !isAcademy && !isAssignment && !isEvent && !isTour) {
			log.warn(String.format("No flights found between %s and %s", _fr.getAirportD(), _fr.getAirportA()));
			boolean wasValid = _info.isScheduleValidated() && _info.matches(_fr);
			if (!wasValid)
				_fr.setAttribute(FlightReport.ATTR_ROUTEWARN, !_fr.hasAttribute(FlightReport.ATTR_CHARTER));
		} else {
			int minHours = (int) ((avgHours.getFlightTime() * 0.75) - 5); // fixed 0.5 hour
			int maxHours = (int) ((avgHours.getFlightTime() * 1.15) + 5);
			if ((_fr.getLength() < minHours) || (_fr.getLength() > maxHours))
				_fr.setAttribute(FlightReport.ATTR_TIMEWARN, true);
			
			// Calculate timeliness of flight
			if (!_fr.hasAttribute(FlightReport.ATTR_DIVERT) && _fr.hasAttribute(FlightReport.ATTR_ACARS)) {
				ACARSFlightReport afr = (ACARSFlightReport) _fr;
				ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("TIME_D"); ssc.setDBName(_db);
				ssc.setAirportD(_fr.getAirportD()); ssc.setAirportA(_fr.getAirportA());
				ssc.setExcludeHistoric(_fr.getAirline().getHistoric() ? Inclusion.INCLUDE : Inclusion.EXCLUDE);
				OnTimeHelper oth = new OnTimeHelper(sdao.search(ssc));
				afr.setOnTime(oth.validate(afr));
				_onTimeEntry = oth.getScheduleEntry();
			}
		}
	}
	
	/**
	 * Calculates runways used and flags any runway length/surface violations.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void calculateRunways() throws DAOException {
		
		if (!_fr.hasAttribute(FlightReport.ATTR_ACARS)) return;
		ACARSFlightReport afr = (ACARSFlightReport) _fr;
		AircraftPolicyOptions opts = _ac.getOptions(_appCode);
		
		// Load the departure runway
		GetNavAirway navdao = new GetNavAirway(_c);
		LandingRunways lr = navdao.getBestRunway(_fr.getAirportD(), afr.getSimulator(), afr.getTakeoffLocation(), afr.getTakeoffHeading());
		Runway r = lr.getBestRunway();
		if (r != null) {
			int dist = r.distanceFeet(afr.getTakeoffLocation());
			_info.setRunwayD(new RunwayDistance(r, dist));
			if (r.getLength() < opts.getTakeoffRunwayLength()) {
				_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Minimum takeoff runway length for the %s is %d feet", _ac.getName(), Integer.valueOf(opts.getTakeoffRunwayLength())));
				_fr.setAttribute(FlightReport.ATTR_RWYWARN, true);
			}
			if (!r.getSurface().isHard() && !opts.getUseSoftRunways()) {
				_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("%s not authorized for soft runway operation on %s", _ac.getName(), r.getName()));
				_fr.setAttribute(FlightReport.ATTR_RWYSFCWARN, true);
			}
		}

		// Load the arrival runway
		lr = navdao.getBestRunway(_fr.getAirportA(), _fr.getSimulator(), afr.getLandingLocation(), afr.getLandingHeading());
		r = lr.getBestRunway();
		if (r != null) {
			int dist = r.distanceFeet(afr.getLandingLocation());
			_info.setRunwayA(new RunwayDistance(r, dist));
			if (r.getLength() < opts.getLandingRunwayLength()) {
				_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Minimum landing runway length for the %s is %d feet", _ac.getName(), Integer.valueOf(opts.getLandingRunwayLength())));
				_fr.setAttribute(FlightReport.ATTR_RWYWARN, true);
			}
			if (!r.getSurface().isHard() && !opts.getUseSoftRunways()) {
				_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("%s not authorized for soft runway operation on %s", _ac.getName(), r.getName()));
				_fr.setAttribute(FlightReport.ATTR_RWYSFCWARN, true);
			}
		}
	}
	
	/**
	 * Checks this Flight for ETOPS and prohibited airspace violations.
	 */
	public void checkAirspace() {
		if (!hasPositionData())
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, "No Position Data for Airspace/Fuel checks");
		
		// Check ETOPS
		AircraftPolicyOptions opts = _ac.getOptions(_appCode);
		ETOPSResult etopsClass = ETOPSHelper.classify(_rte);
		_fr.setAttribute(FlightReport.ATTR_ETOPSWARN, ETOPSHelper.isWarn(opts.getETOPS(), etopsClass.getResult()));
		if (_fr.hasAttribute(FlightReport.ATTR_ETOPSWARN)) {
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("ETOPS classificataion - aircraft %s, route %s", opts.getETOPS(), etopsClass.getResult()));
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("ETOPS route info - %s", etopsClass));
		}
		
		// Check prohibited airspace
		Collection<Airspace> rstAirspaces = AirspaceHelper.classify(_rte, false); 
		if (!rstAirspaces.isEmpty()) {
			_fr.setAttribute(FlightReport.ATTR_AIRSPACEWARN, true);
			_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Entered restricted airspace %s", StringUtils.listConcat(rstAirspaces, ", ")));
		}
	}
	
	/**
	 * Checks whether this Flight should be included as part of a Flight Tour.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void checkTour() throws DAOException {
		
		GetTour trdao = new GetTour(_c);
		GetFlightReports prdao = new GetFlightReports(_c);
		Collection<Tour> possibleTours = trdao.findLeg(_fr, _fr.getDate(), _db);
		if (!possibleTours.isEmpty()) {
			Instant minDate = Instant.ofEpochMilli(possibleTours.stream().mapToLong(t -> t.getStartDate().toEpochMilli()).min().orElseThrow());
			Duration d = Duration.between(minDate, _fr.getSubmittedOn());
			Collection<FlightReport> oldPireps = prdao.getLogbookCalendar(_fr.getAuthorID(), _db, minDate, (int)d.toDaysPart() + 1);
		
			// Init the helper and validate
			TourFlightHelper tfh = new TourFlightHelper(_fr, true);
			tfh.addFlights(oldPireps);
			for (Tour t : possibleTours) {
				int idx = tfh.isLeg(t);
				if (idx > 0) {
					_fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Leg %d in Flight Tour %s", Integer.valueOf(idx), t.getName()));
					_fr.setDatabaseID(DatabaseID.TOUR, t.getID());
					break;
				} else if (tfh.hasMessage())
					tfh.getMessages().forEach(tmsg -> _fr.addStatusUpdate(0, HistoryType.SYSTEM, tmsg));
			}
		}
	}
}