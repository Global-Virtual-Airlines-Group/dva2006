// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.Person;
import org.deltava.beans.academy.*;

import org.deltava.dao.*;

/**
 * A class to support Web Site Commands use a {@link AcademyHistoryHelper} object to determine what
 * Flight Academy examinations/courses a Pilot is eligible for.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractAcademyHistoryCommand extends AbstractCommand {
	
	protected AcademyHistoryHelper _academyHistory;
	protected Collection<Certification> _allCerts;

	/**
	 * Populates the Testing History Helper by calling the proper DAOs in the right order.
	 * @param p the Pilot bean
	 * @param c the JDBC connection to use
	 * @throws DAOException if a JDBC error occurs
	 */
	protected final void initHistory(Person p, Connection c) throws DAOException {
		
		// Load all certifications and the Pilot's courses
		GetAcademyCourses cdao = new GetAcademyCourses(c);
		GetAcademyCertifications crdao = new GetAcademyCertifications(c);
		_allCerts = crdao.getAll();
		_academyHistory = new AcademyHistoryHelper(cdao.getByPilot(p.getID()), _allCerts);
		_academyHistory.setAllowInactive(p.isInRole("Instructor"));
		
		// Get the Pilot's examinations and check rides
		GetExam exdao = new GetExam(c);
		_academyHistory.addExams(exdao.getExams(p.getID()));
	}
}