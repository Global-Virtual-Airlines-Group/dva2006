// Copyright 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetServInfo;
import org.deltava.util.system.SystemData;

public class UsageTracker implements Runnable {

	private final Logger log;
	private static final String JDBC_URL = "jdbc:mysql://localhost/vatsim?dontTrackOpenResources=true&continueBatchOnError=true&user=dva&password=m4n3l5";

	private final String _dataFile;
	private final int _interval;
	
	private long _lastUpdateDate = 1;
	
	public UsageTracker(String dataFile, int interval) {
		super();
		_dataFile = dataFile;
		log = Logger.getLogger(UsageTracker.class);
		_interval = Math.max(2, interval);
	}

	public void run() {
		log.info("Started");
		while (!Thread.currentThread().isInterrupted()) {
			long sleepTime = (_interval * 60000L);
			try {
				File f = new File(_dataFile);
				GetServInfo sidao = new GetServInfo(new FileInputStream(f));
				NetworkInfo info = sidao.getInfo(OnlineNetwork.VATSIM);
				
				// Get the pilots
				Collection<ConnectedUser> users = new ArrayList<ConnectedUser>(info.getPilots());
				users.addAll(info.getControllers());
				
				// Check if we've loaded it
				long validTime = info.getValidDate().getTime();
				if (validTime > _lastUpdateDate) {
					_lastUpdateDate = validTime;
					
					// Connect to the database
					try (Connection c = DriverManager.getConnection(JDBC_URL)) {
						try (PreparedStatement ps = c.prepareStatement("INSERT INTO USERSTATS (ID, DATE, CALLSIGN, USETIME, RATING) VALUES "
								+ "(?, CURDATE(), ?, ?, ?) ON DUPLICATE KEY UPDATE USETIME=USETIME+?")) {
							for (Iterator<ConnectedUser> i = users.iterator(); i.hasNext(); ) {
								ConnectedUser usr = i.next();
								ps.setInt(1, usr.getID());
								ps.setString(2, usr.getCallsign());
								ps.setInt(3, _interval);
								ps.setInt(4, (usr.getType() == NetworkUser.Type.PILOT) ? 0 : usr.getRating().ordinal());
								ps.setInt(5, _interval);
								ps.addBatch();
							}
						
							ps.executeBatch();
						}
					}
					
					log.info("Wrote " + users.size() + " records");
				} else {
					log.info("Servinfo feed not updated, waiting 30s");
					sleepTime = 30000;
				}
			} catch (SQLException se) {
				log.error(se.getMessage(), se);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			// Go to sleep
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

		log.info("Stopped");
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("<servinfo data file> ... ");
			System.exit(1);
			return;
		}

		// Init Log4J
		try {
			Properties p = new Properties();
			p.load(UsageTracker.class.getClassLoader().getResourceAsStream("log4j.properties"));
			PropertyConfigurator.configure(p);
			
			// Load the airports
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection c = DriverManager.getConnection(JDBC_URL)) {
				GetTimeZone tzdao = new GetTimeZone(c);
				tzdao.initAll();
				SystemData.add("airline.code", "DVA");
				GetAirport adao = new GetAirport(c);
				SystemData.add("airports", adao.getAll());
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(2);
		}

		// Start the thread
		Thread t = new Thread(new UsageTracker(args[0], 4), "Usage Tracker");
		t.setDaemon(true);
		t.start();
		while (t.isAlive()) {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException ie) {
				t.interrupt();
			}
			
			File f = new File("/var/run/usageTrack.stop");
			if (f.exists()) {
				t.interrupt();
				f.deleteOnExit();
			}
		}
		
		LogManager.shutdown();
	}
}