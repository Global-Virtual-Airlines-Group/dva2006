// Copyright 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;

import org.deltava.dao.*;
import org.deltava.mail.MailerDaemon;
import org.deltava.taskman.*;

/**
 * A Scheduled Task to remove invalid Push Notification endpoints. 
 * @author Luke
 * @version 11.1
 * @since 10.0
 */

public class PushInvalidationTask extends Task {

	/**
	 * Creates the Task.
	 */
	public PushInvalidationTask() {
		super("Push Endpoint Invalidation", PushInvalidationTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Get the endpoints and IDs
		Collection<PushEndpoint> endpoints = MailerDaemon.getInvalidEndpoints();
		Collection<Integer> IDs = endpoints.stream().map(DatabaseBean::getID).collect(Collectors.toSet());
		if (IDs.isEmpty())
			return;

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			SetPilotPush pwdao = new SetPilotPush(con);
			SetStatusUpdate uwdao = new SetStatusUpdate(con);
			
			// Load the Pilots
			ctx.startTX();
			UserDataMap udm = uddao.get(IDs);
			Map<Integer, Pilot> pilots = pdao.get(udm);
			for (PushEndpoint ep : endpoints) {
				UserData ud = udm.get(Integer.valueOf(ep.getID()));
				if (ud == null) {
					log.warn("Unknown Pilot ID - {}", Integer.valueOf(ep.getID()));
					continue;
				} else if (!ud.getDB().equals(ctx.getDB())) {
					log.error("Cannot invalidate endpoint for Pilot {} (app = {})", Integer.valueOf(ud.getID()), ud.getAirlineCode());
					continue;
				}
				
				// Create the status update
				StatusUpdate upd = new StatusUpdate(ep.getID(), UpdateType.ADDRINVALID);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Invalid Push Endpoint - " + ep.getURL());
				
				Pilot p = pilots.get(Integer.valueOf(ep.getID()));
				log.warn("Deleting invalid endpoint {} for {} ({})", ep.getURL(), p.getName(), Integer.valueOf(ep.getID()));
				pwdao.delete(p.getID(), ep.getURL());
				uwdao.write(upd, ctx.getDB());
			}
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}
		
		log.info("Processing Complete");
	}
}