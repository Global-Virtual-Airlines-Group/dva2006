// Copyright 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.util.*;

import org.jdom2.Document;

public abstract class BGLLoaderTestCase extends SceneryLoaderTestCase {
	
	protected static final String SCENERY_ROOT = "D:\\Program Files\\Flight Simulator 9\\Scenery";
	protected static final String XML_PATH = "C:\\temp\\bgxml";
	
	private static final String BGLXML = "data/bglxml/bglxml.exe";
	protected static final File EXE = new File(BGLXML);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Create the output directory
		File xmlP = new File(XML_PATH);
		if (!xmlP.exists())
			xmlP.mkdirs();
		
		// Check that we're running Windows and the file exists
		assertTrue(System.getProperty("os.name").contains("Windows"));
		assertTrue(EXE.exists() && EXE.isFile());
	}
	
	protected void convertBGLs(FileFilter ff) throws Exception {
		File rt = new File(SCENERY_ROOT);
		assertTrue(rt.isDirectory());
		
		Collection<File> bglFiles = getFiles(rt, ff);
		assertNotNull(bglFiles);
		
		// Process the BGLs
		for (Iterator<File> i = bglFiles.iterator(); i.hasNext(); ) {
			File bgl = i.next();
			String fRoot = bgl.getName().substring(0, bgl.getName().lastIndexOf('.'));
			File xml = new File(XML_PATH, fRoot + ".xml");
			
			// Covert the BGL
			log.info("Converting " + bgl.getName() + " to XML");
			ProcessBuilder pb = new ProcessBuilder(EXE.getAbsolutePath(), "-t", bgl.getPath(), xml.getPath());
			Process p = pb.start();
			int result = p.waitFor();
			if (result != 0) {
				try (InputStream is = new BufferedInputStream(p.getInputStream(), 1024)) {
					try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
						while (br.ready())	
							log.info(br.readLine());
					}
				}
				
				fail("Cannot convert to XML");
			}
			
			// Load the XML
			Document doc = null;
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(xml)))) {
				StringWriter xw = new StringWriter();
				try (PrintWriter pw = new PrintWriter(xw)) {
					while (br.ready()) {
						String data = br.readLine();
						data = data.replace('&', '_');
						pw.println(data);
					}
				}
			
				doc = loadXML(new StringReader(xw.toString()));
				assertNotNull(doc);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}