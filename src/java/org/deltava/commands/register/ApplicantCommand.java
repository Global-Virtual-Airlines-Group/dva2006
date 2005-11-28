// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ApplicantAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command for processing Applicant Profiles.
 * @author Luke
 * @version 1.0
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
      boolean doHire = "1".equals(ctx.getParameter("doHire"));
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the Applicant
         GetApplicant dao = new GetApplicant(con);
         Applicant a = dao.get(ctx.getID());
         if (a == null)
            throw new CommandException("Invalid Applicant - " + ctx.getID());

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
         a.setEmail(ctx.getParameter("eMail"));
         a.setLocation(ctx.getParameter("location"));
         a.setIMHandle(ctx.getParameter("imHandle"));
         a.setNetworkID("VATSIM", ctx.getParameter("VATSIM_ID"));
         a.setNetworkID("IVAO", ctx.getParameter("IVAO_ID"));
         a.setLegacyURL(ctx.getParameter("legacyURL"));
         a.setLegacyVerified("1".equals(ctx.getParameter("legacyOK")));
         a.setHomeAirport(ctx.getParameter("homeAirport"));
         a.setEmailAccess(Person.AUTH_EMAIL);
         a.setDateFormat(ctx.getParameter("df"));
         a.setTimeFormat(ctx.getParameter("tf"));
         a.setNumberFormat(ctx.getParameter("nf"));
         a.setAirportCodeType(ctx.getParameter("airportCodeType"));
         a.setTZ(TZInfo.get(ctx.getParameter("tz")));
         a.setUIScheme(ctx.getParameter("uiScheme"));
         
         // Save hire fields
         a.setEquipmentType(ctx.getParameter("eqType"));
         a.setRank(ctx.getParameter("rank"));

         // Parse legacy hours
         try {
            a.setLegacyHours(Double.parseDouble(ctx.getParameter("legacyHours")));
         } catch (NumberFormatException nfe) { }
         
         // Set Notification Options
         Collection<String> notifyOptions = CollectionUtils.loadList(ctx.getRequest().getParameterValues("notifyOption"), 
        		 new HashSet<String>());
         for (int x = 0; x < RegisterCommand.NOTIFY_ALIASES.length; x++)
            a.setNotifyOption(RegisterCommand.NOTIFY_ALIASES[x], notifyOptions.contains(RegisterCommand.NOTIFY_ALIASES[x]));

         // Save the applicant in the request
         ctx.setAttribute("applicant", a, REQUEST);

         // Get the Pilot DAO and check if we're unique
         GetPilotDirectory pdao = new GetPilotDirectory(con);
         Set<Integer> dupeResults = new HashSet<Integer>(pdao.checkUnique(a, SystemData.get("airline.db")));
         if (!dupeResults.isEmpty())
            throw new CommandException("Applicant name/email not unique");
         
         // Check if we're unique
         dupeResults.addAll(dao.checkUnique(a, SystemData.get("airline.db")));
         if (dupeResults.size() != 1)
            throw new CommandException("Applicant name/email not unique");

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
         result.setType(CommandResult.REDIRECT);
         result.setURL("apphire", null, ctx.getID());
      } else {
         result.setType(CommandResult.REQREDIRECT);
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
      ctx.setAttribute("acTypes", ComboUtils.fromArray(Airport.CODETYPES), REQUEST);
      ctx.setAttribute("timeZones", TZInfo.getAll(), REQUEST);
      ctx.setAttribute("notifyOptions", ComboUtils.fromArray(RegisterCommand.NOTIFY_NAMES, 
            RegisterCommand.NOTIFY_ALIASES), REQUEST);

		// Sort and save the airports
		Map<String, Airport> airports = SystemData.getAirports();
		Set<Airport> apSet = new TreeSet<Airport>(new AirportComparator<Airport>(AirportComparator.NAME));
		apSet.addAll(airports.values());
		ctx.setAttribute("airports", apSet, REQUEST);
      
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the Applicant
         GetApplicant dao = new GetApplicant(con);
         Applicant a = dao.get(ctx.getID());
         if (a == null)
            throw new CommandException("Invalid Applicant - " + ctx.getID());

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
            throw new CommandException("Invalid Applicant - " + ctx.getID());

         // Check our access level
         ApplicantAccessControl access = new ApplicantAccessControl(ctx, a);
         access.validate();
         if (!access.getCanRead())
            throw securityException("Cannot view Applicant");
         
         // Check if the address has been validated
         GetAddressValidation avdao = new GetAddressValidation(con);
         ctx.setAttribute("eMailValid", Boolean.valueOf(avdao.isValid(a.getID())), REQUEST);
         
         // Do a soundex check on the user
         soundexCheck(a, con, ctx);
         
         // Get the questionnaire
         GetQuestionnaire exdao = new GetQuestionnaire(con);
         ctx.setAttribute("questionnaire", exdao.getByApplicantID(a.getID()), REQUEST);
         
         // Get Active Equipment programs
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
    * Helper method to perform the soundex check.
    */
   private void soundexCheck(Applicant a, Connection c, CommandContext ctx) throws DAOException {
      
      // Initialize the DAOs
      GetApplicant dao = new GetApplicant(c);
      GetPilotDirectory pdao = new GetPilotDirectory(c);
      
      // Do a soundex check on the applicant against each database
      Collection<Integer> soundexIDs = new HashSet<Integer>();
      Collection airlines = ((Map) SystemData.getObject("apps")).values();
      for (Iterator i = airlines.iterator(); i.hasNext(); ) {
         AirlineInformation info = (AirlineInformation) i.next();
         soundexIDs.addAll(dao.checkSoundex(a, info.getDB()));
         soundexIDs.addAll(pdao.checkSoundex(a, info.getDB()));
      }
      
      // If nothing found, stop
      if (soundexIDs.isEmpty())
         return;

      // Load the locations of all these matches
      GetUserData uddao = new GetUserData(c);
      UserDataMap udmap = uddao.get(soundexIDs);
      
      // Load the users objects
      Map<Integer, Person> persons = new HashMap<Integer, Person>();
      for (Iterator<String> i = udmap.getTableNames().iterator(); i.hasNext(); ) {
         String tableName = i.next();
         Collection IDs = udmap.getByTable(tableName);
         if (UserDataMap.isPilotTable(tableName)) {
            persons.putAll(pdao.getByID(IDs, tableName));
         } else {
            persons.putAll(dao.getByID(IDs, tableName));
         }
      }
      
      // Filter out applicants where the pilot already matches
      for (Iterator i = persons.values().iterator(); i.hasNext(); ) {
    	  Person p = (Person) i.next();
    	  if (p instanceof Applicant) {
    		  Applicant ap = (Applicant) p;
    		  if (persons.keySet().contains(new Integer(ap.getPilotID())))
    			  i.remove();
    	  }
      }
      
      // Save the userdata map and persons in the request
      ctx.setAttribute("userData", udmap, REQUEST);
      ctx.setAttribute("soundexUsers", persons.values(), REQUEST);
   }
}