// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import javax.servlet.http.Cookie;

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
 * @version 2.2
 * @since 1.0
 */

public class RegisterCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(RegisterCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();

		// If we're authenticated, redirect to the home page
		if (ctx.isAuthenticated()) {
			result.setURL("home.do");
			result.setType(CommandResult.REDIRECT);
			result.setSuccess(true);
			return;
		}
		
		// If we're requesting reactivation, just go to the dupe user page
		boolean isDupe = "dupe".equals(ctx.getCmdParameter(OPERATION, null));
		if (isDupe) {
			result.setURL("/jsp/register/duplicateRegistration.jsp");
			result.setSuccess(true);
			return;
		}

		// Save the notification options
		ctx.setAttribute("notifyOptions", ComboUtils.fromArray(Person.NOTIFY_NAMES, Person.NOTIFY_CODES), REQUEST);
		ctx.setAttribute("acTypes", ComboUtils.fromArray(Airport.CODETYPES), REQUEST);
		ctx.setAttribute("timeZones", TZInfo.getAll(), REQUEST);

		// Sort and save the airports
		Map<String, Airport> airports = SystemData.getAirports();
		Collection<Airport> apSet = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		apSet.addAll(airports.values());
		ctx.setAttribute("airports", apSet, REQUEST);
		
		// Check if we ignore the fact that the airline is full
		boolean isFull = false;
		boolean isDupeAddr = false;
		boolean ignoreFull = "force".equals(ctx.getCmdParameter(OPERATION, null));

		// If we're just doing a get, then redirect to the JSP
		if (ctx.getParameter("firstName") == null) {
			try {
				Connection con = ctx.getConnection();
				
				// Check if we've used this IP address before
				GetApplicant adao = new GetApplicant(con);
				isDupeAddr = adao.isIPRegistered(ctx.getRequest().getRemoteAddr(), SystemData.getInt("registration.ip_interval", 1));
				
				// Check our size - if we're overriding then ignore this
				GetStatistics stdao = new GetStatistics(con);
				int size = stdao.getActivePilots(SystemData.get("airline.db"));
				isFull = (size >= SystemData.getInt("users.max", Integer.MAX_VALUE));
				
				// Load manuals to display
				if ((!isFull) || ignoreFull) {
					GetDocuments docdao = new GetDocuments(con);
					ctx.setAttribute("manuals", docdao.getRegistrationManuals(), REQUEST);
				} else
					ctx.setAttribute("airlineSize", Integer.valueOf(size), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
			
			// Save FS Versions
			ctx.setAttribute("fsVersions", ComboUtils.fromArray(Applicant.FSVERSION), REQUEST);
			
			// Forward to the JSP - redirect to seperate page if we're full
			result.setSuccess(true);
			if (isDupeAddr)
				result.setURL("/jsp/register/dupeAddress.jsp");
			else
				result.setURL("/jsp/register/" + ((ignoreFull || (!isFull)) ? "register.jsp" : "regFullWarn.jsp"));
			
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
		a.setSimVersion(ctx.getParameter("fsVersion"));
		a.setLegacyHours(StringUtils.parse(ctx.getParameter("legacyHours"), 0.0));

		// Save the registration host name
		String hostName = ctx.getRequest().getRemoteHost();
		a.setRegisterAddress(ctx.getRequest().getRemoteAddr());
		a.setRegisterHostName(StringUtils.isEmpty(hostName) ? a.getRegisterAddress() : hostName);
		
		// Check for null UI scheme
		if (a.getUIScheme() == null) {
			log.warn("Detected robot from " + hostName);
			result.setURL("/jsp/register/blackList.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Set Notification Options
		Collection<String> notifyOptions = ctx.getParameters("notifyOption");
		if (notifyOptions != null) {
			for (int x = 0; x < Person.NOTIFY_CODES.length; x++)
				a.setNotifyOption(Person.NOTIFY_CODES[x], notifyOptions.contains(Person.NOTIFY_CODES[x]));
		}

		// Initialize the message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("applicant", a);

		// Determine if we do a uniqueness check
		List okAddrs = (List) SystemData.getObject("registration.email.ok");
		boolean checkAddr = (okAddrs == null) || (!okAddrs.contains(a.getEmail()));
		if (!checkAddr)
			log.warn("Skipping address uniqueness checks for " + a.getEmail());
		
		Examination ex = null;
		Pilot eMailFrom = null;
		try {
			Connection con = ctx.getConnection();
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			
			// Check for Suspended User
			StringBuilder buf = new StringBuilder();
			Cookie wc = ctx.getCookie("dvaAuthStatus");
			Cookie fn = ctx.getCookie("dva_fname");
			if (wc != null) {
				buf.append("Suspended Pilot: ");
				try {
					Pilot sp = pdao.get(StringUtils.parseHex(wc.getValue()));
					buf.append(sp.getName());
				} catch (Exception e) {
					buf.append(wc.getValue());
				} finally {
					buf.append("\n");
				}
			}
			if (fn != null) {
				buf.append("PC used to login as: ");
				buf.append(fn.getValue());
				Cookie ln = ctx.getCookie("dva_lname");
				if (ln != null) {
					buf.append(' ');
					buf.append(fn.getValue());
				}
			}
			
			// Add HR comments
			if (buf.length() > 0)
				a.setHRComments(buf.toString());

			// Load the Registration blacklist
			GetSystemData sysdao = new GetSystemData(con);
			Collection<RegistrationBlock> regBList = sysdao.getBlocks();
			
			// Check the blacklist
			int regAddr = NetworkUtils.pack(a.getRegisterAddress());
			for (Iterator<RegistrationBlock> i = regBList.iterator(); i.hasNext(); ) {
				RegistrationBlock rb = i.next();
				boolean doBlock = false;
				boolean fnMatch = ((rb.getFirstName() != null) && (a.getFirstName().equalsIgnoreCase(rb.getFirstName())));
				boolean lnMatch = ((rb.getLastName() != null) && (a.getLastName().equalsIgnoreCase(rb.getLastName())));
				
				if (((regAddr & rb.getNetMask()) == rb.getAddress()) && (rb.getAddress() != 0)) {
					doBlock = true;
					log.warn("Blocking " + a.getRegisterAddress() + ", matches " + NetworkUtils.format(NetworkUtils.convertIP(rb.getAddress()))
							+ "/" + NetworkUtils.format(NetworkUtils.convertIP(rb.getNetMask())));
				} else if (fnMatch && (rb.getLastName() == null)) {
					doBlock = true;
					log.warn("Blocking " + a.getName() + ", matches fName=" + rb.getFirstName());
				} else if (lnMatch && (rb.getFirstName() == null)) {
					doBlock = true;
					log.warn("Blocking " + a.getName() + ", matches lName=" + rb.getLastName());
				} else if (fnMatch && lnMatch) {
					doBlock = true;
					log.warn("Blocking " + a.getName() + ", matches fName= " + rb.getFirstName() + ", lName=" + rb.getLastName());
				} else if ((rb.getHostName() != null) && (a.getRegisterHostName().endsWith(rb.getHostName()))) {
					doBlock = true;
					log.warn("Blocking " + a.getRegisterHostName() + ", matches host=" + rb.getHostName());
				}
			
				// If we're blocked, shut it down
				if (doBlock) {
					ctx.release();
					result.setURL("/jsp/register/" + (rb.getHasUserFeedback() ? "blackList.jsp" : "applicantWelcome.jsp"));
					result.setSuccess(true);
					return;
				}
			}

			// Do address uniqueness check
			if (checkAddr) {
				GetUserData uddao = new GetUserData(con);
				Collection<AirlineInformation> airlines = uddao.getAirlines(true).values();

				// Check for unique name
				GetApplicant adao = new GetApplicant(con);
				for (Iterator<AirlineInformation> i = airlines.iterator(); i.hasNext();) {
					AirlineInformation info = i.next();

					// Check Pilots & applicants
					Set<Integer> dupeResults = new HashSet<Integer>(pdao.checkUnique(a, info.getDB()));
					dupeResults.addAll(adao.checkUnique(a, info.getDB()));
					if (!dupeResults.isEmpty()) {
						ctx.release();
						ctx.setAttribute("appSubmitted", Boolean.TRUE, REQUEST);

						// Save airline
						ctx.setAttribute("airline", info, REQUEST);
						log.warn("Duplicate IDs " + dupeResults.toString() + " found for " + a.getName());

						// Forward to JSP
						result.setURL("/jsp/register/duplicateRegistration.jsp");
						result.setSuccess(true);
						return;
					}
				}
			}

			// Get the e-mail originator
			eMailFrom = pdao.getByName(SystemData.get("registration.from"), SystemData.get("airline.db"));

			// Get the e-mail message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("USERREGISTER"));

			// Get the questionnaire profile
			String examName = SystemData.get("airline.code") + " " + Examination.QUESTIONNAIRE_NAME; 
			GetExamProfiles epdao = new GetExamProfiles(con);
			ExamProfile ep = epdao.getExamProfile(examName);
			if (ep == null)
				throw notFoundException("Invalid Examination - " + examName);

			// Load the question pool for the questionnaire
			ep.setPools(epdao.getSubPools(ep.getName()));
			GetExamQuestions eqdao = new GetExamQuestions(con);
			Collection<QuestionProfile> qPool = eqdao.getQuestionPool(ep, true);
			if (qPool.isEmpty())
				throw notFoundException("Empty Question Pool for " + examName);

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
			ex = new Examination(examName);
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
			for (Iterator<QuestionProfile> i = qPool.iterator(); i.hasNext();) {
				QuestionProfile qp = i.next();
				Question q = null;
				if (qp instanceof MultipleChoice)
					q = new MultiChoiceQuestion((MultiChoiceQuestionProfile) qp);
				else
					q = new Question(qp);

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
		
		// Save the applicant in the request
		ctx.setAttribute("applicant", a, REQUEST);

		// Send an e-mail notification to the user
		boolean isSMTPDebug = SystemData.getBoolean("smtp.testMode");
		Mailer mailer = new Mailer(isSMTPDebug ? a : eMailFrom);
		mailer.setContext(mctxt);
		if (!isSMTPDebug)
			mailer.setCC(Mailer.makeAddress(SystemData.get("airline.mail.hr")));
		
		// Send the message
		mailer.send(a);

		// Forward to the welcome page
		result.setURL("/jsp/register/applicantWelcome.jsp");
		result.setSuccess(true);
	}
}