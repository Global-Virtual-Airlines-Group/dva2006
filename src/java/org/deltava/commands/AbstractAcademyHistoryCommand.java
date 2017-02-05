// Copyright 2006, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.testing.Test;

import org.deltava.comparators.*;
import org.deltava.dao.*;

/**
 * A class to support Web Site Commands use a {@link AcademyHistoryHelper} object to determine what
 * Flight Academy examinations/courses a Pilot is eligible for.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public abstract class AbstractAcademyHistoryCommand extends AbstractCommand {
	
	/**
	 * Populates the Academy Testing History Helper by calling the proper DAOs in the right order.
	 * @param p the Pilot bean
	 * @param c the JDBC connection to use
	 * @return a populated AcademyHistoryHelper bean
	 * @throws DAOException if a JDBC error occurs
	 */
	protected static final AcademyHistoryHelper initHistory(Pilot p, Connection c) throws DAOException {
		
		// Load the piliot's cross-airline IDs
		GetUserData uddao = new GetUserData(c);
		UserData ud = uddao.get(p.getID());
		
		// Get the DAOs
		GetExam exdao = new GetExam(c);
		GetFlightReports frdao = new GetFlightReports(c);
		GetEquipmentType eqdao = new GetEquipmentType(c);
		GetAcademyCourses cdao = new GetAcademyCourses(c);
		GetAcademyCertifications crdao = new GetAcademyCertifications(c);
		
		// Load exams and courses across all airlines
		List<Course> courses = new ArrayList<Course>(); List<Test> exams = new ArrayList<Test>();
		List<FlightReport> flights = new ArrayList<FlightReport>(); Collection<EquipmentType> eqTypes = new LinkedHashSet<EquipmentType>();
		eqTypes.addAll(eqdao.getActive());
		for (Integer xID : ud.getIDs()) {
			int id = xID.intValue();
			exams.addAll(exdao.getExams(id));
			courses.addAll(cdao.getByPilot(id));
			if (id != p.getID()) {
				UserData ud2 = uddao.get(id);
				flights.addAll(frdao.getByPilot(id, null, ud2.getDB()));
				eqTypes.addAll(eqdao.getActive(ud2.getDB()));
			} else
				flights.addAll(frdao.getByPilot(id, null));
		}
		
		// Sort the lists
		Collections.sort(courses);
		Collections.sort(exams, new TestComparator(TestComparator.DATE));
		Collections.sort(flights, new FlightReportComparator(FlightReportComparator.DATE));
		
		// Load all certifications and return the helper
		AcademyHistoryHelper helper = new AcademyHistoryHelper(p, courses, crdao.getAll());
		helper.addExams(exams);
		helper.addFlights(flights);
		eqTypes.forEach(eq -> helper.addPrimaryRatings(eq));
		helper.setAllowInactive(p.isInRole("Instructor") || p.isInRole("AcademyAdmin") || p.isInRole("AcademyAudit"));
		return helper;
	}
}