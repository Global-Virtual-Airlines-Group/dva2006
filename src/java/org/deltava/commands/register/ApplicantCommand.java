// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.comparators.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ApplicantAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command for processing Applicant Profiles.
 * @author Luke
 * @version 4.0
 * @since 1.0
 */

public class ApplicantCommand extends AbstractFormCommand {
	
	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we are doing a hire at the same time
		boolean doHire = Boolean.valueOf(ctx.getParameter("doHire")).booleanValue();
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Applicant
			GetApplicant dao = new GetApplicant(con);
			Applicant a = dao.get(ctx.getID());
			if (a == null)
				throw notFoundException("Invalid Applicant - " + ctx.getID());

			// Check our access level
			ApplicantAccessControl access = new ApplicantAccessControl(ctx, a);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit Applicant");

			// Make sure we can do the hire as well
			doHire = doHire && access.getCanApprove();

			// Update the applicant from the request
			a.setFirstName(ctx.getParameter("firstName"));
			a.setLastName(ctx.getParameter("lastName"));
			a.setEmail(ctx.getParameter("email"));
			a.setLocation(ctx.getParameter("location"));
			a.setIMHandle(IMAddress.AIM, ctx.getParameter("aimHandle"));
			a.setIMHandle(IMAddress.MSN, ctx.getParameter("msnHandle"));
			a.setNetworkID(OnlineNetwork.VATSIM, ctx.getParameter("VATSIM_ID"));
			a.setNetworkID(OnlineNetwork.IVAO, ctx.getParameter("IVAO_ID"));
			a.setLegacyURL(ctx.getParameter("legacyURL"));
			a.setLegacyVerified("1".equals(ctx.getParameter("legacyOK")));
			a.setLegacyHours(StringUtils.parse(ctx.getParameter("legacyHours"), 0.0d));
			a.setHomeAirport(ctx.getParameter("homeAirport"));
			a.setEmailAccess(Person.AUTH_EMAIL);
			a.setDateFormat(ctx.getParameter("df"));
			a.setTimeFormat(ctx.getParameter("tf"));
			a.setNumberFormat(ctx.getParameter("nf"));
			a.setAirportCodeType(Airport.Code.valueOf(ctx.getParameter("airportCodeType")));
			a.setTZ(TZInfo.get(ctx.getParameter("tz")));
			a.setUIScheme(ctx.getParameter("uiScheme"));
			a.setHRComments(ctx.getParameter("HRcomments"));

			// Save hire fields
			a.setEquipmentType(ctx.getParameter("eqType"));
			a.setRank(Rank.fromName(ctx.getParameter("rank")));

			// Set Notification Options
			Collection<String> notifyOptions = ctx.getParameters("notifyOption");
			if (notifyOptions != null) {
				for (Notification n : Notification.values())
					a.setNotifyOption(n, notifyOptions.contains(n.name()));
			}

			// Save the applicant in the request
			ctx.setAttribute("applicant", a, REQUEST);

