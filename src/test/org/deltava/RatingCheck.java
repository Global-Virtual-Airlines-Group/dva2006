// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.testing.*;
import org.deltava.dao.*;
import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

public class RatingCheck extends TestCase {
	
	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/dva";

	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(SetLocations.class);
		
		SystemData.init();
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "import", "import");
		assertNotNull(_c);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(_c);
		tzdao.initAll();
		GetUserData uddao = new GetUserData(_c);
		SystemData.add("apps", uddao.getAirlines(true));
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());
		
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testCheckRatings() throws Exception {
		
		// Create the entry
		File f = new File("c:\\temp", "newRatings.csv");
		PrintWriter pw = new PrintWriter(new FileWriter(f));
		
		GetPilot pdao = new GetPilot(_c);
		GetApplicant adao = new GetApplicant(_c);
		GetExam exdao = new GetExam(_c);
		GetFlightReports frdao = new GetFlightReports(_c);
		GetEquipmentType eqdao = new GetEquipmentType(_c);
		Collection<EquipmentType> allEQ = eqdao.getActive();
		
		Collection<Pilot> pilots = pdao.getActivePilots("P.PILOT_ID");
		for (Pilot p : pilots) {
			log.info("Processing " + p.getName());
			p = pdao.get(p.getID());
			
			// Load PIREPs and eqType
			EquipmentType eq = eqdao.get(p.getEquipmentType());
			Collection<FlightReport> pireps = frdao.getByPilot(p.getID(), null);
			frdao.getCaptEQType(pireps);
			
			// Get the Pilot's applicant profile to get eq program hired into
			Applicant a = adao.getByPilotID(p.getID());
			EquipmentType ieq = (a != null) ? eqdao.get(a.getEquipmentType()) : eq;
			
			// Get the Pilot's examinations and check rides, and initialize the helper
			TestingHistoryHelper helper = new TestingHistoryHelper(p, eq, exdao.getExams(p.getID()), pireps);
			helper.setEquipmentTypes(eqdao.getAll());
			
			// Create a dummy FO exam(s) for the hired in program
			for (Iterator<String> i = ieq.getExamNames(Rank.FO).iterator(); i.hasNext(); ) {
				String foExam = i.next();
				if (!StringUtils.isEmpty(foExam) && !helper.hasPassed(Collections.singleton(foExam))) {
					Examination ex = new Examination(foExam);
					ex.setSize(1);
					ex.setScore(1);
					ex.setPassFail(true);
					ex.setStatus(TestStatus.SCORED);
					ex.setDate(p.getCreatedOn());
					ex.setScoredOn(p.getCreatedOn());
					ex.setOwner(SystemData.getApp(SystemData.get("airline.code")));
					helper.addExam(ex);
				}
			}
			
			// Loop through the equipment types
			Collection<String> newEQ = new TreeSet<String>();
			Collection<EquipmentType> eqTypes = new ArrayList<EquipmentType>(allEQ);
			for (Iterator<EquipmentType> i = eqTypes.iterator(); i.hasNext(); ) {
				EquipmentType eqType = i.next();
				if (eqType.getName().equals(p.getEquipmentType()))
					newEQ.addAll(eqType.getRatings());
				else {
					try {
						helper.canSwitchTo(eqType);
						newEQ.addAll(eqType.getRatings());
					} catch (IneligibilityException ie) {
						// empty
					}
				}
			}
			
			// Determine what ratings we will gain/lose
			Collection<String> addRatings = CollectionUtils.getDelta(newEQ, p.getRatings());
			Collection<String> rmvRatings = CollectionUtils.getDelta(p.getRatings(), newEQ);
			
			// Write the entry
			pw.print(p.getName());
			pw.print(',');
			pw.print(p.getPilotCode());
			pw.print(',');
			pw.print(p.getStatusName());
			pw.print(',');
			pw.print(p.getRank());
			pw.print(',');
			pw.print(p.getEquipmentType());
			pw.print(',');
			pw.print(StringUtils.listConcat(addRatings, " "));
			pw.print(',');
			pw.println(StringUtils.listConcat(rmvRatings, " "));
		}
		
		pw.close();
	}
}
