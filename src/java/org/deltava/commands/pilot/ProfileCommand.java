// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.system.*;

import org.deltava.comparators.RankComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.*;
import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle editing/saving Pilot Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ProfileCommand extends AbstractFormCommand {

   private static final Logger log = Logger.getLogger(ProfileCommand.class);

   private static final String[] PRIVACY_ALIASES = { "0", "1", "2" };
   private static final String[] PRIVACY_NAMES = { "Show address to Staff Members only",
         "Show address to Authenticated Users", "Show address to All Visitors" };

   private static final String[] NOTIFY_ALIASES = { Person.NEWS, Person.EVENT, Person.FLEET, Person.COOLER };
   private static final String[] NOTIFY_NAMES = { "Send News Notifications", "Send Event Notifications",
         "Send Fleet Notifications", "Send Water Cooler Notifications" };

   /**
    * Callback method called when saving the profile.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   protected void execSave(CommandContext ctx) throws CommandException {
      try {
         Connection con = ctx.getConnection();
         List updates = new ArrayList();

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
         p.setNetworkID("VATSIM", ctx.getParameter("VATSIM_ID"));
         p.setNetworkID("IVAO", ctx.getParameter("IVAO_ID"));
         p.setLocation(ctx.getParameter("location"));
         p.setIMHandle(ctx.getParameter("imHandle"));
         p.setEmailAccess(Integer.parseInt(ctx.getParameter("privacyOption")));
         p.setTZ(TZInfo.get(ctx.getParameter("tz")));
         p.setDateFormat(ctx.getParameter("df"));
         p.setTimeFormat(ctx.getParameter("tf"));
         p.setNumberFormat(ctx.getParameter("nf"));
         p.setAirportCodeType(ctx.getParameter("airportCodeType"));
         p.setMapType(ctx.getParameter("mapType"));
         p.setUIScheme(ctx.getParameter("uiScheme"));

         // Get Water Cooler option checkboxes
         p.setShowSignatures(Boolean.valueOf(ctx.getParameter("showSigs")).booleanValue());
         p.setShowSSThreads(Boolean.valueOf(ctx.getParameter("showImageThreads")).booleanValue());
         p.setHasDefaultSignature(Boolean.valueOf(ctx.getParameter("useDefaultSig")).booleanValue());

         // Set Notification Options
         String[] notifyOpts = ctx.getRequest().getParameterValues("notifyOption");
         if (notifyOpts != null) {
            List notifyOptions = Arrays.asList(notifyOpts);
            for (int x = 0; x < NOTIFY_ALIASES.length; x++)
               p.setNotifyOption(NOTIFY_ALIASES[x], notifyOptions.contains(NOTIFY_ALIASES[x]));
         } else {
            for (int x = 0; x < NOTIFY_ALIASES.length; x++)
               p.setNotifyOption(NOTIFY_ALIASES[x], false);
         }

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
               log.info(upd.getDescription());
            }
         }
         
         // Update legacy hours
         if (p_access.getCanChangeStatus() && (ctx.getParameter("legacyHours") != null))
        	 p.setLegacyHours(Double.parseDouble(ctx.getParameter("legacyHours")));

         // Load the ratings from the request and convert to a set to maintain uniqueness
         Set newRatings = new HashSet(CollectionUtils.loadList(ctx.getRequest().getParameterValues("ratings"), p
               .getRatings()));

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
                  throw new CommandException("Unknown Equipment Type program - " + newEQ);

               // Figure out if this is truly a promotion
               RankComparator rcmp = new RankComparator((List) SystemData.getObject("ranks"));
               rcmp.setRank2(p.getRank(), eq1.getStage());
               rcmp.setRank1(newRank, eq2.getStage());

               // Update the rank/equipment program
               p.setRank(newRank);
               p.setEquipmentType(newEQ);

               // Load the ratings from the new equipment type
               newRatings.addAll(eq2.getPrimaryRatings());
               newRatings.addAll(eq2.getSecondaryRatings());

               // Write the status update
               if (rcmp.compare() > 0) {
                  int promoType = eqChange ? StatusUpdate.EXTPROMOTION : StatusUpdate.INTPROMOTION;
                  StatusUpdate upd = new StatusUpdate(p.getID(), promoType);
                  upd.setAuthorID(ctx.getUser().getID());
                  upd.setDescription("Promoted to " + newRank + ", " + newEQ);
                  updates.add(upd);
               } else {
                  StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.RANK_CHANGE);
                  upd.setAuthorID(ctx.getUser().getID());
                  upd.setDescription("Rank Changed to " + newRank + ", " + newEQ);
                  updates.add(upd);
               }
            }
         }

         // Update the Pilot's equipment type ratings
         if ((p_access.getCanPromote()) && CollectionUtils.hasDelta(newRatings, p.getRatings())) {
            // Figure out what ratings have been added
            Collection addedRatings = CollectionUtils.getDelta(newRatings, p.getRatings());
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
            Collection removedRatings = CollectionUtils.getDelta(p.getRatings(), newRatings);
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
         Collection newRoles = p_access.getCanChangeRoles() ? CollectionUtils.loadList(roles, Collections.EMPTY_SET)
               : p.getRoles();
         newRoles.add("Pilot");

         // Update the Pilot's Security Roles
         if ((p_access.getCanChangeRoles()) && CollectionUtils.hasDelta(newRoles, p.getRoles())) {
            // Figure out what roles have been added
            Collection addedRoles = CollectionUtils.getDelta(newRoles, p.getRoles());
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
            Collection removedRoles = CollectionUtils.getDelta(p.getRoles(), newRoles);
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
               sigdao.write(p);
               ctx.setAttribute("sigUpdated", Boolean.TRUE, REQUEST);
               log.info("Signature Updated");
            }
         } else if (Boolean.valueOf(ctx.getParameter("removeCoolerImg")).booleanValue()) {
            sigdao.delete(p.getID());
            ctx.setAttribute("sigRemoved", Boolean.TRUE, REQUEST);
            log.info("Signature Removed");
         }
         
         // TODO Update the e-mail configuration if necessary
         boolean isDelete = Boolean.valueOf(ctx.getParameter("IMAPDelete")).booleanValue();
         if (isDelete && m_access.getCanDelete()) {
            SetPilotEMail ewdao = new SetPilotEMail(con);
            ewdao.delete(p.getID());
            ctx.setAttribute("imapDelete", Boolean.TRUE, REQUEST);
         } else if (m_access.getCanChangePassword()) {
             emailCfg.setPassword(ctx.getParameter("IMAPPassword"));
             
             // Update the configuration
             if (m_access.getCanEdit()) {
                emailCfg.setAddress(ctx.getParameter("IMAPAddr"));
                emailCfg.setMailDirectory(ctx.getParameter("IMAPPath"));
                emailCfg.setQuota(Integer.parseInt(ctx.getParameter("IMAPQuota")));
                emailCfg.setActive(Boolean.valueOf(ctx.getParameter("IMAPActive")).booleanValue());
                emailCfg.setAliases(StringUtils.split(ctx.getParameter("IMAPAliases"), ","));
             }
             
             // Save the profile
             SetPilotEMail ewdao = new SetPilotEMail(con);
             ewdao.write(emailCfg, p.getName());
         }

         // If we have access to the Staff profile, update it
         if (s_access.getCanEdit()) {
            s.setBody(ctx.getParameter("staffBody"));

            // Load special parameters
            boolean removeStaffProfile = false;
            if (p_access.getCanChangeRoles()) {
               s.setTitle(ctx.getParameter("staffTitle"));
               s.setSortOrder(Integer.parseInt(ctx.getParameter("staffSort")));
               removeStaffProfile = ("1".equals(ctx.getParameter("removeStaff")));
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

         // Check if we're updating the e-mail address
         String newEMail = ctx.getParameter("email");
         boolean isEMailUpdate = !p.getEmail().equals(newEMail);
         if (isEMailUpdate && ctx.isUserInRole("HR")) {
            p.setEmail(newEMail);
         } else if (isEMailUpdate) {
            AddressValidation av = new AddressValidation(p.getID(), newEMail);
            AddressValidationHelper.calculateHashCode(av);
            ctx.setAttribute("addrValid", av, REQUEST);

            // Initialize the message context
            MessageContext mctxt = new MessageContext();
            mctxt.addData("addrValid", av);
            mctxt.addData("user", p);

            // Send the validation code via e-mail
            GetMessageTemplate mtdao = new GetMessageTemplate(con);
            mctxt.setTemplate(mtdao.get("EMAILUPDATE"));

            // Send the message
            Mailer mailer = new Mailer(null);
            mailer.setContext(mctxt);
            mailer.send(Mailer.makeAddress(av.getAddress(), p.getName()));

            // Set the session flag
            if (p.getID() == ctx.getUser().getID())
               ctx.setAttribute(CommandContext.ADDRINVALID_ATTR_NAME, Boolean.TRUE, SESSION);

            // Save the address validation entry
            SetAddressValidation avwdao = new SetAddressValidation(con);
            avwdao.write(av);
         }

         // Update the pilot name
         boolean nameChanged = (!p.getFirstName().equals(ctx.getParameter("firstName")))
               || (!p.getLastName().equals(ctx.getParameter("lastName")));
         if (p_access.getCanChangeRoles() && nameChanged) {
            Pilot p2 = p.cloneExceptID();
            p2.setFirstName(ctx.getParameter("firstName"));
            p2.setLastName(ctx.getParameter("lastName"));

            // Get the databases
            GetUserData uddao = new GetUserData(con);
            Collection airlines = uddao.getAirlines(true).values();

            // Get the Applicant Read DAO and search for our applicant record
            GetApplicant adao = new GetApplicant(con);
            Applicant a = adao.getByPilotID(p.getID());

            // Check for unique name
            boolean isUnique = true;
            for (Iterator i = airlines.iterator(); isUnique && i.hasNext();) {
               AirlineInformation info = (AirlineInformation) i.next();

               // Check Pilots & applicants
               Set dupeResults = new HashSet(rdao.checkUnique(p2, info.getDB()));
               dupeResults.addAll(adao.checkUnique(p2, info.getDB()));
               
               // Remove our entry, or that of our applicant entry
               dupeResults.remove(new Integer(p.getID()));
               dupeResults.remove(new Integer(a.getID()));
               isUnique = dupeResults.isEmpty();
            }

            // If we're unique, continue the update
            if (isUnique) {
               String newDN = "cn=" + p2.getName() + "," + SystemData.get("security.baseDN");

               // Create the status update
               StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
               upd.setAuthorID(ctx.getUser().getID());
               upd.setDescription("Renamed to " + p.getName());
               updates.add(upd);

               // Rename the user in the Directory if it's not just a case-sensitivity issue
               Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
               auth.rename(p.getDN(), newDN);
               p.setDN(newDN);
               p.setFirstName(p2.getFirstName());
               p.setLastName(p2.getLastName());

               // Set status attribute
               ctx.setAttribute("newName", p.getName(), REQUEST);
            } else {
               ctx.setAttribute("renameNotUnique", Boolean.TRUE, REQUEST);
            }
         }

         // Write the Pilot profile
         SetPilot pwdao = new SetPilot(con);
         pwdao.write(p);

         // If we're marking Inactive/Retired, purge any Inactivity records
         if ((p.getStatus() != Pilot.ACTIVE) && (p.getStatus() != Pilot.ON_LEAVE)) {
            SetInactivity idao = new SetInactivity(con);
            idao.delete(p.getID());
         }

         // Write the status updates
         SetStatusUpdate stwdao = new SetStatusUpdate(con);
         stwdao.write(updates);

         // If we're updating the password, then save it
         if (!StringUtils.isEmpty(ctx.getParameter("pwd1"))) {
            Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
            auth.updatePassword(p.getDN(), ctx.getParameter("pwd1"));
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
      ctx.setAttribute("notifyOptions", ComboUtils.fromArray(NOTIFY_NAMES, NOTIFY_ALIASES), REQUEST);
      ctx.setAttribute("privacyOptions", ComboUtils.fromArray(PRIVACY_NAMES, PRIVACY_ALIASES), REQUEST);
      ctx.setAttribute("acTypes", ComboUtils.fromArray(Airport.CODETYPES), REQUEST);
      ctx.setAttribute("statuses", ComboUtils.fromArray(Pilot.STATUS), REQUEST);
      ctx.setAttribute("mapTypes", ComboUtils.fromArray(Pilot.MAP_TYPES), REQUEST);

      try {
         Connection con = ctx.getConnection();

         // Get the DAO and load the pilot/email profile
         GetPilotDirectory dao = new GetPilotDirectory(con);
         GetPilotEMail edao = new GetPilotEMail(con);
         Pilot p = dao.get(ctx.getID());
         EMailConfiguration emailCfg = edao.getEMailInfo(ctx.getID());
         if (emailCfg != null)
        	 ctx.setAttribute("emailCfg", emailCfg, REQUEST);

         // Get the staff profile (if any)
         GetStaff dao2 = new GetStaff(con);
         Staff s = dao2.get(ctx.getID());
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

         // Save pilot status
         ctx.setAttribute("status", Pilot.STATUS[p.getStatus()], REQUEST);

         // Get the Online Hours/Legs if not already loaded
         GetFlightReports dao3 = new GetFlightReports(con);
         dao3.getOnlineTotals(p);

         // Save the pilot profile in the request
         ctx.setAttribute("pilot", p, REQUEST);
         ctx.setAttribute("access", ac, REQUEST);
         ctx.setAttribute("m_access", m_access, REQUEST);

         // Get all equipment type profiles
         GetEquipmentType dao4 = new GetEquipmentType(con);
         ctx.setAttribute("eqTypes", dao4.getActive(), REQUEST);

         // Get Pilot Examinations
         GetExam exdao = new GetExam(con);
         ctx.setAttribute("exams", exdao.getExams(p.getID()), REQUEST);

         // Get status updates
         GetStatusUpdate updao = new GetStatusUpdate(con);
         ctx.setAttribute("statusUpdates", updao.getByUser(p.getID()), REQUEST);
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
   protected void execRead(CommandContext ctx) throws CommandException {

      // Get airport map
      Map airports = (Map) SystemData.getObject("airports");

      try {
         Connection con = ctx.getConnection();

         // Get the DAO and load the pilot profile
         GetPilot dao = new GetPilot(con);
         Pilot p = dao.get(ctx.getID());
         if (p == null)
            throw new CommandException("Invalid Pilot ID - " + ctx.getID());

         // Get the access controller
         PilotAccessControl access = new PilotAccessControl(ctx, p);
         access.validate();

         // Check if we can view examinations
         if (access.getCanPromote() || access.getIsOurs()) {
            GetExam exdao = new GetExam(con);
            ctx.setAttribute("exams", exdao.getExams(p.getID()), REQUEST);
         }
         
         // Check for an applicant profile
         if (ctx.isUserInRole("HR")) {
        	 GetApplicant adao = new GetApplicant(con);
        	 ctx.setAttribute("applicant", adao.getByPilotID(p.getID()), REQUEST);
         }

         // Get status updates
         GetStatusUpdate updao = new GetStatusUpdate(con);
         ctx.setAttribute("statusUpdates", updao.getByUser(p.getID()), REQUEST);

         // Get the online totals
         GetFlightReports dao2 = new GetFlightReports(con);
         dao2.getOnlineTotals(p);

         // Save the pilot profile and ratings in the request
         ctx.setAttribute("pilot", p, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
         ctx.setAttribute("airport", airports.get(p.getHomeAirport()), REQUEST);
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