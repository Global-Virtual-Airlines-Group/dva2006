// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import junit.framework.TestCase;

public abstract class SceneryLoaderTestCase extends TestCase {
	
	protected Logger log;
	
	final class DirectoryFilter implements FileFilter {
		public boolean accept(File f) {
			return ((f != null) && f.isDirectory() && !f.getName().startsWith("."));
		}
	}
	
	final class XMLFilter implements FileFilter {
		public boolean accept(File f) {
			String fn = f.getName().toLowerCase();
			return (f.isFile() && fn.endsWith(".xml"));
		}
	}
	
	protected Collection<File> getFiles(File root, FileFilter filter) {
		Collection<File> files = new ArrayList<File>();
		File[] dirs = root.listFiles(new DirectoryFilter());
		assertNotNull(dirs);
		for (int x = 0; x < dirs.length; x++) {
			File f = new File(dirs[x], "scenery");
			if (f.isDirectory()) {
				File[] bgls = f.listFiles(filter);
				if (bgls != null)
					files.addAll(Arrays.asList(bgls));
			}
		}
		
		return files;
	}
	
	protected Document loadXML(File f) throws IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(f);
	}
	
	protected void filterAmpersands(File f) throws IOException {
		InputStream in = new FileInputStream(f);
		LineNumberReader lr = new LineNumberReader(new InputStreamReader(in), 524288);
		File outF = new File(f.getCanonicalPath() + ".new");
		OutputStream out = new FileOutputStream(outF);
		PrintWriter pw = new PrintWriter(new BufferedOutputStream(out, 524288));
		while (lr.ready()) {
			String data = lr.readLine();
			int pos = data.indexOf('&');
			while (pos != -1) {
				boolean unescaped = (pos > (data.length() - 5));
				if (!unescaped) {
					String next4 = data.substring(pos+1, pos+5);
					unescaped = !"amp;".equals(next4);
				}
				
				if (unescaped) {
					StringBuilder buf = new StringBuilder(data.substring(0, pos));
					buf.append("&amp;");
					buf.append(data.substring(pos + 1));
					data = buf.toString();
				}
				
				pos = data.indexOf('&', pos+1);
			}
			
			// Filter unescaped quotes
			pos = data.indexOf('"');
			while (pos != -1) {
				boolean noEscape = (data.charAt(pos -1) == '=');
				noEscape |= (pos >= data.length() - 1);
				if (!noEscape) {
					noEscape |= Character.isWhitespace(data.charAt(pos+1));
					noEscape |= (data.charAt(pos+1) == '>');
				}
				
				if (!noEscape) {
					StringBuilder buf = new StringBuilder(data.substring(0, pos));
					buf.append('\'');	
					buf.append(data.substring(pos + 1));
					data = buf.toString();
				}
			
				pos = data.indexOf('"', pos+1);
			}
			
			pw.println(data);
		}
		
		in.close();
		pw.flush();
		out.close();
		f.delete();
		if (!outF.renameTo(f))
			log.error("Cannot rename " + outF.getName() + " to " + f.getName());
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
	}

	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}
}