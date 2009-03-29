package org.deltava.search;

import java.io.*;
import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.*;

import org.deltava.beans.*;
import org.deltava.beans.cooler.MessageThread;

import org.deltava.dao.*;
import org.deltava.util.SearchUtils;
import org.deltava.util.system.SystemData;

public class CoolerIndexCreate extends TestCase {

	private Logger log;

	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/common?useCompression=true";

	private Connection _c;
	private Connection _c2;
	
	private IndexWriter iw;

	protected void setUp() throws Exception {
		super.setUp();

		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(CoolerIndexCreate.class);

		SystemData.init();
		SystemData.add("airline.db", "dva");

		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "import", "import");
		assertNotNull(_c);
		_c2 = DriverManager.getConnection(JDBC_URL, "import", "import");
		assertNotNull(_c2);

		// Load Database information
		GetUserData uddao = new GetUserData(_c);
		SystemData.add("apps", uddao.getAirlines(true));
		
		// Init the Lucene index
		File dir = new File("c:\\temp\\coolerIdx");
		iw = new IndexWriter(dir, SearchUtils.getAnaylyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
		assertNotNull(iw);
	}

	protected void tearDown() throws Exception {
		iw.close();
		_c.close();
		_c2.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testCreateIndex() throws Exception {
		
		// Get the threads
		PreparedStatement ps = _c.prepareStatement("SELECT ID, CHANNEL, SUBJECT FROM COOLER_THREADS ORDER BY ID");
		ps.setFetchSize(3000);
		ResultSet rs = ps.executeQuery();

		// Load the thread Data
		int threadCount = 0;
		Map<Integer, MessageThread> threads = new HashMap<Integer, MessageThread>();
		while (rs.next()) {
			MessageThread mt = new MessageThread(rs.getString(3));
			mt.setID(rs.getInt(1));
			mt.setChannel(rs.getString(2));
			threads.put(new Integer(mt.getID()), mt);
			threadCount++;
		}

		// Clean up
		rs.close();
		ps.close();
		log.info(threadCount + " threads loaded");
		
		// Load user profiles
		Map<Integer, String> IDs = new HashMap<Integer, String>();
		ps = _c.prepareStatement("SELECT UD.ID, UD.AIRLINE FROM USERDATA UD, COOLER_POSTS CP WHERE (UD.ID=CP.AUTHOR_ID) "
				+ "AND (UD.TABLENAME=?)");
		ps.setString(1, "PILOTS");
		ps.setFetchSize(2000);
		rs = ps.executeQuery();
		while (rs.next())
			IDs.put(new Integer(rs.getInt(1)), rs.getString(2));
		
		// Clean up
		rs.close();
		ps.close();
		log.info("Loaded user data");
		
		// Get the user info
		GetPilot pdao = new GetPilot(_c2);
		Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
		
		// Get the posts
		ps = _c.prepareStatement("SELECT THREAD_ID, AUTHOR_ID, CREATED, MSGBODY FROM COOLER_POSTS LIMIT ?, 15000");
		ps.setFetchSize(3000);
		ps.setInt(1, 0);
		rs = ps.executeQuery();

		// Load the thread data
		int postCount = 0;
		boolean doMore = rs.next();
		while (doMore) {
			int id = rs.getInt(1);
			MessageThread mt = threads.get(new Integer(id));
			if (mt != null) {
				Document doc = new Document();
				doc.add(new Field("id", NumberTools.longToString(id), Field.Store.YES, Field.Index.NOT_ANALYZED));
				doc.add(new Field("created", DateTools.dateToString(rs.getTimestamp(3), DateTools.Resolution.MINUTE), Field.Store.NO, Field.Index.NOT_ANALYZED));
				doc.add(new Field("subject", mt.getSubject(), Field.Store.NO, Field.Index.ANALYZED));
				doc.add(new Field("channel", mt.getChannel(), Field.Store.NO, Field.Index.NOT_ANALYZED));
				
				// Load author
				Integer userID = new Integer(rs.getInt(2));
				Pilot usr = pilots.get(userID);
				if (usr == null) {
					String db = IDs.get(userID);
					if (db != null) {
						UserData ud = new UserData(db, "PILOTS", "blah");
						ud.setID(userID.intValue());
						usr = pdao.get(ud);
						pilots.put(userID, usr);
						IDs.remove(userID);
					}
				}	
					
				if (usr != null)
					doc.add(new Field("author", usr.getName(), Field.Store.NO, Field.Index.NOT_ANALYZED));
			
				// Load Body
				String body = rs.getString(4);
				if (body != null) {
					doc.add(new Field("body", body, Field.Store.NO, Field.Index.ANALYZED));
					iw.addDocument(doc);
				}
			}
			
			// Update counters
			postCount++;
			doMore = rs.next();
			if (!doMore) {
				log.info(postCount + " posts loaded");
				rs.close();
				ps.setInt(1, postCount);
				rs = ps.executeQuery();
				doMore = rs.next();
			}
		}

		// Clean up
		ps.close();
		rs.close();
		iw.commit();

		log.info("Completed");
	}
}