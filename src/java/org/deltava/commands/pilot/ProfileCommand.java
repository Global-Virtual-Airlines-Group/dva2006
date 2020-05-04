// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.academy.Course;
import org.deltava.beans.acars.Restriction;
import org.deltava.beans.cooler.SignatureImage;
import org.deltava.beans.servinfo.Certificate;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.testing.Test;
import org.deltava.beans.system.*;

import org.deltava.comparators.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.security.*;
import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;

/**
 * A Web Site Command to handle editing/saving Pilot Profiles.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ProfileCommand extends AbstractFormCommand {

	private static final Logger log = Logger.getLogger(ProfileCommand.class);

	private static final String[] PRIVACY_ALIASES = { "0", "1", "2" };
	private static final String[] PRIVACY_NAMES = { "Show address to Staff Members only", "Show address to Authenticated Users", "Show address to All Visitors" };

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			Collection<StatusUpdate> updates = new ArrayList<StatusUpdate>();

			// Get the Pilot Profile and e-mail configuration to update
			GetPilotDirectory rdao = new GetPilotDirectory(con);
			GetPilotEMail edao = new GetPilotEMail(con);
			Pilot p = rdao.get(ctx.getID());
			IMAPConfiguration emailCfg = edao.getEMailInfo(ctx.getID());

			// Get the Staff Profile if it exists
			GetStaff rsdao = new GetStaff(con);
			Staff s = rsdao.get(ctx.getID());

			// Check our access level to the Pilot profile
			PilotAccessControl p_access = new PilotAccessControl(ctx, p);
			p_access.validate();
			if (!p_access.getCanEdit())
				throw securityException("Cannot edit Pilot " + p.getName());

			// Check our access level to the Staff profile
			StaffAccessControl s_access = new StaffAccessControl(ctx, s);
			s_access.validate();
			
			// Validate VATSIM ID
			if (!StringUtils.isEmpty(ctx.getParameter("VATSIM_ID"))) {
				String vid = ctx.getParameter("VATSIM_ID");
				String uri = SystemData.get("online.vatsim.validation_url");
				if (!StringUtils.isEmpty(uri)) {
					try {
						GetVATSIMData dao = new GetVATSIMData();
						Certificate c = dao.getInfo(ctx.getParameter("VATSIM_ID"));
						APILogger.add(new APIRequest(API.VATSIM.createName("CERT"), !ctx.isAuthenticated()));
						if (c != null) {
							Collection<String> msgs = new ArrayList<String>();
							if (!c.isActive())
								msgs.add("VATSIM ID inactive");
							if (!vid.equals(String.valueOf(c.getID())))
								msgs.add("VATSIM ID does not match");
							if (!p.getFirstName().equalsIgnoreCase(c.getFirstName()))
								msgs.add("First Name does not match");
							if (!p.getLastName().equalsIgnoreCase(c.getLastName()))
								msgs.add("Last Name does not match");
							
							// Save messages
							if (msgs.isEmpty() || ctx.isUserInRole("HR"))
								p.setNetworkID(OnlineNetwork.VATSIM, vid);
							if (!msgs.isEmpty()) {
								if (ctx.isUserInRole("HR"))
									msgs.add(c.toString());
								else
									vid = null;
								
								ctx.setAttribute("vatsimValidationMsgs", msgs, REQUEST);
							}
						} else {
							ctx.setAttribute("vatsimValidationMsgs", "Unknown/Invalid VATSIM ID - " + vid, REQUEST);
							vid = null;
						}
					} catch (IllegalStateException ise) {
						log.warn(ise);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					} finally {
						if (!StringUtils.isEmpty(vid)) {
							p.setNetworkID(OnlineNetwork.VATSIM, vid);
							ctx.setAttribute("vatsimOK", Boolean.TRUE, REQUEST);
						}
					}
				} else
					p.setNetworkID(OnlineNetwork.VATSIM, vid);
			} else
				p.setNetworkID(OnlineNetwork.VATSIM, null);

			// Update the profile with data from the request
			p.setHomeAirport(ctx.getParameter("homeAirport"));
			p.setNetworkID(OnlineNetwork.IVAO, ctx.getParameter("IVAO_ID"));
			p.setNetworkID(OnlineNetwork.PILOTEDGE, ctx.getParameter("PilotEdge_ID"));
			p.setMotto(ctx.getParameter("motto"));
			p.setEmailAccess(StringUtils.parse(ctx.getParameter("privacyOption"), Person.HIDE_EMAIL));
			p.setTZ(TZInfo.get(ctx.getParameter("tz")));
			p.setDistanceType(DistanceUnit.valueOf(ctx.getParameter("distanceUnits")));
			p.setWeightType(WeightUnit.valueOf(ctx.getParameter("weightUnits")));
			p.setAirportCodeType(Airport.Code.valueOf(ctx.getParameter("airportCodeType")));
			p.setMapType(MapType.valueOf(ctx.getParameter("mapType")));
			p.setUIScheme(ctx.getParameter("uiScheme"));
			p.setViewCount(StringUtils.parse(ctx.getParameter("viewCount"), SystemData.getInt("html.table.viewSize")));
			p.setDateFormat(ctx.getParameter("df"));
			p.setTimeFormat(ctx.getParameter("tf"));
			p.setNumberFormat(ctx.getParameter("nf"));
			p.setShowNavBar(Boolean.valueOf(ctx.getParameter("showNavBar")).booleanValue());

			// Update IM handles
			for (IMAddress im : IMAddress.values()) {
				if (im.getIsVisible())
					p.setIMHandle(im, ctx.getParameter(im.toString() + "Handle"));
			}
			
			// Set location
			if (!StringUtils.isEmpty(ctx.getParameter("location")))
				p.setLocation(ctx.getParameter("location"));

			// Get Discussion Forum option checkboxes
			p.setShowSignatures(Boolean.valueOf(ctx.getParameter("showSigs")).booleanValue());
			p.setShowSSThreads(Boolean.valueOf(ctx.getParameter("showImageThreads")).booleanValue());
			p.setShowNewPosts(Boolean.valueOf(ctx.getParameter("scrollToNewPosts")).booleanValue());
			p.setHasDefaultSignature(Boolean.valueOf(ctx.getParameter("useDefaultSig")).booleanValue());

			// Set Notification Options
			Collection<String> notifyOpts = ctx.getParameters("notifyOption");
			if (notifyOpts != null)
				Arrays.asList(Notification.values()).forEach(n -> p.setNotifyOption(n, notifyOpts.contains(n.name())));
			else
				Arrays.asList(Notification.values()).forEach(n -> p.setNotifyOption(n, false));

			// Determine if we are changing the pilot's status
			if (p_access.getCanChangeStatus() || p_access.getCanChangeRoles()) {
				// Check Discussion Forum access
				boolean coolerPostsLocked = Boolean.valueOf(ctx.getParameter("noCooler")).booleanValue();
				if (coolerPostsLocked != p.getNoCooler()) {
					p.setNoCooler(coolerPostsLocked);
					String forumName = SystemData.get("airline.forum");
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription(forumName + " posts " + (coolerPostsLocked ? "locked out" : "enabled"));
					updates.add(upd);
					log.info(p.getName() + " " + upd.getDescription());
				}

				// Check Testing Center access
				boolean examsLocked = Boolean.valueOf(ctx.getParameter("noExams")).booleanValue();
				if (examsLocked != p.getNoExams()) {
					p.setNoExams(examsLocked);
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription(examsLocked ? "Testing Center locked out" : "Testing Center enabled");
					updates.add(upd);
					log.info(p.getName() + " " + upd.getDescription());
				}

				// Check Voice server access
				boolean voiceLocked = Boolean.valueOf(ctx.getParameter("noVoice")).booleanValue();
				if (voiceLocked != p.getNoVoice()) {
					p.setNoVoice(voiceLocked);
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription(examsLocked ? "Voice access locked out" : "Voice access enabled");
					updates.add(upd);
					log.info(p.getName() + " " + upd.getDescription());
				}

				// Check ACARS server access
				Restriction newACARSRest = Restriction.valueOf(ctx.getParameter("ACARSrestrict"));
				if (newACARSRest != p.getACARSRestriction()) {
					p.setACARSRestriction(newACARSRest);
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("ACARS restrictions set to " + p.getACARSRestriction().getName());
					updates.add(upd);
					log.info(p.getName() + " " + upd.getDescription());
				}
				
				// Check ACARS Time compression access
				boolean compressLocked = Boolean.valueOf(ctx.getParameter("noTimeCompress")).booleanValue();
				if (compressLocked != p.getNoTimeCompression()) {
					p.setNoTimeCompression(compressLocked);
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription(compressLocked ? "Time Compression locked out" : "Time Compression enabled");
					updates.add(upd);
					log.info(p.getName() + " " + upd.getDescription());
				}
				
				// Check permanent account status
				boolean newPermAccount = Boolean.valueOf(ctx.getParameter("permAccount")).booleanValue();
				if (newPermAccount != p.getIsPermanent()) {
					p.setIsPermanent(newPermAccount);
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.STATUS_CHANGE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Permanent account flag " + (newPermAccount ? "Set" : "Cleared"));
					updates.add(upd);
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
			Rank newRank = Rank.fromName(ctx.getParameter("rank"));
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
					RankComparator rcmp = new RankComparator();
					rcmp.setRank2(p.getRank(), eq1.getStage());
					rcmp.setRank1(newRank, eq2.getStage());

					// Update the rank/equipment program
					p.setRank(newRank);
					p.setEquipmentType(newEQ);

					// Load the ratings from the new equipment type
					newRatings.addAll(eq2.getRatings());

					// Check if we're going to Senior Captain for the first time
					GetStatusUpdate sudao = new GetStatusUpdate(con);
					boolean newSC = ((newRank == Rank.SC) && !sudao.isSeniorCaptain(p.getID()));

					// Write the status update
					if (rcmp.compare() > 0) {
						UpdateType promoType = UpdateType.INTPROMOTION;
						if (newSC)
							promoType = UpdateType.SR_CAPTAIN;
						else if (eqChange)
							promoType = UpdateType.EXTPROMOTION;

						// Build the status update
						StatusUpdate upd = new StatusUpdate(p.getID(), promoType);
						upd.setAuthorID(ctx.getUser().getID());
						upd.setDescription("Promoted to " + newRank + ", " + newEQ);
						updates.add(upd);
					} else {
						StatusUpdate upd = new StatusUpdate(p.getID(), newSC ? UpdateType.SR_CAPTAIN : UpdateType.RANK_CHANGE);
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
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.RATING_ADD);
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
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.RATING_REMOVE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Ratings removed: " + StringUtils.listConcat(removedRatings, ", "));
					updates.add(upd);
					log.info(upd.getDescription());
				}
			}

			// Load the roles from the request and convert to a set to maintain uniqueness
			Collection<String> newRoles = p_access.getCanChangeRoles() ? ctx.getParameters("securityRoles", new HashSet<String>()) : p.getRoles();
			newRoles.add(Role.PILOT.getName());

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
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.SECURITY_ADD);
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
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.SECURITY_REMOVE);
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
					ctx.setMessage("Your Signature Image is too large. (Max = " + maxX + "x" + maxY + ", Yours = " + info.getWidth() + "x" + info.getHeight() + ")");
				}

				// Check the image size
				int maxSize = SystemData.getInt("cooler.sig_max.size");
				if (imgOK && (imgData.getSize() > maxSize)) {
					imgOK = false;
					ctx.setMessage("Your signature Image is too large. (Max = " + maxSize + "bytes, Yours =" + imgData.getSize() + " bytes)");
				}

				// Update the image if it's OK
				if (imgOK) {
					boolean isAuth = ctx.isUserInRole("HR") && Boolean.valueOf(ctx.getParameter("isAuthSig")).booleanValue();
					if (isAuth) {
						SignatureImage si = new SignatureImage(p.getID());
						si.load(imgData.getBuffer());
						si.watermark("Approved Signature", si.getWidth() - 120, si.getHeight() - 4);
						try {
							p.load(si.getImage("png"));
						} catch (Exception e) {
							throw new DAOException(e);
						}
						
						// Write status update
						StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.COMMENT);
						upd.setAuthorID(ctx.getUser().getID());
						upd.setDescription("Approved Signature");
						updates.add(upd);
					} else
						p.load(imgData.getBuffer());

					// Save the image
					sigdao.write(p, info.getWidth(), info.getHeight(), info.getFormatName(), isAuth);
					ctx.setAttribute("sigUpdated", Boolean.TRUE, REQUEST);
					log.info("Signature Updated");
				}
			} else if (Boolean.valueOf(ctx.getParameter("removeCoolerImg")).booleanValue()) {
				sigdao.delete(p.getID());
				ctx.setAttribute("sigRemoved", Boolean.TRUE, REQUEST);
				log.info("Signature Removed");
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
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.COMMENT);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Staff Profile removed");
					updates.add(upd);

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
				p.setEmailInvalid(false);
				SetAddressValidation avwdao = new SetAddressValidation(con);
				avwdao.delete(p.getID());
			}

			// Update the pilot name
			boolean nameChanged = (!p.getFirstName().equals(ctx.getParameter("firstName"))) || (!p.getLastName().equals(ctx.getParameter("lastName")));
			if ((p_access.getCanChangeStatus() || p_access.getCanChangeRoles()) && nameChanged) {
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
				for (AirlineInformation info : airlines) {
					dupeResults.addAll(rdao.checkUnique(p2, info.getDB()));
					dupeResults.addAll(adao.checkUnique(p2, info.getDB(), 21));

					// Remove our entry, or that of our applicant entry
					dupeResults.remove(Integer.valueOf(p.getID()));
					if (a != null)
						dupeResults.remove(Integer.valueOf(a.getID()));
				}

				// If we're unique, continue the update
				if (dupeResults.isEmpty() || ctx.isUserInRole("HR")) {
					String newDN = "cn=" + p2.getName() + ",o=" + SystemData.get("airline.code");

					// Create the status update
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.STATUS_CHANGE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Renamed from " + p.getName() + " to " + p2.getName());
					updates.add(upd);

					// Rename the user in the Directory if it's not just a case-sensitivity issue
					try (Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR)) {
						if (auth instanceof SQLAuthenticator) ((SQLAuthenticator) auth).setConnection(con);
						auth.rename(p, newDN);
					}

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
					ctx.setAttribute("dupeResults", users.values(), REQUEST);
				}
			}

			// Write the Pilot profile
			SetPilot pwdao = new SetPilot(con);
			pwdao.write(p);
			
			// If we're marking Inactive/Retired, purge any Inactivity/Address Update records and remove from Child Authenticators
			if ((p.getStatus() != PilotStatus.ACTIVE) && (p.getStatus() != PilotStatus.ONLEAVE)) {
				SetInactivity idao = new SetInactivity(con);
				SetAddressValidation avwdao = new SetAddressValidation(con);
				idao.delete(p.getID());
				avwdao.delete(p.getID());
				
				// Remove the user from any destination directories
				Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
				if (auth instanceof MultiAuthenticator) {
					try (MultiAuthenticator mAuth = (MultiAuthenticator) auth) {
						if (auth instanceof SQLAuthenticator)
							((SQLAuthenticator) auth).setConnection(con);
					
						mAuth.removeDestination(p);
					}
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
					try (SQLAuthenticator sqlAuth = (SQLAuthenticator) auth) {
						sqlAuth.setConnection(con);
						sqlAuth.updatePassword(p, p.getPassword());
					}
				} else
					auth.updatePassword(p, p.getPassword());

				ctx.setAttribute("pwdUpdate", Boolean.TRUE, REQUEST);
			}

			// Commit the transaction
			ctx.commitTX();
			
			// Invalidate the Pilot cache across applications
			EventDispatcher.send(new UserEvent(SystemEvent.Type.USER_INVALIDATE, p.getID()));
			
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
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Save privacy options
		ctx.setAttribute("privacyOptions", ComboUtils.fromArray(PRIVACY_NAMES, PRIVACY_ALIASES), REQUEST);
		
		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the pilot profile
			GetPilot dao = new GetPilot(con);
			p = dao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());

			// load the email configuration
			GetPilotEMail edao = new GetPilotEMail(con);
			IMAPConfiguration emailCfg = edao.getEMailInfo(ctx.getID());
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
			GetPilotBoard pbdao = new GetPilotBoard(con);
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("pilotLocation", pbdao.getLocation(p.getID()), REQUEST);
			ctx.setAttribute("homeAirport", SystemData.getAirport(p.getHomeAirport()), REQUEST);

			// Get all equipment type profiles
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);

			// Get all equipment types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);

			// Get Pilot Examinations
			GetExam exdao = new GetExam(con);
			ctx.setAttribute("exams", exdao.getExams(p.getID()), REQUEST);
			
			// Load if signature validated
			if (ctx.isUserInRole("HR") || ctx.isUserInRole("Signature")) {
				GetImage imgdao = new GetImage(con);
				ctx.setAttribute("sigAuthorized", Boolean.valueOf(imgdao.isSignatureAuthorized(p.getID())), REQUEST);
			}

			// Get status updates
			GetStatusUpdate updao = new GetStatusUpdate(con);
			Collection<StatusUpdate> upds = updao.getByUser(p.getID(), SystemData.get("airline.db"));
			ctx.setAttribute("statusUpdates", upds, REQUEST);
			
			// Get Author IDs from Status Updates
			Collection<Integer> IDs = upds.stream().map(StatusUpdate::getAuthorID).collect(Collectors.toSet());
			
			// Load authors
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("authors", dao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
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
	@Override
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
			if (crossDB && !ctx.isUserInRole("HR"))
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());

			// Get the DAO and load the pilot profile
			GetPilot dao = new GetPilot(con);
			Pilot p = dao.get(usrInfo);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Get the access controller
			PilotAccessControl access = crossDB ? new CrossAppPilotAccessControl(ctx, p) : new PilotAccessControl(ctx, p);
			access.validate();
			if (!access.getCanView())
				throw forgottenException();

			// Check if we can view examinations
			if (access.getCanViewExams()) {
				GetExam exdao = new GetExam(con);
				List<Test> exams = new ArrayList<Test>();
				for (Integer id : usrInfo.getIDs()) {
					int dbID = id.intValue();
					Collection<Test> aExams = exdao.getExams(dbID);
					if (dbID != p.getID()) {
						 for (Iterator<Test> i = aExams.iterator(); i.hasNext(); ) {
							 Test t = i.next();
							 if (!t.getAcademy())
								 i.remove();
						 }
					}

					exams.addAll(aExams);
				}
				
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
				Collections.sort(exams, new TestComparator(TestComparator.DATE));
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
			GetAcademyCourses fadao = new GetAcademyCourses(con);
			Collection<Course> certs = new ArrayList<Course>();
			Collection<Course> courses = new ArrayList<Course>();
			for (Integer id : usrInfo.getIDs()) {
				int dbID = id.intValue();
				certs.addAll(fadao.getCompleted(dbID, "C.STARTDATE"));
				courses.addAll(fadao.getByPilot(dbID));
			}
			
			ctx.setAttribute("certs", certs, REQUEST);
			ctx.setAttribute("courses", courses, REQUEST);
			
			// Get Accomplishments
			GetAccomplishment acdao = new GetAccomplishment(con);
			ctx.setAttribute("accs", acdao.getByPilot(p, SystemData.get("airline.db")), REQUEST);
			
			// Load instructor IDs
			Collection<Integer> IDs = courses.stream().map(Course::getInstructorID).collect(Collectors.toSet());

			// Get status updates
			GetStatusUpdate updao = new GetStatusUpdate(con);
			Collection<StatusUpdate> upds = updao.getByUser(p.getID(), usrInfo.getDB());
			ctx.setAttribute("statusUpdates", upds, REQUEST);
			
			// Get Author IDs from Status Updates
			IDs.addAll(upds.stream().map(StatusUpdate::getAuthorID).collect(Collectors.toSet()));
			
			// Load authors
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("authors", dao.get(udm), REQUEST);
			
			// Load VATSIM certs
			if (p.hasNetworkID(OnlineNetwork.VATSIM)) {
				GetATOData atodao = new GetATOData();
				ctx.setAttribute("vatsim_ratings", atodao.get(p.getNetworkID(OnlineNetwork.VATSIM)), REQUEST);
			}
			
			// Load ACARS client data
			if (ctx.isUserInRole("Operations") || ctx.isUserInRole("Developer")) {
				GetSystemInfo sysdao = new GetSystemInfo(con);
				ctx.setAttribute("acarsClientInfo", sysdao.get(p.getID()), REQUEST);
			}
			
			// Get the online totals
			if (p.getACARSLegs() < 0) {
				GetFlightReports prdao = new GetFlightReports(con);
				prdao.getOnlineTotals(p, usrInfo.getDB());
			}
			
			// If we're a moderator, get the water cooler post stats
			if (ctx.isUserInRole("HR") || ctx.isUserInRole("Moderator")) {
				GetStatistics stdao = new GetStatistics(con);
				Map<Integer, Long> wcStats = stdao.getCoolerStatistics(Collections.singleton(Integer.valueOf(p.getID())));
				if (!wcStats.isEmpty())
					ctx.setAttribute("wcPosts", wcStats.get(Integer.valueOf(p.getID())), REQUEST);
			}

			// Get email delivery data
			if (ctx.isUserInRole("Developer") || ctx.isUserInRole("Operations") || ctx.isUserInRole("HR")) {
				GetEMailDelivery eddao = new GetEMailDelivery(con);
				ctx.setAttribute("emailDelivery", eddao.getByPilot(p.getID()), REQUEST);
			}
			
			// Load if signature validated
			if (ctx.isUserInRole("HR") || ctx.isUserInRole("Signature")) {
				GetImage imgdao = new GetImage(con);
				ctx.setAttribute("sigAuthorized", Boolean.valueOf(imgdao.isSignatureAuthorized(p.getID())), REQUEST);
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