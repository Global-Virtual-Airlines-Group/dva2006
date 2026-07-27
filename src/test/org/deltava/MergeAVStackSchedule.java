package org.deltava;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.SetSchedule;
import org.deltava.dao.file.GetJSONSchedule;

import org.deltava.util.FileUtils;

public class MergeAVStackSchedule extends ScheduleTestCase {

	public void testMergeSchedules() throws Exception {
		
		File fd = new File("C:\\Temp");
		assertTrue(fd.isDirectory());
		FilenameFilter ff = FileUtils.fileFilter("avstack", "json");
		String[] fileNames = fd.list(ff);
		
		// Build the merge array
		JSONArray ja = new JSONArray();
		for (int x = 0; x < fileNames.length; x++) {
			File f = new File(fd, fileNames[x]);
			log.info("Loading {}", f.getName());			
			
			try (InputStream is = new BufferedInputStream(new FileInputStream(f), 131072)) {
				JSONObject sjo = new JSONObject(new JSONTokener(is));
				assertNotNull(sjo);
				assertTrue(sjo.has("entries"));
				JSONArray da = sjo.getJSONArray("entries");
				log.info("Loading {} entries", Integer.valueOf(da.length()));
				for (int y = 0; y < da.length(); y++)
					ja.put(da.getJSONObject(y));
			}
		}
		
		// Create the merge object
		JSONObject jo = new JSONObject();
		JSONObject jio = new JSONObject();
		jio.put("created", System.currentTimeMillis());
		jo.put("info", jio);
		jo.put("entries", ja);
		
		// Write the merge object
		try (FileWriter fw = new FileWriter(new File(fd, "avstack.json"))) {
			fw.write(jo.toString(2));
			fw.write("\n");
		}
	}
	
	public void testLoadSchedules() throws Exception {
		
		File fd = new File("C:\\Temp");
		File jf = new File(fd, "avstack.json");
		assertTrue(jf.exists());
		
		// Read the merged file
		Collection<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
		try (InputStream is = new BufferedInputStream(new FileInputStream(jf), 131072)) {
			GetJSONSchedule jsdao = new GetJSONSchedule(ScheduleSource.AVSTACK, is);
			entries.addAll(jsdao.process());
			assertFalse(entries.isEmpty());
		}
		
		// Save to the database
		try (Connection con = getConnection()) {
			SetSchedule wdao = new SetSchedule(con);
			wdao.purge(ScheduleSource.AVSTACK);
			for (RawScheduleEntry rse : entries)
				wdao.writeRaw(rse, false);
			
			con.commit();
			log.info("Wrote {} entries to database", Integer.valueOf(entries.size()));
		}
	}
}