package org.deltava.dao.file;

import java.io.*;

import junit.framework.TestCase;

public class TestPDFReader extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
	}
	
	@SuppressWarnings("static-method")
	public void testSkyTeamPDF() throws Exception {
		
		File f = new File("C:\\Temp\\Skyteam_Timetable_Q1_2020.pdf");
		assertTrue(f.exists());
		
		String txt = null;
		try (InputStream is = new FileInputStream(f)) {
			GetPDFText prdao = new GetPDFText(is);
			prdao.setStartPage(5);
			txt = prdao.getText();
		}
		
		assertNotNull(txt);
		try (FileWriter fw = new FileWriter(new File("C:\\Temp\\Skyteam_Timetable.txt"))) {
			fw.write(txt);
		}
		
		try (LineNumberReader lr = new LineNumberReader(new StringReader(txt))) {
			String data = lr.readLine();
			while (data != null)
				data = lr.readLine();
		}
	}
	
	@SuppressWarnings("static-method")
	public void testDeltaPDF() throws Exception {
		
		File f = new File("C:\\Temp\\flight_schedules.pdf");
		assertTrue(f.exists());
		
		String txt = null;
		try (InputStream is = new FileInputStream(f)) {
			GetPDFText prdao = new GetPDFText(is);
			txt = prdao.getText();
		}

		assertNotNull(txt);
		try (FileWriter fw = new FileWriter(new File("C:\\Temp\\delta_schedule.txt"))) {
			fw.write(txt);
		}
		
		try (LineNumberReader lr = new LineNumberReader(new StringReader(txt))) {
			String data = lr.readLine();
			while (data != null)
				data = lr.readLine();
		}
	}
}