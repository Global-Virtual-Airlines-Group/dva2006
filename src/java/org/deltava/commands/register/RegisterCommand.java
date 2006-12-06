// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.beans.system.*;
import org.deltava.beans.testing.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.AddressValidationHelper;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to register a new Applicant.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RegisterCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(RegisterCommand.class);

	// Package-private since ApplicantCommand uses these
	static final String[] NOTIFY_ALIASES = { Person.NEWS, Person.EVENT, Person.FLEET, Person.PIREP };
	static final String[] NOTIFY_NAMES = { "Send News Notifications", "Send Event Notifications",
			"Send Library Notifications", "Send Flight Approval Notifications" };

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();
		
		// Check for spambots
		if ("...".equals(ctx.getParameter("firstName"))) {
			log.error("Detected spambot from " + ctx.getRequest().getRemoteHost());
			result.setURL("/jsp/register/applicantWelcome.jsp");
			result.setSuccess(true);
			return;
		}

		// Save the notification options
		ctx.setAttribute("notifyOptions", ComboUtils.fromArray(NOTIFY_NAMES, NOTIFY_ALIASES), REQUEST);
		ctx.setAttribute("acTypes", ComboUtils.fromArray(Airport.CODETYPES), REQUEST);
		ctx.setAttribute("timeZones", TZInfo.getAll(), REQUEST);

		// Sort and save the airports
		Map<String, Airport> airports = SystemData.getAirports();
		Set<Airport> apSet = new TreeSet<Airport>(new AirportComparator<Airport>(AirportComparator.NAME));
		apSet.addAll(airports.values());
		ctx.setAttribute("airports", apSet, REQUEST);

		// If we're just doing a get, then redirect to the JSP
		if (ctx.getParameter("firstName") == null) {
			result.setURL("/jsp/register/register.jsp");
			result.setSuccess(true);
			return;
		}

		// Load the data from the request
		Applicant a = new Applicant(StringUtils.properCase(ctx.getParameter("firstName")), StringUtils.properCase(ctx
				.getParameter("lastName")));
		a.setStatus(Applicant.PENDING);
		a.setEmail(ctx.getParameter("email"));
		a.setLocation(ctx.getParameter("location"));
		a.setIMHandle(InstantMessage.AIM, ctx.getParameter("aimHandle"));
		a.setIMHandle(InstantMessage.MSN, ctx.getParameter("msnHandle"));
		a.setNetworkID(OnlineNetwork.VATSIM, ctx.getParameter("VATSIM_ID"));
		a.setNetworkID(OnlineNetwork.IVAO, ctx.getParameter("IVAO_ID"));
		a.setLegacyURL(ctx.getParameter("legacyURL"));
		a.setHomeAirport(ctx.getParameter("homeAirport"));
		a.setEmailAccess(Person.AUTH_EMAIL);
		a.setAirportCodeType(ctx.getParameter("airportCodeType"));
		a.setTZ(TZInfo.get(ctx.getParameter("tz")));
		a.setUIScheme(ctx.getParameter("uiScheme"));
		a.setComments(ctx.getParameter("comments"));
		a.setDateFormat(ctx.getParameter("df"));
		a.setTimeFormat(ctx.getParameter("tf"));
		a.setNumberFormat(ctx.getParameter("nf"));

		// Save the registration host name
		String hostName = ctx.getRequest().getRemoteHost();
		a.setRegisterAddress(ctx.getRequest().getRemoteAddr());
		a.setRegisterHostName(StringUtils.isEmpty(hostName) ? a.getRegisterAddress() : hostName);

		// Parse legacy hours
		try {
			a.setLegacyHours(Double.parseDouble(ctx.getParameter("legacyHours")));
		} catch (NumberFormatException nfe) {
			a.setLegacyHours(0);
		}

		// Set Notification Options
		Collection<String> notifyOptions = ctx.getParameters("notifyOption");
		if (notifyOptions != null) {
			for (int x = 0; x < NOTIFY_ALIASES.length; x++)
				a.setNotifyOption(NOTIFY_ALIASES[x], notifyOptions.contains(NOTIFY_ALIASES[x]));
		}

		// Save the applicant in the request
		ctx.setAttribute("applicant", a, REQUEST);

		// Log registration
		log.info("Commencing registration for " + a.getName());

		// Initialize the message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("applicant", a);

		Examination ex = null;
		Pilot eMailFrom = null;
		try {
			Connection con = ctx.getConnection();

			// Get the databases
			GetUserData uddao = new GetUserData(con);
			Collection<AirlineInformation> airlines = uddao.getAirlines(true).values();

			// Get the Pilot/Applicant Read DAOs
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			GetApplicant adao = new GetApplicant(con);

			// Check for unique name
			for (Iterator<AirlineInformation> i = airlines.iterator(); i.hasNext();) {
				AirlineInformation info = i.next();

				// Check Pilots & applicants
				Set<Integer> dupeResults = new HashSet<Integer>(pdao.checkUnique(a, info.getDB()));
				dupeResults.addAll(adao.checkUnique(a, info.getDB()));
				if (!dupeResults.isEmpty()) {
					ctx.release();

					// Save airline
					ctx.setAttribute("airline", info, REQUEST);
					log.warn("Duplicate IDs " + dupeResults.toString() + " found for " + a.getName());

					// Forward to JSP
					result.setURL("/jsp/register/duplicateRegistration.jsp");
					result.setSuccess(true);
					return;
				}
			}

			// Get the e-mail originator
			eMailFrom = pdao.getByName(SystemData.get("registration.from"), SystemData.get("airline.db"));

			// Get the e-mail message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("USERREGISTER"));

			// Get the questionnaire profile
			GetExamProfiles epdao = new GetExamProfiles(con);
			ExamProfile ep = epdao.getExamProfile(Examination.QUESTIONNAIRE_NAME);
			if (ep == null)
				throw notFoundException("Invalid Examination - " + Examination.QUESTIONNAIRE_NAME);

			// Load the question pool for the questionnaire
			epdao.setQueryMax(ep.getSize());
			Collection<QuestionProfile> qPool = epdao.getQuestionPool(Examination.QUESTIONNAIRE_NAME, true, true);
			if (qPool.isEmpty())
				throw notFoundException("Empty Question Pool for " + Examination.QUESTIONNAIRE_NAME);

			// Start the transaction
			ctx.startTX();

			// Get the DAO and write to the database
			SetApplicant wdao = new SetApplicant(con);
			wdao.write(a);

			// Create an entry that marks our email as invalid
			AddressValidation addrValid = new AddressValidation(a.getID(), a.getEmail());
			addrValid.setHash(AddressValidationHelper.calculateHashCode(a.getEmail()));
			ctx.setAttribute("addrValid", addrValid, REQUEST);
			mctxt.addData("addr", addrValid);

			// Create the examination
			ex = new Examination(Examination.QUESTIONNAIRE_NAME);
			ex.setPilotID(a.getID());
			ex.setStage(1);
			ex.setStatus(Test.NEW);
			mctxt.addData("questionnaire", ex);

			// Set the creation/expiration date/times
			Calendar cld = Calendar.getInstance();
			ex.setDate(cld.getTime());
			cld.add(Calendar.DATE, SystemData.getInt("registration.auto_reject"));
			cld.add(Calendar.HOUR, -1);
			ex.setExpiryDate(cld.getTime());

			// Add the questions to the exam
			int qNum = 0;
			for (Iterator i = qPool.iterator(); i.hasNext();) {
				QuestionProfile qp = (QuestionProfile) i.next();
				Question q = new Question(qp);
				q.setNumber(++qNum);
				ex.addQuestion(q);
			}

			// Save the examination
			ctx.setAttribute("questionnaire", ex, REQUEST);

			// Get the DAO and write the questionnaire to the database
			SetQuestionnaire qwdao = new SetQuestionnaire(con);
			qwdao.write(ex);

			// Get the DAO and write the address validation entry
			SetAddressValidation wavdao = new SetAddressValidation(con);
			wavdao.write(addrValid);

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send an e-mail notification to the user
		Mailer mailer = new Mailer(SystemData.getBoolean("smtp.testMode") ? a : eMailFrom);
		mailer.setContext(mctxt);
		mailer.send(a);

		// Forward to the welcome page
		result.setURL("/jsp/register/applicantWelcome.jsp");
		result.setSuccess(true);
	}
}