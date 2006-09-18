// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.EMailConfiguration;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to create a new IMAP mailbox profile.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IMAPMailboxCommand extends AbstractCommand {

	//private class 
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Pilot
			GetPilotDirectory dao = new GetPilotDirectory(con);
            GetPilotEMail edao = new GetPilotEMail(con);
			Pilot usr = dao.get(ctx.getID());
			if (usr == null)
				throw notFoundException("Invalid Pilot - " + ctx.getID());
			
			// Get the Mailbox profile
			EMailConfiguration emailCfg = edao.getEMailInfo(ctx.getID());
			if (emailCfg != null) {
				CommandException ce = new CommandException(usr.getName() + " already has an IMAP mailbox");
				ce.setLogStackDump(false);
				throw ce;
			}
            
            // Check our access
            PilotAccessControl access = new PilotAccessControl(ctx, usr);
            access.validate();
            if (!access.getCanChangeMailProfile())
               throw securityException("Cannot create IMAP mailbox");
            
			// Pre-populate the mailbox address
            String fName = usr.getFirstName().toLowerCase(); 
            String mbAddr = fName + "@" + SystemData.get("airline.domain");
            if (!edao.isAvailable(mbAddr))
               mbAddr = fName + usr.getLastName().substring(0, 1).toLowerCase() + "@" + SystemData.get("airline.domain");
            
            // Start a transaction
            ctx.startTX();
            
            // Create the mailbox profile
            emailCfg = new EMailConfiguration(usr.getID(), mbAddr);
            emailCfg.setMailDirectory(String.valueOf(usr.getID()));
            emailCfg.setQuota(SystemData.getInt("smtp.imap.quota"));
            emailCfg.setActive(true);
            
            // Generate the mailbox directory
            ProcessBuilder pBuilder = new ProcessBuilder(SystemData.get("smtp.imap.script"), emailCfg.getMailDirectory(),
            		SystemData.get("smtp.imap.path"));
            pBuilder.redirectErrorStream(true);
            try {
            	Process p = pBuilder.start();
            	BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            	
            	// Wait for the process to complete
            	int runTime = 0;
            	while (runTime < 5000) {
            		ThreadUtils.sleep(250);
            		try {
            			if (p.exitValue() != 1)
                    		throw new DAOException("Unable to create mailbox - error " + p.exitValue());
            			
            			break;
            		} catch (IllegalThreadStateException itse) {
            			runTime += 250;            			
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
            
            // Write the mailbox profile
            SetPilotEMail wdao = new SetPilotEMail(con);
            wdao.write(emailCfg, usr.getName());
            wdao.updatePassword(emailCfg.getID(), SystemData.get("smtp.imap.default_pwd"));
            
            // Commit the transaction
            ctx.commitTX();
            
            // Save context attributes
            ctx.setAttribute("pilot", usr, REQUEST);
            ctx.setAttribute("imap", emailCfg, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
        result.setURL("/jsp/admin/imapCreated.jsp");
        result.setSuccess(true);
	}
}