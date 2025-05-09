// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.Instant;

import javax.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.servinfo.Certificate;
import org.deltava.beans.system.*;
import org.deltava.beans.testing.*;

import org.deltava.comparators.GeoComparator;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.http.GetVATSIMData;

import org.deltava.mail.*;
import org.deltava.security.AddressValidationHelper;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to register a new Applicant.
 * @author Luke
 * @version 11.6
 * @since 1.0
 */

public class RegisterCommand extends AbstractCommand {

	private static final Logger log = LogManager.getLogger(RegisterCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// If we're authenticated, redirect to the home page
		CommandResult result = ctx.getResult();
		if (ctx.isAuthenticated()) {
			result.setURL("home.do");
			result.setType(ResultType.REDIRECT);
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

		// Check if we ignore the fact that the airline is full
		boolean isFull = false;
		boolean isDupeAddr = false;
		boolean ignoreFull = "force".equals(ctx.getCmdParameter(OPERATION, null));

		// If we're just doing a get, then redirect to the JSP
		if (ctx.getParameter("firstName") == null) {
			try {
				Connection con = ctx.getConnection();
				
				// Check if we've used this IP address before
				String remoteAddr = ctx.getRequest().getRemoteAddr();
				GetApplicant adao = new GetApplicant(con);
				isDupeAddr = adao.isIPRegistered(remoteAddr, SystemData.getInt("registration.ip_interval", 1));
				
				// Check our size - if we're overriding then ignore this
				GetStatistics stdao = new GetStatistics(con);
				int size = stdao.getActivePilots(ctx.getDB());
				isFull = (size >= SystemData.getInt("users.max", Integer.MAX_VALUE));
				
				// Load IP data and guess the time zone
				GetIPLocation ipdao = new GetIPLocation(con);
				IPBlock ip = ipdao.get(remoteAddr);
				if (ip != null) {
					GetTimeZone tzdao = new GetTimeZone(con);
					TZInfo tz = tzdao.locate(ip);
					if (tz == null) {
						List<Airport> aps = new ArrayList<Airport>(SystemData.getAirports().values());
						Collections.sort(aps, new GeoComparator(ip));
						tz = aps.get(0).getTZ();
					}
						
					ctx.setAttribute("myTZ", tz, REQUEST);
					ctx.setAttribute("ipInfo", ip, REQUEST);
				}
				
				// Load program names
				GetEquipmentType eqdao = new GetEquipmentType(con);
				Map<Long, Collection<ComboAlias>> eqTypes = new HashMap<Long, Collection<ComboAlias>>();
				for (EquipmentType eq : eqdao.getActive()) {
					if (eq.getNewHires()) {
						Long s = Long.valueOf(eq.getStage());
						Collection<ComboAlias> types = eqTypes.get(s);
						if (types == null) {
							types = new LinkedHashSet<ComboAlias>();
							types.add(ComboUtils.fromString("No Preference", ""));
							eqTypes.put(s, types);
						}
						
						types.add(eq);
					}
				}

				// Save programs
				ctx.setAttribute("eqTypes", eqTypes, REQUEST);
				
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
			
			if (isDupeAddr) {
				result.setURL("/jsp/register/dupeAddress.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Check for an HTTP session
			HttpSession s = ctx.getSession();
			boolean isSession = (s != null) && (s.getAttribute("newSession") != null);
			
			// Forward to the JSP - redirect to seperate page if we're full
			if (!isSession) {
				ctx.setAttribute("newSession", Boolean.TRUE, SESSION);
				ctx.setAttribute("java.util.Locale", Locale.US, SESSION);
				result.setURL("/jsp/register/initSession.jsp");
			} else
				result.setURL("/jsp/register/" + ((ignoreFull || (!isFull)) ? "register.jsp" : "regFullWarn.jsp"));
			
			return;
		}

		// Load the data from the request
		Applicant a = new Applicant(StringUtils.properCase(ctx.getParameter("firstName")), StringUtils.properCase(ctx.getParameter("lastName")));
		a.setStatus(ApplicantStatus.PENDING);
		a.setEmail(ctx.getParameter("email"));
		a.setLocation(ctx.getParameter("location"));
		a.setNetworkID(OnlineNetwork.VATSIM, ctx.getParameter("VATSIM_ID"));
		a.setNetworkID(OnlineNetwork.IVAO, ctx.getParameter("IVAO_ID"));
		a.setNetworkID(OnlineNetwork.PILOTEDGE, ctx.getParameter("PilotEdge_ID"));
		a.setLegacyURL(ctx.getParameter("legacyURL"));
		a.setHomeAirport(ctx.getParameter("homeAirport"));
		a.setEmailAccess(Person.AUTH_EMAIL);
		a.setDistanceType(EnumUtils.parse(DistanceUnit.class, ctx.getParameter("distanceUnits"), DistanceUnit.MI));
		a.setWeightType(EnumUtils.parse(WeightUnit.class, ctx.getParameter("weightUnits"), WeightUnit.LB));
		a.setAirportCodeType(EnumUtils.parse(Airport.Code.class, ctx.getParameter("airportCodeType"), Airport.Code.ICAO));
		a.setTZ(TZInfo.get(ctx.getParameter("tz")));
		a.setUIScheme(ctx.getParameter("uiScheme"));
		a.setComments(ctx.getParameter("comments"));
		a.setDateFormat(ctx.getParameter("df"));
		a.setTimeFormat(ctx.getParameter("tf"));
		a.setNumberFormat(ctx.getParameter("nf"));
		a.setSimVersion(Simulator.fromName(ctx.getParameter("fsVersion"), Simulator.FSX));
		a.setLegacyHours(StringUtils.parse(ctx.getParameter("legacyHours"), 0.0));
		
		// Check for CAPTCHA
		a.setHasCAPTCHA(ctx.passedCAPTCHA());
		if (!a.getHasCAPTCHA())
			a.addHRComment("CAPTCHA Validation failed!");

		// Save the registration host name
		String hostName = ctx.getRequest().getRemoteHost();
		a.setRegisterAddress(ctx.getRequest().getRemoteAddr());
		a.setRegisterHostName(StringUtils.isEmpty(hostName) ? a.getRegisterAddress() : hostName);
		
		// Check for null UI scheme
		if ((a.getUIScheme() == null) || !a.getHasCAPTCHA()) {
			log.warn("Detected robot from {} - CAPTCHA = {}", hostName, Boolean.valueOf(a.getHasCAPTCHA()));
			result.setURL("/jsp/register/blackList.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Set Notification Options
		Collection<String> notifyOptions = ctx.getParameters("notifyOption", Collections.emptyList());
		for (Notification n : Notification.values())
			a.setNotifyOption(n, notifyOptions.contains(n.name()));
		
		// Get eq type preferences
		for (int x = 1; x < 10; x++) {
			String paramName = "s" + x + "prefs";
			if (!StringUtils.isEmpty(ctx.getParameter(paramName)))
				a.setTypeChoice(x, ctx.getParameter(paramName));
		}
		
		// Validate the VATSIM account if any
		if (a.hasNetworkID(OnlineNetwork.VATSIM)) {
			String uri = SystemData.get("online.vatsim.validation_url");
			if (!StringUtils.isEmpty(uri)) {
				try {
					GetVATSIMData dao = new GetVATSIMData();
					Certificate c = dao.getInfo(a.getNetworkID(OnlineNetwork.VATSIM));
					APILogger.add(new APIRequest(API.VATSIM.createName("CERT"), !ctx.isAuthenticated()));
					if (c != null) {
						a.addHRComment("VATSIM ID exists");
						if (!c.isActive())
							a.addHRComment("VATSIM ID is inactive!");
						if (!a.getNetworkID(OnlineNetwork.VATSIM).equals(String.valueOf(c.getID())))
							a.addHRComment("VATSIM ID does not match!");
					} else
						a.addHRComment("Unknown/Invalid VATSIM ID");
				} catch (IllegalStateException ise) {
					log.warn(ise.getMessage());
				} catch (Exception e) {
					log.atError().withThrowable(e).log(e.getMessage());
				}
			}
		}

		// Initialize the message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("applicant", a);

		// Determine if we do a uniqueness check
		List<?> okAddrs = (List<?>) SystemData.getObject("registration.email.ok");
		boolean checkAddr = (okAddrs == null) || (!okAddrs.contains(a.getEmail()));
		if (!checkAddr)
			log.warn("Skipping address uniqueness checks for {}", a.getEmail());
		
		Examination ex = null;
		Pilot eMailFrom = null;
		try {
			Connection con = ctx.getConnection();
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			
			// Check for Suspended User
			
			javax.servlet.http.Cookie wc = ctx.getCookie("dvaAuthStatus");
			javax.servlet.http.Cookie fn = ctx.getCookie("dva_fname64");
			if (wc != null) {
				StringBuilder buf = new StringBuilder("Suspended Pilot: ");
				try {
					Pilot sp = pdao.get(StringUtils.parseHex(wc.getValue()));
					buf.append(sp.getName());
				} catch (Exception e) {
					buf.append(wc.getValue());
				} finally {
					a.addHRComment(buf.toString());
				}
			}
			if (fn != null) {
				Base64.Decoder b64d = Base64.getDecoder();
				StringBuilder buf = new StringBuilder("PC used to login as: ");
				buf.append(new String(b64d.decode(fn.getValue()), StandardCharsets.UTF_8));
				javax.servlet.http.Cookie ln = ctx.getCookie("dva_lname64");
				if (ln != null) {
					buf.append(' ');
					buf.append(new String(b64d.decode(ln.getValue()), StandardCharsets.UTF_8));
				}
				
				a.addHRComment(buf.toString());
			}
			
			// Check for blacklist
			GetSystemData sysdao = new GetSystemData(con);
			BlacklistEntry be = sysdao.getBlacklist(a.getRegisterAddress());
			if (be != null) {
				log.warn("Registration blacklist for {} from {}", a.getName(), be);
				result.setURL("/jsp/register/blackList.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Do address uniqueness check
			if (checkAddr) {
				GetUserData uddao = new GetUserData(con);
				Collection<AirlineInformation> airlines = uddao.getAirlines(true).values();

				// Check for unique name
				GetApplicant adao = new GetApplicant(con);
				for (AirlineInformation info : airlines) {
					Set<Integer> dupeResults = new HashSet<Integer>(pdao.checkUnique(a, info.getDB(), 28));
					dupeResults.addAll(adao.checkUnique(a, info.getDB(), 28));
					if (!dupeResults.isEmpty()) {
						ctx.release();
						ctx.setAttribute("appSubmitted", Boolean.TRUE, REQUEST);

						// Save airline
						ctx.setAttribute("airline", info, REQUEST);
						log.warn("Duplicate IDs {} found for {}", dupeResults, a.getName());

						// Forward to JSP
						result.setURL("/jsp/register/duplicateRegistration.jsp");
						result.setSuccess(true);
						return;
					}
				}
			}
			
			// Set the default equipment type
			GetEquipmentType eqdao = new GetEquipmentType(con);
			a.setEquipmentType(eqdao.getDefault(ctx.getDB()));

			// Get the e-mail originator
			eMailFrom = pdao.getByCode(SystemData.get("registration.from"));

			// Get the e-mail message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("USERREGISTER"));

			// Get the questionnaire profile
			String examName = SystemData.get("airline.code") + " " + Examination.QUESTIONNAIRE_NAME; 
			GetExamProfiles epdao = new GetExamProfiles(con);
			ExamProfile ep = epdao.getExamProfile(examName);
			if (ep == null)
				throw notFoundException(String.format("Invalid Examination - %s", examName));

			// Load the question pool for the questionnaire
			GetExamQuestions exqdao = new GetExamQuestions(con);
			Collection<QuestionProfile> qPool = exqdao.getQuestionPool(ep, true);
			if (qPool.isEmpty())
				throw notFoundException(String.format("Empty Question Pool for %s", examName));

			// Start the transaction
			ctx.startTX();

			// Get the DAO and write to the database
			SetApplicant wdao = new SetApplicant(con);
			wdao.write(a);

			// Create an entry that marks our email as invalid
			AddressValidation addrValid = new AddressValidation(a.getID(), a.getEmail());
			addrValid.setHash(AddressValidationHelper.calculateCRC32(a.getEmail()));
			ctx.setAttribute("addrValid", addrValid, REQUEST);
			mctxt.addData("addr", addrValid);

			// Create the examination
			ex = new Examination(examName);
			ex.setAuthorID(a.getID());
			ex.setStage(1);
			ex.setStatus(TestStatus.NEW);
			mctxt.addData("questionnaire", ex);

			// Set the creation/expiration date/times
			ex.setDate(Instant.now());
			ex.setExpiryDate(ex.getDate().plusSeconds(SystemData.getInt("registration.auto_reject", 5) * 86400 - 3600));

			// Add the questions to the exam
			int qNum = 0;
			for (QuestionProfile qp : qPool) {
				Question q = qp.toQuestion();
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
		if (a.getHasCAPTCHA()) {
			boolean isSMTPDebug = SystemData.getBoolean("smtp.testMode");
			Mailer mailer = new Mailer(isSMTPDebug ? a : eMailFrom);
			mailer.setContext(mctxt);
			if (!isSMTPDebug)
				mailer.setCC(MailUtils.makeAddress(SystemData.get("airline.mail.hr")));
		
			// Send the message
			mailer.send(a);
		}

		// Invalidate the temporary session
		HttpSession s = ctx.getSession();
		if (s != null)
			s.invalidate();
		
		// Forward to the welcome page
		result.setURL("/jsp/register/applicantWelcome.jsp");
		result.setSuccess(true);
	}
}