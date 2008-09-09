// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mail;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.EMailConfiguration;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle IMAP mailbox profiles. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class IMAPMailboxCommand extends AbstractFormCommand {

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Load the e-mail configuration
			GetPilotEMail idao = new GetPilotEMail(con);
			EMailConfiguration cfg = idao.getEMailInfo(p.getID());
			if (cfg == null) {
				cfg = new EMailConfiguration(p.getID(), "");
				cfg.setMailDirectory(String.valueOf(p.getID()) + "/");
			}
			
			// Save in the request
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("mb", cfg, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/imapEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		boolean isNew = false;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(ctx.getID());
			if (usr == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Load the e-mail configuration
			GetPilotEMail idao = new GetPilotEMail(con);
			EMailConfiguration cfg = idao.getEMailInfo(usr.getID());
			isNew = (cfg == null);
			if (cfg == null)
				cfg = new EMailConfiguration(usr.getID(), ctx.getParameter("IMAPAddr"));
			else
				cfg.setAddress(ctx.getParameter("IMAPAddr"));
			
			// Update from the fields
			cfg.setMailDirectory(ctx.getParameter("IMAPPath"));
			cfg.setQuota(StringUtils.parse(ctx.getParameter("IMAPQuota"), 0));
			cfg.setActive(Boolean.valueOf(ctx.getParameter("IMAPActive")).booleanValue());
			cfg.setAliases(StringUtils.split(ctx.getParameter("IMAPAliases"), ","));
			
			// Start a transaction
			ctx.startTX();
			
            // Generate the mailbox directory
			if (isNew) {
				ProcessBuilder pBuilder = new ProcessBuilder(SystemData.get("smtp.imap.script"), cfg.getMailDirectory(), SystemData.get("smtp.imap.path"));
				pBuilder.redirectErrorStream(true);
            
				try {
					Process p = pBuilder.start();
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            	
					// Wait for the process to complete
					int runTime = 0;
					while (runTime < 3500) {
						ThreadUtils.sleep(200);
						try {
							if (p.exitValue() != 1)
								throw new DAOException("Unable to create mailbox - error " + p.exitValue());
							
							break;
						} catch (IllegalThreadStateException itse) {
							runTime += 200;            			
						}
					}
            	
					// Get the stdout results
					Collection<String> pOut = new ArrayList<String>();
					while (br.ready())
						pOut.add(br.readLine());
            	
					ctx.setAttribute("scriptResults", pOut, REQUEST);
				} catch (IOException ie) {
					throw new DAOException(ie);
				}
			}
			
			// Save the profile
			SetPilotEMail ewdao = new SetPilotEMail(con);
			if (isNew) {
				ewdao.write(cfg, usr.getName());
				ewdao.updatePassword(cfg.getID(), SystemData.get("smtp.imap.default_pwd"));
			} else
				ewdao.update(cfg, usr.getName());
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setSuccess(true);	
		if (isNew) {
			result.setType(CommandResult.REQREDIRECT);
			result.setURL("/jsp/admin/imapCreated.jsp");
		} else {
			result.setType(CommandResult.REDIRECT);
			result.setURL("imaplist.do");
		}
	}
}