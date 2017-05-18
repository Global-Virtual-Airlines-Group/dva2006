// Copyright 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.io.*;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.deltava.beans.Helper;
import org.deltava.util.StringUtils;

/**
 * A utility class to register caches from an XML file.
 * @author Luke
 * @version 7.4
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
		
		Document doc = null;
		try {
			doc = new SAXBuilder().build(in);
		} catch (JDOMException je) {
			throw new IOException(je);
		}
		
		// Parse the entries
		for (Element ce : doc.getRootElement().getChildren("cache")) {
			CacheConfig cfg = new CacheConfig(ce.getAttributeValue("id"));
			cfg.setMaxSize(StringUtils.parse(ce.getAttributeValue("max", "0"), 10));
			cfg.setExpiryTime(StringUtils.parse(ce.getAttributeValue("expires", "-1"), 0));
			cfg.setGeo(Boolean.valueOf(ce.getAttributeValue("geo", "false")).booleanValue());
			cfg.setRemote(Boolean.valueOf(ce.getAttributeValue("remote", "false")).booleanValue());
			if (cfg.isGeo())
				cfg.setPrecision(StringUtils.parse(ce.getAttributeValue("precision"), 2));
			
			CacheManager.register(Cacheable.class, cfg);
		}
	}
}