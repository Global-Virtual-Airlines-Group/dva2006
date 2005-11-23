// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;

/**
 * A class to support Web Site Commands use a {@link TestingHistoryHelper} object to determine what
 * examinations/transfers a Pilot is eligible for.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractTestHistoryCommand extends AbstractCommand {

	protected TestingHistoryHelper _testHistory;

	/**
	 * Populates the Testing History Helper by calling the proper DAOs in the right order.
	 * @param p the Pilot bean
	 * @param c the JDBC connection to use
	 * @throws DAOException if a JDBC error occurs
	 */
	protected final void initTestHistory(Pilot p, Connection c) throws DAOException {

		// Load the PIREP beans
		GetFlightReports frdao = new GetFlightReports(c);
		Collection pireps = frdao.getByPilot(p.getID(), null);
		frdao.getCaptEQType(pireps);

		// Get the Pilot's equipment program
		GetEquipmentType eqdao = new GetEquipmentType(c);
		EquipmentType eq = eqdao.get(p.getEquipmentType());
		
		// Get the Pilot's examinations and check rides, and initialize the helper
		GetExam exdao = new GetExam(c);
		_testHistory = new TestingHistoryHelper(p, eq, exdao.getExams(p.getID()), pireps);
		
		// Get the Pilot's applicant profile to get eq program hired into
		GetApplicant adao = new GetApplicant(c);
		Applicant a = adao.getByPilotID(p.getID());
		
		// Create a dummy FO exam for the hired in program
		if (a != null) {
			EquipmentType ieq = eqdao.get(a.getEquipmentType());
			if ((ieq != null) && (ieq.getExamName(Ranks.RANK_FO) != null)) {
				Examination ex = new Examination(ieq.getExamName(Ranks.RANK_FO));
				ex.setSize(1);
				ex.setScore(1);
				ex.setPassFail(true); 
				ex.setDate(p.getCreatedOn());
				ex.setScoredOn(p.getCreatedOn());
				_testHistory.addExam(ex);
			}
		}
	}
}