			// Get the DAO and write to the database
			SetApplicant wdao = new SetApplicant(con);
			wdao.write(a);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP or redirect to the hire command
		CommandResult result = ctx.getResult();
		result.setSuccess(true);
		if (doHire) {
			result.setType(ResultType.REDIRECT);
			result.setURL("apphire", null, ctx.getID());
		} else {
			result.setType(ResultType.REQREDIRECT);
			result.setURL("/jsp/register/applicantUpdate.jsp");
		}
	}

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Save the notification options
		ctx.setAttribute("acTypes", ComboUtils.fromArray(Airport.Code.values()), REQUEST);
		ctx.setAttribute("timeZones", TZInfo.getAll(), REQUEST);

		// Sort and save the airports
		Map<String, Airport> airports = SystemData.getAirports();
		Set<Airport> apSet = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		apSet.addAll(airports.values());
		ctx.setAttribute("airports", apSet, REQUEST);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Applicant
			GetApplicant dao = new GetApplicant(con);
			Applicant a = dao.get(ctx.getID());
			if (a == null)
				throw notFoundException("Invalid Applicant - " + ctx.getID());

			// Check our access level
			ApplicantAccessControl access = new ApplicantAccessControl(ctx, a);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit Applicant");

			// Check if the address has been validated
			GetAddressValidation avdao = new GetAddressValidation(con);
			ctx.setAttribute("eMailValid", Boolean.valueOf(avdao.isValid(a.getID())), REQUEST);

			// Do a soundex check on the user
			soundexCheck(a, con, ctx);

			// Get Active Equipment programs
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);

			// Get the questionnaire
			GetQuestionnaire exdao = new GetQuestionnaire(con);
			ctx.setAttribute("questionnaire", exdao.getByApplicantID(a.getID()), REQUEST);

			// Get the applicant home airport
			ctx.setAttribute("homeAirport", SystemData.getAirport(a.getHomeAirport()), REQUEST);

			// Save the applicant and the access controller
			ctx.setAttribute("applicant", a, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/register/applicantEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Applicant
			GetApplicant dao = new GetApplicant(con);
			Applicant a = dao.get(ctx.getID());
			if (a == null)
				throw notFoundException("Invalid Applicant - " + ctx.getID());

			// Check our access level
			ApplicantAccessControl access = new ApplicantAccessControl(ctx, a);
			access.validate();
			if (!access.getCanRead())
				throw securityException("Cannot view Applicant");

			// Check if the address has been validated
			GetAddressValidation avdao = new GetAddressValidation(con);
			ctx.setAttribute("eMailValid", Boolean.valueOf(avdao.isValid(a.getID())), REQUEST);
			
			// Validate that the name is not a duplicate
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Map<Integer, Pilot> matches = pdao.getByID(pdao.checkUnique(a, SystemData.get("airline.db")), "PILOTS");
			for (Iterator<Integer> i = matches.keySet().iterator(); i.hasNext(); ) {
				int id = i.next().intValue();
				if (id == a.getPilotID())
					i.remove();
			}
			
			// Save duplicates
			ctx.setAttribute("nameMatches", matches.values(), REQUEST);

			// Do a soundex and netmask check on the applicant
			soundexCheck(a, con, ctx);
			netmaskCheck(a, con, ctx);

			// Get the questionnaire
			GetQuestionnaire exdao = new GetQuestionnaire(con);
			ctx.setAttribute("questionnaire", exdao.getByApplicantID(a.getID()), REQUEST);

			// Get Active Equipment programs and counts
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);

			// Get the applicant home airport
			ctx.setAttribute("homeAirport", SystemData.getAirport(a.getHomeAirport()), REQUEST);
			ctx.setAttribute("statusName", Applicant.STATUS[a.getStatus()], REQUEST);

			// Save the applicant and the access controller
			ctx.setAttribute("applicant", a, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/register/applicantView.jsp");
		result.setSuccess(true);
	}

	/**
	 * Helper method to perform the netmask check.
	 */
	private void netmaskCheck(Applicant a, Connection c, CommandContext ctx) throws DAOException {
		
		// Get the user's netblock
		GetIPLocation ipdao = new GetIPLocation(c);
		IPAddressInfo addrInfo = ipdao.get(a.getRegisterAddress());
		ctx.setAttribute("addrInfo", addrInfo, REQUEST);
		
		// Initialize the DAO
		GetApplicant dao = new GetApplicant(c);

		// Do a netmask check on the applicant against each database
		Collection<Integer> netmaskIDs = new HashSet<Integer>();
		Collection<?> airlines = ((Map<?, ?>) SystemData.getObject("apps")).values();
		for (Iterator<?> i = airlines.iterator(); i.hasNext() && (addrInfo != null); ) {
			AirlineInformation info = (AirlineInformation) i.next();
			netmaskIDs.addAll(dao.checkAddress(addrInfo.getBlock(), info.getDB()));
		}
		
		// Load the locations of all these matches
		GetUserData uddao = new GetUserData(c);
		UserDataMap udmap = uddao.get(netmaskIDs);

		// Load the users objects
		GetPilot pdao = new GetPilot(c);
		Map<Integer, Person> persons = new HashMap<Integer, Person>();
		for (Iterator<String> i = udmap.getTableNames().iterator(); i.hasNext();) {
			String tableName = i.next();
			Collection<UserData> IDs = udmap.getByTable(tableName);
			if (UserData.isPilotTable(tableName))
				persons.putAll(pdao.getByID(IDs, tableName));
			else
				persons.putAll(dao.getByID(IDs, tableName));
		}
		
		// Save the persons in the request
		List<Person> users = new ArrayList<Person>(persons.values());
		Collections.sort(users, new PersonComparator<Person>(PersonComparator.CREATED));
		Collections.reverse(users);
		if (users.size() > 65)
			users.subList(0, users.size() - 65).clear();
		
		ctx.setAttribute("netmaskUsers", users, REQUEST);
	}

	/**
	 * Helper method to perform the soundex check.
	 */
	private void soundexCheck(Applicant a, Connection c, CommandContext ctx) throws DAOException {

		// Initialize the DAOs
		GetApplicant dao = new GetApplicant(c);
		GetPilotDirectory pdao = new GetPilotDirectory(c);

		// Do a soundex check on the applicant against each database
		Collection<Integer> soundexIDs = new LinkedHashSet<Integer>();
		Collection<?> airlines = ((Map<?, ?>) SystemData.getObject("apps")).values();
		for (Iterator<?> i = airlines.iterator(); i.hasNext();) {
			AirlineInformation info = (AirlineInformation) i.next();
			soundexIDs.addAll(dao.checkSoundex(a, info.getDB()));
			soundexIDs.addAll(pdao.checkSoundex(a, info.getDB()));
		}
		
		// If we have too many, abort
		if (soundexIDs.size() > 1500)
			soundexIDs.clear();

		// Load the locations of all these matches
		GetUserData uddao = new GetUserData(c);
		UserDataMap udmap = uddao.get(soundexIDs);

		// Load the users objects
		Map<Integer, Person> persons = new HashMap<Integer, Person>();
		for (Iterator<String> i = udmap.getTableNames().iterator(); i.hasNext();) {
			String tableName = i.next();
			Collection<UserData> IDs = udmap.getByTable(tableName);
			if (UserData.isPilotTable(tableName))
				persons.putAll(pdao.getByID(IDs, tableName));
			else
				persons.putAll(dao.getByID(IDs, tableName));
		}

		// Filter out applicants where the pilot already matches
		for (Iterator<Person> i = persons.values().iterator(); i.hasNext();) {
			Person p = i.next();
			if (p instanceof Applicant) {
				Applicant ap = (Applicant) p;
				if (persons.containsKey(new Integer(ap.getPilotID())))
					i.remove();
			}
		}

		// Save the persons in the request
		AbstractComparator<Person> cmp = new PersonComparator<Person>(PersonComparator.CREATED);
		cmp.setReverseSort(true);
		Collection<Person> users = new TreeSet<Person>(cmp);
		users.addAll(persons.values());
		ctx.setAttribute("soundexUsers", users, REQUEST);
	}
}