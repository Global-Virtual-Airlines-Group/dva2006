package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.deltava.beans.acars.*;
import org.deltava.dao.GetACARSPositions;
import org.deltava.util.ThreadUtils;

import junit.framework.TestCase;

public class TestConvertArchive extends TestCase {
	
	private static final int WORKERS = 8;
	private static final String URL = "jdbc:mysql://localhost/acars?user=luke&password=14072";
	
	protected final Queue<Integer> _IDwork = new ConcurrentLinkedQueue<Integer>();

	private final class ConvertWorker extends Thread {
		private final Connection _c;
		
		ConvertWorker(int id) throws SQLException {
			super("ConvertWorker-" + id);
			setDaemon(true);
			_c = DriverManager.getConnection(URL);
			_c.setReadOnly(true);
		}
		
		@Override
		public void run() {
			GetACARSPositions addao = new GetACARSPositions(_c);
			
			Integer i = _IDwork.poll();
			while (i !=  null) {
				int id = i.intValue();
				try {
					Collection<? extends RouteEntry> entries = addao.getRouteEntries(id, true);
					
					OutputStream out = new FileOutputStream(new File("/tmp", String.valueOf(id) + ".data.gz"));
					ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(out, 32768));
					oo.writeInt(id);
					oo.writeInt(entries.size());
					for (RouteEntry re : entries)
						oo.writeObject(re);
						
					oo.flush();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				i = _IDwork.poll();
			}
			
			try {
				_c.close();
			} catch (Exception e) { 
				// empty
			}
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		Class<?> c = Class.forName("com.mysql.jdbc.Driver");
		assertNotNull(c);
	}

	public void testConvert() throws SQLException {
		
		try (Connection c = DriverManager.getConnection(URL)) {
			c.setReadOnly(true);
			try (Statement s = c.createStatement()) {
				s.setFetchSize(500);
				try (ResultSet rs = s.executeQuery("SELECT ID FROM ARCHIVE_UPDATES")) {
					while (rs.next())
						_IDwork.add(Integer.valueOf(rs.getInt(1)));
				}
			}
		}
		
		Collection<Thread> workers = new ArrayList<Thread>();
		for (int x = 0; x < WORKERS; x++) {
			ConvertWorker wrk = new ConvertWorker(x+1);
			workers.add(wrk);
			wrk.start();
		}
		
		ThreadUtils.waitOnPool(workers);
	}
}