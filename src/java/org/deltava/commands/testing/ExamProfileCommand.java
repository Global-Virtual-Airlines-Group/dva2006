// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.ExamProfile;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamProfileAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to support the modification of Examination Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamProfileCommand extends AbstractFormCommand {

   /**
    * Callback method called when saving the Examination Profile.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   protected void execSave(CommandContext ctx) throws CommandException {

      String examName = (String) ctx.getCmdParameter(Command.ID, null);
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and load the existing exam profile, or create a new one
         ExamProfile ep = null;
         if (examName != null) {
            GetExamProfiles rdao = new GetExamProfiles(con);
            ep = rdao.getExamProfile(examName);
            if (ep == null)
            	throw notFoundException("Examination " + examName + " not found");
            
            // Update the exam name
            ep.setName(ctx.getParameter("examName"));
         } else
            ep = new ExamProfile(ctx.getParameter("examName"));
         
         // Check our access level
         ExamProfileAccessControl access = new ExamProfileAccessControl(ctx, ep);
         access.validate();
         if (!access.getCanEdit())
        	 throw securityException("Cannot edit Examination Profile");

         // Load the fields from the request
         ep.setEquipmentType("N/A".equals(ctx.getParameter("eqType")) ? null : ctx.getParameter("eqType"));
         ep.setStage(StringUtils.parse(ctx.getParameter("stage"), 1));
         ep.setMinStage(StringUtils.parse(ctx.getParameter("minStage"), 1));
         ep.setSize(StringUtils.parse(ctx.getParameter("size"), 1));
         ep.setPassScore(StringUtils.parse(ctx.getParameter("passScore"), 1));
         ep.setTime(StringUtils.parse(ctx.getParameter("time"), 15));
         ep.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
       	 ep.setAcademy(Boolean.valueOf(ctx.getParameter("isAcademy")).booleanValue());
       	 ep.setOwner(SystemData.getApp(ctx.getParameter("owner")));
       	 Collection<String> airlines = ctx.getParameters("airlines");
       	 if (airlines != null) {
       		 Collection<AirlineInformation> ai = new ArrayList<AirlineInformation>();
       		 for (Iterator<String> i = airlines.iterator(); i.hasNext(); )
       			 ai.add(SystemData.getApp(i.next()));
       		 
       		 ep.setAirlines(ai);
       	 }

         // Get the write DAO and save the profile
         SetExamProfile wdao = new SetExamProfile(con);
         if (examName == null)
            wdao.create(ep);
         else
            wdao.update(ep, examName);
         
         // Save the exam profile
         ctx.setAttribute("exam", ep, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/testing/profileUpdate.jsp");
      result.setSuccess(true);
   }

   /**
    * Callback method called when editing the Examination Profile.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   protected void execEdit(CommandContext ctx) throws CommandException {

      String examName = (String) ctx.getCmdParameter(Command.ID, null);
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the exam profile
         GetExamProfiles dao = new GetExamProfiles(con);
         ExamProfile ep = null;
         if (examName != null) {
            ep = dao.getExamProfile(examName);
            if (ep == null)
            	throw notFoundException("Invalid Exam Name - " + examName);
            
            // Save the profile in the request
            ctx.setAttribute("eProfile", ep, REQUEST);
         }
         
         // Check our access level
         ExamProfileAccessControl access = new ExamProfileAccessControl(ctx, ep);
         access.validate();
         if (!access.getCanEdit())
         	throw securityException("Cannot edit Examination Profile");

         // Get Equipment Type programs
         GetEquipmentType eqdao = new GetEquipmentType(con);
         ctx.setAttribute("eqTypes", eqdao.getAll(), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/testing/examProfileEdit.jsp");
      result.setSuccess(true);
   }

   /**
    * Callback method called when reading the Examination profile.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   protected void execRead(CommandContext ctx) throws CommandException {

      String examName = (String) ctx.getCmdParameter(Command.ID, null);
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the exam profile
         GetExamProfiles dao = new GetExamProfiles(con);
         ExamProfile ep = dao.getExamProfile(examName);
         if (ep == null)
        	 throw notFoundException("Invalid Examination Name - " + examName);
         
         // Check our access level
         ExamProfileAccessControl access = new ExamProfileAccessControl(ctx, ep);
         access.validate();
         if (!access.getCanRead())
            throw securityException("Cannot view Examination Profile");

         // Save the profile in the request
         ctx.setAttribute("eProfile", ep, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/testing/examProfile.jsp");
      result.setSuccess(true);
   }
}