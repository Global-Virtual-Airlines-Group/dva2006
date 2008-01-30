// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.testing.Test;
import org.deltava.beans.system.*;
import org.deltava.beans.ts2.*;

import org.deltava.comparators.RankComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.*;
import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle editing/saving Pilot Profiles.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class ProfileCommand extends AbstractFormCommand {

	private static final Logger log = Logger.getLogger(ProfileCommand.class);

	private static final String[] PRIVACY_ALIASES = { "0", "1", "2" };
	private static final String[] PRIVACY_NAMES = { "Show address to Staff Members only",
			"Show address to Authenticated Users", "Show address to All Visitors" };

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			List<StatusUpdate> updates = new ArrayList<StatusUpdate>();

			// Get the Pilot Profile and e-mail configuration to update
			GetPilotDirectory rdao = new GetPilotDirectory(con);
			GetPilotEMail edao = new GetPilotEMail(con);
			Pilot p = rdao.get(ctx.getID());
			EMailConfiguration emailCfg = edao.getEMailInfo(ctx.getID());

			// Get the Staff Profile if it exists
			GetStaff rsdao = new GetStaff(con);
			Staff s = rsdao.get(ctx.getID());

			// Check our access level to the Pilot profile
			PilotAccessControl p_access = new PilotAccessControl(ctx, p);
			p_access.validate();
			if (!p_access.getCanEdit())
				throw securityException("Cannot edit Pilot " + p.getName());

			// Check our access level to the Staff/Email profiles
			StaffAccessControl s_access = new StaffAccessControl(ctx, s);
			s_access.validate();
			MailboxAccessControl m_access = new MailboxAccessControl(ctx, emailCfg);
			m_access.validate();

			// Update the profile with data from the request
			p.setHomeAirport(ctx.getParameter("homeAirport"));
			p.setNetworkID(OnlineNetwork.VATSIM, ctx.getParameter("VATSIM_ID"));
			p.setNetworkID(OnlineNetwork.IVAO, ctx.getParameter("IVAO_ID"));
			p.setLocation(ctx.getParameter("location"));
			p.setIMHandle(InstantMessage.AIM, ctx.getParameter("aimHandle"));
			p.setIMHandle(InstantMessage.MSN, ctx.getParameter("msnHandle"));
			p.setMotto(ctx.getParameter("motto"));
			p.setEmailAccess(StringUtils.parse(ctx.getParameter("privacyOption"), Person.HIDE_EMAIL));
			p.setTZ(TZInfo.get(ctx.getParameter("tz")));
			p.setAirportCodeType(ctx.getParameter("airportCodeType"));
			p.setMapType(ctx.getParameter("mapType"));
			p.setUIScheme(ctx.getParameter("uiScheme"));
			p.setDateFormat(ctx.getParameter("df"));
			p.setTimeFormat(ctx.getParameter("tf"));
			p.setNumberFormat(ctx.getParameter("nf"));

			// Get Discussion Forum option checkboxes
			p.setShowSignatures(Boolean.valueOf(ctx.getParameter("showSigs")).booleanValue());
			p.setShowSSThreads(Boolean.valueOf(ctx.getParameter("showImageThreads")).booleanValue());
			p.setShowNewPosts(Boolean.valueOf(ctx.getParameter("scrollToNewPosts")).booleanValue());
			p.setHasDefaultSignature(Boolean.valueOf(ctx.getParameter("useDefaultSig")).booleanValue());

			// Set Notification Options
			Collection<String> notifyOpts = ctx.getParameters("notifyOption");
			if (notifyOpts != null)
				for (int x = 0; x < Person.NOTIFY_CODES.length; x++)
					p.setNotifyOption(Person.NOTIFY_CODES[x], notifyOpts.contains(Person.NOTIFY_CODES[x]));
			else
				for (int x = 0; x < Person.NOTIFY_CODES.length; x++)
					p.setNotifyOption(Person.NOTIFY_CODES[x], false);

			// Determine if we are changing the pilot's status
			String newStatus = ctx.getParameter("status");
			if (p_access.getCanChangeStatus() && (newStatus != null)) {
				if (!p.getStatusName().equals(newStatus)) {
					p.setStatus(newStatus);
					ctx.setAttribute("statusUpdated", Boolean.TRUE, REQUEST);

					// Write the Status Update entry
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Status changed to " + p.getStatusName());
					updates.add(upd);
					log.info(p.getName() + " " + upd.getDescription());
				}

				// Check Discussion Forum access
				boolean coolerPostsLocked = Boolean.valueOf(ctx.getParameter("noCooler")).booleanValue();
				if (coolerPostsLocked != p.getNoCooler()) {
					p.setNoCooler(coolerPostsLocked);
					String forumName = SystemData.get("airline.forum");

					// Write the status update entry
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription(forumName + " posts " + (coolerPostsLocked ? "locked out" : "enabled"));
					updates.add(upd);
					log.warn(p.getName() + " " + upd.getDescription());
				}

				// Check Testing Center access
				boolean examsLocked = Boolean.valueOf(ctx.getParameter("noExams")).booleanValue();
				if (examsLocked != p.getNoExams()) {
					p.setNoExams(examsLocked);

					// Write the status update entry
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription(examsLocked ? "Testing Center locked out" : "Testing Center enabled");
					updates.add(upd);
					log.warn(p.getName() + " " + upd.getDescription());
				}

				// Check Voice server access
				boolean voiceLocked = Boolean.valueOf(ctx.getParameter("noVoice")).booleanValue();
				if (voiceLocked != p.getNoVoice()) {
					p.setNoVoice(voiceLocked);

					// Write the status update entry
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription(examsLocked ? "Voice access locked out" : "Voice access enabled");
					updates.add(upd);
					log.warn(p.getName() + " " + upd.getDescription());
				}

				// Check ACARS server access
				int newACARSRest = StringUtils.arrayIndexOf(Pilot.RESTRICT, ctx.getParameter("ACARSrestrict"));
				if (newACARSRest != p.getACARSRestriction()) {
					p.setACARSRestriction(newACARSRest);

					// Write the status update entry
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("ACARS restrictions set to " + p.getACARSRestrictionName());
					updates.add(upd);
					log.warn(p.getName() + " " + upd.getDescription());
				}
			}

			// Update legacy hours
			if (p_access.getCanChangeStatus() && (ctx.getParameter("legacyHours") != null))
				p.setLegacyHours(Double.parseDouble(ctx.getParameter("legacyHours")));

			// Load the ratings from the request and convert to a set to maintain uniqueness
			Collection<String> newRatings = ctx.getParameters("ratings");
			if (newRatings == null)
				newRatings = new LinkedHashSet<String>(p.getRatings());

			// Determine if we are promoting the pilot
			String newRank = ctx.getParameter("rank");
			String newEQ = ctx.getParameter("eqType");
			if (p_access.getCanPromote() && (newRank != null) && (newEQ != null)) {
				boolean eqChange = !p.getEquipmentType().equals(newEQ);
				boolean rankChange = !p.getRank().equals(newRank);

				// Check if anything has changed
				if (eqChange || rankChange) {
					ctx.setAttribute("rankUpdated", Boolean.valueOf(rankChange), REQUEST);
					ctx.setAttribute("eqUpdated", Boolean.valueOf(eqChange), REQUEST);

					// Get the DAO and the new equipment type
					GetEquipmentType eqdao = new GetEquipmentType(con);
					EquipmentType eq1 = eqdao.get(p.getEquipmentType());
					EquipmentType eq2 = eqdao.get(newEQ);
					if (eq2 == null)
						throw notFoundException("Unknown Equipment Type program - " + newEQ);

					// Figure out if this is truly a promotion
					@SuppressWarnings("unchecked")
					RankComparator rcmp = new RankComparator((List) SystemData.getObject("ranks"));
					rcmp.setRank2(p.getRank(), eq1.getStage());
					rcmp.setRank1(newRank, eq2.getStage());

					// Update the rank/equipment program
					p.setRank(newRank);
					p.setEquipmentType(newEQ);

					// Load the ratings from the new equipment type
					newRatings.addAll(eq2.getRatings());

					// Check if we're going to Senior Captain for the first time
					GetStatusUpdate sudao = new GetStatusUpdate(con);
					boolean newSC = ((newRank.equals(Ranks.RANK_SC)) && !sudao.isSeniorCaptain(p.getID()));

					// Write the status update
					if (rcmp.compare() > 0) {
						int promoType = StatusUpdate.INTPROMOTION;
						if (newSC)
							promoType = StatusUpdate.SR_CAPTAIN;
						else if (eqChange)
							promoType = StatusUpdate.EXTPROMOTION;

						// Build the status update
						StatusUpdate upd = new StatusUpdate(p.getID(), promoType);
						upd.setAuthorID(ctx.getUser().getID());
						upd.setDescription("Promoted to " + newRank + ", " + newEQ);
						updates.add(upd);
					} else {
						StatusUpdate upd = new StatusUpdate(p.getID(), newSC ? StatusUpdate.SR_CAPTAIN
								: StatusUpdate.RANK_CHANGE);
						upd.setAuthorID(ctx.getUser().getID());
						upd.setDescription("Rank Changed to " + newRank + ", " + newEQ);
						updates.add(upd);
					}
				}
			}

			// Update the Pilot's equipment type ratings
			if ((p_access.getCanPromote()) && CollectionUtils.hasDelta(newRatings, p.getRatings())) {
				// Figure out what ratings have been added
				Collection<String> addedRatings = CollectionUtils.getDelta(newRatings, p.getRatings());
				if (!addedRatings.isEmpty()) {
					ctx.setAttribute("addedRatings", addedRatings, REQUEST);
					p.addRatings(addedRatings);

					// Note the changed ratings
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.RATING_ADD);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Ratings added: " + StringUtils.listConcat(addedRatings, ", "));
					updates.add(upd);
					log.info(upd.getDescription());
				}

				// Figure out what ratings have been removed
				Collection<String> removedRatings = CollectionUtils.getDelta(p.getRatings(), newRatings);
				if (!removedRatings.isEmpty()) {
					ctx.setAttribute("removedRatings", removedRatings, REQUEST);
					p.removeRatings(removedRatings);

					// Note the changed ratings
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.RATING_REMOVE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Ratings removed: " + StringUtils.listConcat(removedRatings, ", "));
					updates.add(upd);
					log.info(upd.getDescription());
				}
			}

			// Load the roles from the request and convert to a set to maintain uniqueness
			String[] roles = ctx.getRequest().getParameterValues("securityRoles");
			Collection<String> newRoles = p_access.getCanChangeRoles() ? CollectionUtils.loadList(roles,
					new HashSet<String>()) : p.getRoles();
			newRoles.add("Pilot");

			// Update LDAP name
			if (p_access.getCanChangeRoles())
				p.setLDAPName(ctx.getParameter("uid"));

			// Update the Pilot's Security Roles
			if ((p_access.getCanChangeRoles()) && CollectionUtils.hasDelta(newRoles, p.getRoles())) {
				// Figure out what roles have been added
				Collection<String> addedRoles = CollectionUtils.getDelta(newRoles, p.getRoles());
				if (!addedRoles.isEmpty()) {
					ctx.setAttribute("addedRoles", addedRoles, REQUEST);
					p.addRoles(addedRoles);

					// Note the changed roles
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.SECURITY_ADD);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Roles added: " + StringUtils.listConcat(addedRoles, ", "));
					updates.add(upd);
				}

				// Figure out what roles have been removed
				Collection<String> removedRoles = CollectionUtils.getDelta(p.getRoles(), newRoles);
				if (!removedRoles.isEmpty()) {
					ctx.setAttribute("removedRoles", removedRoles, REQUEST);
					p.removeRoles(removedRoles);

					// Note the changed roles
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.SECURITY_REMOVE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Roles removed: " + StringUtils.listConcat(removedRoles, ", "));
					updates.add(upd);
				}
			}

			// Turn off auto-commit
			ctx.startTX();

			// Save or remove the signature image if found
			SetSignatureImage sigdao = new SetSignatureImage(con);
			FileUpload imgData = ctx.getFile("coolerImg");
			if (imgData != null) {
				// Check the image
				ImageInfo info = new ImageInfo(imgData.getBuffer());
				boolean imgOK = info.check();

				// Check the image dimensions
				int maxX = SystemData.getInt("cooler.sig_max.x");
				int maxY = SystemData.getInt("cooler.sig_max.y");
				if (imgOK && ((info.getWidth() > maxX) || (info.getHeight() > maxY))) {
					imgOK = false;
					ctx.setMessage("Your Signature Image is too large. (Max = " + maxX + "x" + maxY + ", Yours = "
							+ info.getWidth() + "x" + info.getHeight());
				}

				// Check the image size
				int maxSize = SystemData.getInt("cooler.sig_max.size");
				if (imgOK && (imgData.getSize() > maxSize)) {
					imgOK = false;
					ctx.setMessage("Your signature Image is too large. (Max = " + maxSize + "bytes, Yours ="
							+ imgData.getSize() + " bytes");
				}

				// Update the image if it's OK
				if (imgOK) {
					p.load(imgData.getBuffer());
					sigdao.write(p, info.getWidth(), info.getHeight(), info.getFormatName());
					ctx.setAttribute("sigUpdated", Boolean.TRUE, REQUEST);
					log.info("Signature Updated");
				}
			} else if (Boolean.valueOf(ctx.getParameter("removeCoolerImg")).booleanValue()) {
				sigdao.delete(p.getID());
				ctx.setAttribute("sigRemoved", Boolean.TRUE, REQUEST);
				log.info("Signature Removed");
			}

			// Update the e-mail configuration if necessary
			boolean isDelete = Boolean.valueOf(ctx.getParameter("IMAPDelete")).booleanValue();
			if (isDelete && m_access.getCanDelete()) {
				SetPilotEMail ewdao = new SetPilotEMail(con);
				ewdao.delete(p.getID());
				ctx.setAttribute("imapDelete", Boolean.TRUE, REQUEST);
				emailCfg = null;
			} else if (m_access.getCanEdit()) {
				emailCfg.setAddress(ctx.getParameter("IMAPAddr"));
				emailCfg.setMailDirectory(ctx.getParameter("IMAPPath"));
				emailCfg.setQuota(Integer.parseInt(ctx.getParameter("IMAPQuota")));
				emailCfg.setActive(Boolean.valueOf(ctx.getParameter("IMAPActive")).booleanValue());
				emailCfg.setAliases(StringUtils.split(ctx.getParameter("IMAPAliases"), ","));

				// Save the profile
				SetPilotEMail ewdao = new SetPilotEMail(con);
				ewdao.update(emailCfg, p.getName());
			}

			// If we have access to the Staff profile, update it
			if (s_access.getCanEdit()) {
				s.setBody(ctx.getParameter("staffBody"));

				// Load special parameters
				boolean removeStaffProfile = false;
				if (p_access.getCanChangeRoles()) {
					s.setTitle(ctx.getParameter("staffTitle"));
					s.setSortOrder(StringUtils.parse(ctx.getParameter("staffSort"), 6));
					s.setArea(ctx.getParameter("staffArea"));
					removeStaffProfile = Boolean.valueOf(ctx.getParameter("removeStaff")).booleanValue();
				}

				// Update the staff profile table
				SetStaff swdao = new SetStaff(con);
				if (removeStaffProfile) {
					swdao.delete(p.getID());
					ctx.setAttribute("spRemoved", Boolean.TRUE, REQUEST);
					log.info("Staff Profile Removed");
				} else {
					swdao.write(s);
					ctx.setAttribute("spUpdated", Boolean.TRUE, REQUEST);
					ctx.setAttribute("staff", s, REQUEST);
					log.info("Staff Profile Updated");
				}
			}

			// Only allow e-mail updates if in HR
			if (ctx.isUserInRole("HR") && (ctx.getUser().getID() != p.getID())) {
				p.setEmail(ctx.getParameter("email"));
				SetAddressValidation avwdao = new SetAddressValidation(con);
				avwdao.delete(p.getID());
			}

			// Update the pilot name
			boolean nameChanged = (!p.getFirstName().equals(ctx.getParameter("firstName")))
					|| (!p.getLastName().equals(ctx.getParameter("lastName")));
			if (p_access.getCanChangeStatus() && nameChanged) {
				Pilot p2 = p.cloneExceptID();
				p2.setFirstName(ctx.getParameter("firstName"));
				p2.setLastName(ctx.getParameter("lastName"));

				// Get the databases
				GetUserData uddao = new GetUserData(con);
				Collection<AirlineInformation> airlines = uddao.getAirlines(true).values();

				// Get the Applicant Read DAO and search for our applicant record
				GetApplicant adao = new GetApplicant(con);
				Applicant a = adao.getByPilotID(p.getID());

				// Check for unique names
				Collection<Integer> dupeResults = new HashSet<Integer>();
				for (Iterator<AirlineInformation> i = airlines.iterator(); i.hasNext();) {
					AirlineInformation info = i.next();

					// Check Pilots & applicants
					dupeResults.addAll(rdao.checkUnique(p2, info.getDB()));
					dupeResults.addAll(adao.checkUnique(p2, info.getDB()));

					// Remove our entry, or that of our applicant entry
					dupeResults.remove(new Integer(p.getID()));
					if (a != null)
						dupeResults.remove(new Integer(a.getID()));
				}

				// If we're unique, continue the update
				if (dupeResults.isEmpty()) {
					String newDN = "cn=" + p2.getName() + "," + SystemData.get("security.baseDN");

					// Create the status update
					StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Renamed from " + p.getName() + " to " + p2.getName());
					updates.add(upd);

					// Rename the user in the Directory if it's not just a case-sensitivity issue
					Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
					if (auth instanceof SQLAuthenticator) {
						SQLAuthenticator sqlAuth = (SQLAuthenticator) auth;
						sqlAuth.setConnection(con);
						sqlAuth.rename(p, newDN);
						sqlAuth.clearConnection();
					} else
						auth.rename(p, newDN);

					p.setDN(newDN);
					p.setFirstName(p2.getFirstName());
					p.setLastName(p2.getLastName());

					// Set status attribute
					ctx.setAttribute("newName", p.getName(), REQUEST);
				} else {
					// Load the Pilot profiles
					Map<Integer, Person> users = new LinkedHashMap<Integer, Person>();
					UserDataMap udm = uddao.get(dupeResults);
					users.putAll(rdao.get(udm));
					users.putAll(adao.get(udm));

					// Save the Pilot profiles
					ctx.setAttribute("userData", udm, REQUEST);
					ctx.setAttribute("dupeResults", users, REQUEST);
				}
			}

			// Update teamspeak data
			if ((p.getStatus() == Pilot.ACTIVE) && (!StringUtils.isEmpty(p.getPilotCode()))) {
				GetTS2Data ts2dao = new GetTS2Data(con);
				Collection<Server> srvs = ts2dao.getServers(p.getID());
				Collection<Server> newSrvs = ts2dao.getServers(p.getRoles());
				List<Client> usrs = ts2dao.getUsers(p.getID());

				// Get the TS2 password
				String pwd = usrs.isEmpty() ? "$dummy" : usrs.get(0).getPassword();

				// Determine what TeamSpeak servers to remove us from
				SetTS2Data ts2wdao = new SetTS2Data(con);
				Collection<Server> rmvServers = CollectionUtils.getDelta(srvs, newSrvs);
				for (Iterator<Server> i = rmvServers.iterator(); i.hasNext();) {
					Server srv = i.next();
					log.info("Removed " + p.getPilotCode() + " from TeamSpeak server " + srv.getName());
					Collection<Integer> ids = new HashSet<Integer>();
					ids.add(new Integer(p.getID()));
					ts2wdao.removeUsers(srv, ids);
				}

				// Determine what servers to add us to
				Collection<Server> addServers = CollectionUtils.getDelta(newSrvs, srvs);
				if (!addServers.isEmpty()) {
					Collection<Client> ts2usrs = new HashSet<Client>();
					for (Iterator<Server> i = addServers.iterator(); i.hasNext();) {
						Server srv = i.next();
						log.info("Added " + p.getPilotCode() + " to TeamSpeak server " + srv.getName());

						// Build the client record
						Client c = new Client(p.getPilotCode());
						c.setPassword(pwd);
						c.setID(p.getID());
						c.addChannels(srv);
						c.setServerID(srv.getID());
						c.setAutoVoice(RoleUtils.hasAccess(p.getRoles(), srv.getRoles().get(Server.VOICE)));
						c.setServerOperator(RoleUtils.hasAccess(p.getRoles(), srv.getRoles().get(Server.OPERATOR)));
						c.setServerAdmin(RoleUtils.hasAccess(p.getRoles(), srv.getRoles().get(Server.ADMIN)));
						ts2usrs.add(c);
					}

					// Add to the servers
					ts2wdao.write(ts2usrs);
				}
			} else if (!StringUtils.isEmpty(p.getPilotCode())) {
				log.info("Removed " + p.getPilotCode() + " from TeamSpeak servers");
				SetTS2Data ts2wdao = new SetTS2Data(con);
				ts2wdao.delete(p.getID());
			}

			// Write the Pilot profile
			SetPilot pwdao = new SetPilot(con);
			pwdao.write(p);

			// If we're marking Inactive/Retired, purge any Inactivity/Address Update records and remove from Child Authenticators
			if ((p.getStatus() != Pilot.ACTIVE) && (p.getStatus() != Pilot.ON_LEAVE)) {
				SetInactivity idao = new SetInactivity(con);
				SetAddressValidation avwdao = new SetAddressValidation(con);
				idao.delete(p.getID());
				avwdao.delete(p.getID());
				
				// Remove the user from any destination directories
				Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
				if (auth instanceof MultiAuthenticator) {
					MultiAuthenticator mAuth = (MultiAuthenticator) auth;
					if (auth instanceof SQLAuthenticator) {
						SQLAuthenticator sqlAuth = (SQLAuthenticator) auth;
						sqlAuth.setConnection(con);
						mAuth.removeDestination(p);
						sqlAuth.clearConnection();
					} else
						mAuth.removeDestination(p);
				}
			}

			// Write the status updates
			SetStatusUpdate stwdao = new SetStatusUpdate(con);
			stwdao.write(updates);

			// If we're updating the password, then save it
			if (!StringUtils.isEmpty(ctx.getParameter("pwd1"))) {
				p.setPassword(ctx.getParameter("pwd1"));

				// If we have an IMAP mailbox, sync there first
				if (emailCfg != null) {
					SetPilotEMail ewdao = new SetPilotEMail(con);
					ewdao.updatePassword(p.getID(), p.getPassword());
				}

				Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
				if (auth instanceof SQLAuthenticator) {
					SQLAuthenticator sqlAuth = (SQLAuthenticator) auth;
					sqlAuth.setConnection(con);
					sqlAuth.updatePassword(p, p.getPassword());
					sqlAuth.clearConnection();
				} else
					auth.updatePassword(p, p.getPassword());

				ctx.setAttribute("pwdUpdate", Boolean.TRUE, REQUEST);
			}

			// Commit the transaction
			ctx.commitTX();

			// Save the pilot profile in the request
			ctx.setAttribute("pilot", p, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Save time zones and notification/privacy options
		ctx.setAttribute("timeZones", TZInfo.getAll(), REQUEST);
		ctx.setAttribute("notifyOptions", ComboUtils.fromArray(Person.NOTIFY_NAMES, Person.NOTIFY_CODES), REQUEST);
		ctx.setAttribute("privacyOptions", ComboUtils.fromArray(PRIVACY_NAMES, PRIVACY_ALIASES), REQUEST);
		ctx.setAttribute("acTypes", ComboUtils.fromArray(Airport.CODETYPES), REQUEST);
		ctx.setAttribute("mapTypes", ComboUtils.fromArray(Pilot.MAP_TYPES), REQUEST);
		ctx.setAttribute("acarsRest", ComboUtils.fromArray(Pilot.RESTRICT), REQUEST);
		
		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the pilot profile
			GetPilotDirectory dao = new GetPilotDirectory(con);
			p = dao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());

			// load the email configuration
			GetPilotEMail edao = new GetPilotEMail(con);
			EMailConfiguration emailCfg = edao.getEMailInfo(ctx.getID());
			if (emailCfg != null)
				ctx.setAttribute("emailCfg", emailCfg, REQUEST);

			// Get the staff profile (if any)
			GetStaff stdao = new GetStaff(con);
			Staff s = stdao.get(ctx.getID());
			if (s != null)
				ctx.setAttribute("staff", s, REQUEST);

			// Check our access
			PilotAccessControl ac = new PilotAccessControl(ctx, p);
			ac.validate();
			if (!ac.getCanEdit())
				throw securityException("Not Authorized");

			// Calculate mailbox access
			MailboxAccessControl m_access = new MailboxAccessControl(ctx, emailCfg);
			m_access.validate();

			// Save access controllers
			ctx.setAttribute("access", ac, REQUEST);
			ctx.setAttribute("m_access", m_access, REQUEST);

			// Get the Online Hours/Legs if not already loaded
			GetFlightReports frdao = new GetFlightReports(con);
			frdao.getOnlineTotals(p, SystemData.get("airline.db"));

			// Save the pilot profile in the request
			ctx.setAttribute("pilot", p, REQUEST);

			// Get all equipment type profiles
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);

			// Get all equipment types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);

			// Get Pilot Examinations
			GetExam exdao = new GetExam(con);
			ctx.setAttribute("exams", exdao.getExams(p.getID()), REQUEST);

			// Get status updates
			GetStatusUpdate updao = new GetStatusUpdate(con);
			Collection<StatusUpdate> upds = updao.getByUser(p.getID(), SystemData.get("airline.db"));
			ctx.setAttribute("statusUpdates", upds, REQUEST);
			
			// Get Author IDs from Status Updates
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<StatusUpdate> i = upds.iterator(); i.hasNext(); ) {
				StatusUpdate upd = i.next();
				IDs.add(new Integer(upd.getAuthorID()));
			}
			
			// Load authors
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("authors", dao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Don't allow manual switching to Suspended if the pilot isn't already in that status
		if (p.getStatus() != Pilot.SUSPENDED) {
			List<ComboAlias> statuses = ComboUtils.fromArray(Pilot.STATUS); 
			statuses.remove(Pilot.SUSPENDED);
			ctx.setAttribute("statuses", statuses, REQUEST);
		} else
			ctx.setAttribute("statuses", ComboUtils.fromArray(Pilot.STATUS), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotEdit.jsp");
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
			
			// Load the User Data
			GetUserData uddao = new GetUserData(con);
			UserData usrInfo = uddao.get(ctx.getID());
			if (usrInfo == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());

			// If it's in a different database check our role
			boolean crossDB = !SystemData.get("airline.db").equalsIgnoreCase(usrInfo.getDB());
			if (crossDB && !ctx.isUserInRole("Admin"))
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());

			// Get the DAO and load the pilot profile
			GetPilot dao = new GetPilot(con);
			Pilot p = dao.get(usrInfo);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());

			// Get the access controller
			PilotAccessControl access = crossDB ? new CrossAppPilotAccessControl(ctx, p) : new PilotAccessControl(ctx, p);
			access.validate();

			// Check if we can view examinations
			if (access.getCanViewExams()) {
				GetExam exdao = new GetExam(con);
				Collection<Test> exams = exdao.getExams(p.getID());
				for (Iterator<Test> i = exams.iterator(); i.hasNext();) {
					Test t = i.next();
					try {
						ExamAccessControl ac = new ExamAccessControl(ctx, t, usrInfo);
						ac.validate();
					} catch (AccessControlException ace) {
						i.remove();
					}
				}

				// Save remaining exams
				ctx.setAttribute("exams", exams, REQUEST);
			}

			// Check for an applicant profile and login data
			if (ctx.isUserInRole("HR") && !crossDB) {
				GetApplicant adao = new GetApplicant(con);
				GetLoginData ldao = new GetLoginData(con);
				ctx.setAttribute("applicant", adao.getByPilotID(p.getID()), REQUEST);
				ctx.setAttribute("loginAddrs", ldao.getAddresses(p.getID()), REQUEST);
			}

			// Get Academy Certifications
			if (!crossDB) {
				GetAcademyCourses fadao = new GetAcademyCourses(con);
				ctx.setAttribute("courses", fadao.getCompleted(p.getID(), "C.STARTDATE"), REQUEST);
			}

			// Get status updates
			GetStatusUpdate updao = new GetStatusUpdate(con);
			Collection<StatusUpdate> upds = updao.getByUser(p.getID(), usrInfo.getDB());
			ctx.setAttribute("statusUpdates", upds, REQUEST);
			
			// Get Author IDs from Status Updates
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<StatusUpdate> i = upds.iterator(); i.hasNext(); ) {
				StatusUpdate upd = i.next();
				IDs.add(new Integer(upd.getAuthorID()));
			}
			
			// Load authors
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("authors", dao.get(udm), REQUEST);
			
			// Get the online totals
			if (p.getACARSLegs() < 0) {
				GetFlightReports prdao = new GetFlightReports(con);
				prdao.getOnlineTotals(p, usrInfo.getDB());
			}

			// Get TeamSpeak2 data
			if (SystemData.getBoolean("airline.voice.ts2.enabled") && !crossDB) {
				GetTS2Data ts2dao = new GetTS2Data(con);
				ctx.setAttribute("ts2Servers", CollectionUtils.createMap(ts2dao.getServers(p.getRoles()), "ID"), REQUEST);
				ctx.setAttribute("ts2Clients", ts2dao.getUsers(p.getID()), REQUEST);
			}

			// Save the pilot profile and ratings in the request
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("crossDB", Boolean.valueOf(crossDB), REQUEST);
			ctx.setAttribute("airport", SystemData.getAirport(p.getHomeAirport()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotRead.jsp");
		result.setSuccess(true);
	}
}