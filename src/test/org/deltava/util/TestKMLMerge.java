package org.deltava.util;

import java.io.*;

import org.jdom.*;
import org.jdom.input.*;

import junit.framework.TestCase;

public class TestKMLMerge extends TestCase {

	private Document _doc;
	private Element _de;
	
	protected class KMLFilter implements FileFilter {
		
		public boolean accept(File f) {
			return f.getName().endsWith(".kml");
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		// Build the XML document
		_doc = new Document();
		Element ke = new Element("kml");
		_doc.setRootElement(ke);
		_de = new Element("Document");
		ke.addContent(_de);
	}
	
	public void testFIRMerge() throws IOException {
		
		SAXBuilder builder = new SAXBuilder();
		
		File f = new File("c:\\temp\\fir");
		File[] files = f.listFiles(new KMLFilter());
		for (int x = 0; x < files.length; x++) {
			File fn = files[x];
			System.out.println("Processing " + fn.getName());
			
			try {
				Document doc = builder.build(fn);
				Element ke = doc.getRootElement();
				Element pe = ke.getChild("Placemark", ke.getNamespace());
				if (pe == null) {
					System.err.println("No Placemark element");
				} else {
					Element pe2 = (Element) pe.clone();
					KMLUtils.setVisibility(pe2, false);
					_de.addContent(pe2);
				}
			} catch (JDOMException je) {
				System.err.println("Error - " + je.getMessage());
			}
		}
		
		// Write the XML
		PrintWriter pw = new PrintWriter(new FileWriter("c:\\temp\\firs.kml"));
		pw.println(XMLUtils.format(_doc));
		pw.close();
	}
}