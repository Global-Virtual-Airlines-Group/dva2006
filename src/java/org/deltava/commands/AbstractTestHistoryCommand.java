// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A class to support Web Site Commands use a {@link TestingHistoryHelper} object to determine what
 * examinations/transfers a Pilot is eligible for.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractTestHistoryCommand extends AbstractCommand {

	/**
	 * Populates the Testing History Helper by calling the proper DAOs in the right order.
	 * @param p the Pilot bean
	 * @param c the JDBC connection to use
	 * @throws DAOException if a JDBC error occurs
	 */
	protected final TestingHistoryHelper initTestHistory(Person p, Connection c) throws DAOException {

		// Load the PIREP beans
		GetFlightReports frdao = new GetFlightReports(c);
		Collection<FlightReport> pireps = frdao.getByPilot(p.getID(), null);
		frdao.getCaptEQType(pireps);

		// Get the Pilot's equipment program and all equipment types
		GetEquipmentType eqdao = new GetEquipmentType(c);
		EquipmentType eq = eqdao.get(p.getEquipmentType());

		// Get the Pilot's applicant profile to get eq program hired into
		GetApplicant adao = new GetApplicant(c);
		Applicant a = adao.getByPilotID(p.getID());
		EquipmentType ieq = (a != null) ? eqdao.get(a.getEquipmentType()) : null;

		// Get the Pilot's examinations and check rides, and initialize the helper
		GetExam exdao = new GetExam(c);
		TestingHistoryHelper helper = new TestingHistoryHelper((Pilot) p, eq, exdao.getExams(p.getID()), pireps);
		helper.setEquipmentTypes(eqdao.getAll());

		// Create a dummy FO exam for the hired in program
		if (ieq != null) {
			String foExam = ieq.getExamName(Ranks.RANK_FO);
			if (!StringUtils.isEmpty(foExam) && !helper.hasPassed(foExam)) {
				Examination ex = new Examination(ieq.getExamName(Ranks.RANK_FO));
				ex.setSize(1);
				ex.setScore(1);
				ex.setPassFail(true);
				ex.setStatus(Test.SCORED);
				ex.setDate(p.getCreatedOn());
				ex.setScoredOn(p.getCreatedOn());
				helper.addExam(ex);
			}
		}
		
		return helper;
	}
}