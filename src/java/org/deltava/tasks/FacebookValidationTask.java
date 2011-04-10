// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.util.concurrent.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.fb.ProfileInfo;

import org.deltava.dao.*;
import org.deltava.dao.http.GetFacebookData;

import org.deltava.taskman.*;

import org.deltava.util.ThreadUtils;

/**
 * A Scheduled Task to validate Facbook tokens.
 * @author Luke
 * @version 3.6
 * @since 3.6
 */

public class FacebookValidationTask extends Task {

	private class FacebookWorker extends Thread {
		private Logger tLog;
		private Queue<Pilot> _work;
		private Queue<Pilot> _output;
		
		FacebookWorker(int id, Queue<Pilot> in, Queue<Pilot> out) {
			super("FacebookWorker-" + String.valueOf(id));
			tLog = Logger.getLogger(FacebookValidationTask.class.getPackage().getName() + "." + getName());
			_work = in;
			_output = out;
		}
		
		@Override
		public void run() {
			Pilot p = _work.poll();
			while (p != null) {
				GetFacebookData fbdao = new GetFacebookData();
				fbdao.setToken(p.getIMHandle(IMAddress.FBTOKEN));
				fbdao.setWarnMode(true);
				
				int retryCount = 0; ProfileInfo info = null;
				while ((info == null) && (retryCount < 3)) {
					retryCount++;
					try {
						tLog.info("Validating Facebook token for " + p.getName() + ", attempt #" + retryCount);
						info = fbdao.getUserInfo();
					} catch (DAOException de) {
						// empty
					}
				}
				
				if (info == null)
					_output.add(p);
				
				// Get next pilot
				p = isInterrupted() ? null : _work.poll();
			}
		}
	}
	
	/**
	 * Initializes the Scheduled Task.
	 */
	public FacebookValidationTask() {
		super("Facebook Validation", FacebookValidationTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Load Pilots with a Facebook token
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Queue<Pilot> work = new LinkedBlockingQueue<Pilot>();
			work.addAll(pdao.getByIMType(IMAddress.FBTOKEN).values());
			int totalPilots = work.size();
			ctx.release();
			
			// Fire up the workers
			Queue<Pilot> pilots = new LinkedBlockingQueue<Pilot>();
			Collection<Thread> workers = new ArrayList<Thread>();
			for (int x = 1; x <= 6; x++) {
				FacebookWorker wrk = new FacebookWorker(x, work, pilots);
				workers.add(wrk);
				wrk.start();
			}
			
			// Wait for the workers to finish
			ThreadUtils.waitOnPool(workers);
			
			// Do a sanity check to ensure that FB isn't down
			if (pilots.size() >= (totalPilots * 0.8)) {
				log.warn(totalPilots + " total, " + pilots.size() + " invalid, aborting");
				pilots.clear();
			}
			
			// Write the data
			con = ctx.getConnection();
			SetPilot pwdao = new SetPilot(con);
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
				Pilot p = i.next();
				log.info("Clearing Facebook token for " + p.getName());
				p.setIMHandle(IMAddress.FB, null);
				p.setIMHandle(IMAddress.FBTOKEN, null);
				
				// Create status update
				StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.EXT_AUTH);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setCreatedOn(new Date());
				upd.setDescription("Clearing Facebook Token after errors");

				// Start transaction
				ctx.startTX();
				
				// Update pilot and status update
				pwdao.write(p);
				swdao.write(upd);
				
				// Commit
				ctx.commitTX();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}