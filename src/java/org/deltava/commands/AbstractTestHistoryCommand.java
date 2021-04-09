// Copyright 2005, 2006, 2007, 2009, 2010, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A class to support Web Site Commands use a {@link TestingHistoryHelper} object to determine what
 * examinations/transfers a Pilot is eligible for.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public abstract class AbstractTestHistoryCommand extends AbstractCommand {

	/**
	 * Populates the Testing History Helper by calling the proper DAOs in the right order.
	 * @param p the Pilot bean
	 * @param c the JDBC connection to use
	 * @return a populated TestingHistoryHelper bean
	 * @throws DAOException if a JDBC error occurs
	 */
	protected static final TestingHistoryHelper initTestHistory(Pilot p, Connection c) throws DAOException {

		// Load the PIREP beans
		String db = SystemData.get("airline.db");
		GetFlightReports frdao = new GetFlightReports(c);
		Collection<FlightReport> pireps = frdao.getByPilot(p.getID(), null);
		frdao.loadCaptEQTypes(p.getID(), pireps, db);

		// Get the Pilot's equipment program and all equipment types
		GetEquipmentType eqdao = new GetEquipmentType(c);
		EquipmentType eq = eqdao.get(p.getEquipmentType(), db);

		// Get the Pilot's applicant profile to get eq program hired into
		GetApplicant adao = new GetApplicant(c);
		Applicant a = adao.getByPilotID(p.getID());
		EquipmentType ieq = (a != null) ? eqdao.get(a.getEquipmentType()) : null;

		// Get the Pilot's examinations and check rides, and initialize the helper
		GetExam exdao = new GetExam(c);
		TestingHistoryHelper helper = new TestingHistoryHelper(p, eq, exdao.getExams(p.getID()), pireps);
		helper.setEquipmentTypes(eqdao.getAll());
		if (p.getProficiencyCheckRides() && SystemData.getBoolean("testing.currency.enabled"))
			helper.applyExpiration(SystemData.getInt("testing.currency.validity", 365));

		// Create a dummy FO exam(s) for the hired in program
		if (ieq != null) {
			for (String foExam : ieq.getExamNames(Rank.FO)) {
				if (!StringUtils.isEmpty(foExam) && !helper.hasPassed(Collections.singleton(foExam))) {
					Examination ex = new Examination(foExam);
					ex.setSize(1);
					ex.setScore(1);
					ex.setPassFail(true);
					ex.setStatus(TestStatus.SCORED);
					ex.setDate(p.getCreatedOn());
					ex.setScoredOn(p.getCreatedOn());
					ex.setOwner(SystemData.getApp(SystemData.get("airline.code")));
					helper.add(ex);
				}
			}
		}
		
		return helper;
	}
}