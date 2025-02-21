// Copyright 2012, 2015, 2017, 2022, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.io.*;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.deltava.beans.Helper;
import org.deltava.util.StringUtils;

/**
 * A utility class to register caches from an XML file.
 * @author Luke
 * @version 11.5
 * @since 5.0
 */

@Helper(CacheManager.class)
public class CacheLoader {

	// singleton
	private CacheLoader() {
		super();
	}
	
	/**
	 * Initializes the Cache Manager.
	 * @param in an InputStream to the XML file to load
	 * @throws IOException if an I/O error occurs
	 */
	public static void load(InputStream in) throws IOException {
		
		try {
			Document doc = new SAXBuilder().build(in);
		
			// Parse the entries
			Element re = doc.getRootElement();
			CacheManager.init(re.getAttributeValue("id", "???"));
			for (Element ce : doc.getRootElement().getChildren("cache")) {
				CacheConfig cfg = new CacheConfig(ce.getAttributeValue("id"));
				cfg.setMaxSize(StringUtils.parse(ce.getAttributeValue("max", "0"), 10));
				cfg.setExpiryTime(parseExpiration(ce.getAttributeValue("expires", "-1")));
				cfg.setGeo(Boolean.parseBoolean(ce.getAttributeValue("geo", "false")));
				cfg.setRemote(Boolean.parseBoolean(ce.getAttributeValue("remote", "false")));
				if (cfg.isGeo())
					cfg.setPrecision(StringUtils.parse(ce.getAttributeValue("precision"), 0.01));
			
				CacheManager.register(Cacheable.class, cfg);
			}
		} catch (JDOMException je) {
			throw new IOException(je);
		}
	}
	
	/*
	 * Helper method to parse human-readable expiration intervals.
	 */
	private static int parseExpiration(String v) {
		char c = Character.toLowerCase(v.charAt(v.length() - 1));
		int factor = switch (c) {
			case 'm' -> 60;
			case 'h' -> 3600;
			case 'd' -> 86400;
			case 'w' -> 86400 * 7;
			default -> 1;
		};
		
		String vv = Character.isDigit(c) ? v : v.substring(0, v.length() - 1); 
		return StringUtils.parse(vv, 0) * factor;
	}
}