// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.testing.ExamProfile;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamProfileAccessControl;

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

      // Check our access level
      ExamProfileAccessControl access = new ExamProfileAccessControl(ctx);
      access.validate();
      if (!access.getCanEdit()) throw new CommandSecurityException("Cannot edit Examination Profile");

      String examName = (String) ctx.getCmdParameter(Command.ID, null);
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and load the existing exam profile, or create a new one
         ExamProfile ep = null;
         if (examName != null) {
            GetExamProfiles rdao = new GetExamProfiles(con);
            ep = rdao.getExamProfile(examName);
            if (ep == null) throw new CommandException("Examination " + examName + " not found");
         } else {
            ep = new ExamProfile(ctx.getParameter("examName"));
         }

         // Load the fields from the request
         ep.setEquipmentType("N/A".equals(ctx.getParameter("eqType")) ? null : ctx.getParameter("eqType"));
         ep.setStage(Integer.parseInt(ctx.getParameter("stage")));
         ep.setMinStage(Integer.parseInt(ctx.getParameter("minStage")));
         ep.setSize(Integer.parseInt(ctx.getParameter("size")));
         ep.setPassScore(Integer.parseInt(ctx.getParameter("passScore")));
         ep.setTime(Integer.parseInt(ctx.getParameter("time")));
         ep.setActive("1".equals(ctx.getParameter("active")));

         // Get the write DAO and save the profile
         SetExamProfile wdao = new SetExamProfile(con);
         if (examName == null) {
            wdao.create(ep);
         } else {
            wdao.update(ep);
         }
         
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

      // Check our access level
      ExamProfileAccessControl access = new ExamProfileAccessControl(ctx);
      access.validate();
      if (!access.getCanEdit()) throw new CommandSecurityException("Cannot edit Examination Profile");

      String examName = (String) ctx.getCmdParameter(Command.ID, null);
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the exam profile
         GetExamProfiles dao = new GetExamProfiles(con);
         if (examName != null) {
            ExamProfile ep = dao.getExamProfile(examName);
            if (ep == null) throw new CommandException("Invalid Exam Name - " + examName);

            // Save the profile in the request
            ctx.setAttribute("eProfile", ep, REQUEST);
         }

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

      // Check our access level
      ExamProfileAccessControl access = new ExamProfileAccessControl(ctx);
      access.validate();
      if (!access.getCanRead())
         throw new CommandSecurityException("Cannot view Examination Profile");

      String examName = (String) ctx.getCmdParameter(Command.ID, null);
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the exam profile
         GetExamProfiles dao = new GetExamProfiles(con);
         ExamProfile ep = dao.getExamProfile(examName);
         if (ep == null) throw new CommandException("Invalid Exam Name - " + examName);

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