// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.Authenticator;
import org.deltava.security.command.ApplicantAccessControl;

import org.deltava.util.PasswordGenerator;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to hire new Applicants as Pilots. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ApplicantApproveCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Create the message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		
		Applicant a = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Applicant
			GetApplicant dao = new GetApplicant(con);
			a = dao.get(ctx.getID());
			if (a == null)
				throw new CommandException("Invalid Applicant - " + ctx.getID());
			
			// Get the Equipment Type hired into
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(a.getEquipmentType());
			if (eq == null)
			   throw new CommandException("Invalid Equipment Program - " + a.getEquipmentType());
			
			// Check our access level
			ApplicantAccessControl access = new ApplicantAccessControl(ctx, a);
			access.validate();
			if (!access.getCanApprove())
				throw securityException("Cannot Approve Applicant");
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("APPAPPROVE"));
			mctxt.addData("applicant", a);
			mctxt.addData("eqType", eq);
			
			// Calculate the DN and password
			a.setDN("cn=" + a.getName() + "," + SystemData.get("security.baseDN"));
			a.setPassword(PasswordGenerator.generate(8));
			
			// Turn off autocommits on the connection
			ctx.startTX();
			
			// Get the write DAO and approve the applicant
			SetApplicant wdao = new SetApplicant(con);
			wdao.hire(a);
			
			// Delete the e-mail validation record
			SetAddressValidation avdao = new SetAddressValidation(con);
			avdao.delete(a.getID());
			
			// Get the Questionnaire and convert into an Examination
			GetQuestionnaire qdao = new GetQuestionnaire(con);
			Examination aq =qdao.getByApplicantID(a.getID());
			if (aq != null) {
			   SetQuestionnaire qwdao = new SetQuestionnaire(con);
			   qwdao.convertToExam(aq, a.getPilotID());
			}
			
			// Check a dummy check ride entry for the user to reflect the stage qualification
			CheckRide cr = new CheckRide(a.getEquipmentType() + " Initial Hire");
			cr.setDate(new Date());
			cr.setPilotID(a.getPilotID());
			cr.setPassFail(true);
			cr.setComments(a.getName() + " hired into " + eq.getName() + " program");
			cr.setScorerID(ctx.getUser().getID());
			cr.setStage(eq.getStage());
			cr.setStatus(Test.SCORED);
			cr.setSubmittedOn(new Date());
			cr.setScoredOn(cr.getSubmittedOn());
			
			// Write the check ride (call the DAO twice to write all fields)
			SetExam exwdao = new SetExam(con);
			exwdao.write(cr);
			exwdao.write(cr);
			
			// Write Status update history
			List updates = new ArrayList();
			
			// Create a StatusUpdate for the registration
			StatusUpdate upd = new StatusUpdate(a.getPilotID(), StatusUpdate.STATUS_CHANGE);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setCreatedOn(a.getCreatedOn());
			upd.setDescription("Pilot Application Submitted");
			updates.add(upd);
			
			// Create a StatusUpdate for the hire
			StatusUpdate upd2 = new StatusUpdate(a.getPilotID(), StatusUpdate.STATUS_CHANGE);
			upd2.setAuthorID(ctx.getUser().getID());
			upd2.setDescription("Applicant Approved, Pilot Hired");
			updates.add(upd2);
			
			// Write the status updates
			SetStatusUpdate updao = new SetStatusUpdate(con);
			updao.write(updates);
			
			// Write an inactivity purge entry
			SetInactivity idao = new SetInactivity(con);
			idao.setInactivity(a.getPilotID(), SystemData.getInt("users.inactive_new_days"), false);
			
			// Get the authenticator and add the user
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			auth.addUser(a.getDN(), a.getPassword());
			
			// Commit the transactions
			ctx.commitTX();
			
			// Save the applicant in the request
			ctx.setAttribute("applicant", a, REQUEST);
		} catch (SecurityException se) {
		   ctx.rollbackTX();
		   throw new CommandException(se);
		} catch (DAOException de) {
		   ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send e-mail notification
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(a);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/register/applicantApprove.jsp");
		result.setSuccess(true);
	}
}