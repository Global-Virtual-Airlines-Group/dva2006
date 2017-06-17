// Copyright 2008, 2010, 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mail;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.IMAPConfiguration;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle IMAP mailbox profiles.
 * @author Luke
 * @version 7.4
 * @since 2.2
 */

public class IMAPMailboxCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
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
			IMAPConfiguration cfg = idao.getEMailInfo(p.getID());
			if (cfg == null) {
				cfg = new IMAPConfiguration(p.getID(), "");
				cfg.setMailDirectory(String.valueOf(p.getID()) + "/");
				cfg.setAllowSMTP(true);
				cfg.setActive(true);
			}

			// Save in the request
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("mb", cfg, REQUEST);
			readAuditLog(ctx, cfg);
			ctx.setAttribute("aliases", StringUtils.listConcat(cfg.getAliases(), "\n"), REQUEST);
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
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
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
			IMAPConfiguration cfg = idao.getEMailInfo(usr.getID());
			isNew = (cfg == null); IMAPConfiguration ocfg = BeanUtils.clone(cfg);
			if (cfg == null)
				cfg = new IMAPConfiguration(usr.getID(), ctx.getParameter("IMAPAddr"));
			else
				cfg.setAddress(ctx.getParameter("IMAPAddr"));

			// Update from the fields
			cfg.setMailDirectory(ctx.getParameter("IMAPPath"));
			cfg.setQuota(StringUtils.parse(ctx.getParameter("IMAPQuota"), 0));
			cfg.setActive(Boolean.valueOf(ctx.getParameter("IMAPActive")).booleanValue());
			cfg.setAllowSMTP(Boolean.valueOf(ctx.getParameter("IMAPAllowSMTP")).booleanValue());
			cfg.setAliases(StringUtils.split(ctx.getParameter("IMAPAliases"), "\n"));
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(ocfg, cfg);
			AuditLog ae = AuditLog.create(cfg, delta, ctx.getUser().getID());

			// Start a transaction
			ctx.startTX();

			// Generate the mailbox directory
			if (isNew) {
				ProcessBuilder pBuilder = new ProcessBuilder(SystemData.get("smtp.imap.script"), cfg.getMailDirectory(), SystemData.get("smtp.imap.path"));
				pBuilder.redirectErrorStream(true);

				try {
					Process p = pBuilder.start();
					Collection<String> pOut = new ArrayList<String>();
					try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
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
						while (br.ready())
							pOut.add(br.readLine());
					}

					ctx.setAttribute("scriptResults", pOut, REQUEST);
					ctx.setAttribute("pilot", usr, REQUEST);
					ctx.setAttribute("imap", cfg, REQUEST);
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
			writeAuditLog(ctx, ae);
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
			result.setType(ResultType.REQREDIRECT);
			result.setURL("/jsp/admin/imapCreated.jsp");
		} else {
			result.setType(ResultType.REDIRECT);
			result.setURL("imaplist.do");
		}
	}
}