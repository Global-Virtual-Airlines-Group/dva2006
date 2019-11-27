package org.deltava.dao.file;

import java.io.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

public class TestPDFReader extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
	}
	
	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	@SuppressWarnings("static-method")
	public void testReadPDF() throws Exception {
		
		File f = new File("C:\\Temp\\Skyteam_Timetable_NA_EU.pdf");
		assertTrue(f.exists());
		
		String txt = null;
		try (InputStream is = new FileInputStream(f)) {
			GetPDFText prdao = new GetPDFText(is);
			prdao.setStartPage(5);
			txt = prdao.getText();
		}
		
		assertNotNull(txt);
		try (FileWriter fw = new FileWriter(new File("C:\\Temp\\Skyteam_Timetable_NA_EU.txt"))) {
			fw.write(txt);
		}
		
		try (LineNumberReader lr = new LineNumberReader(new StringReader(txt))) {
			String data = lr.readLine();
			while (data != null)
				data = lr.readLine();
		}
	}
}