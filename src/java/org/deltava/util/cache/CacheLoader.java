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
 * @version 7.3
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
			int maxSize = StringUtils.parse(ce.getAttributeValue("max", "0"), 10);
			int expires = StringUtils.parse(ce.getAttributeValue("expires", "0"), 0);
			boolean isGeo = Boolean.valueOf(ce.getAttributeValue("geo", "false")).booleanValue();
			boolean isRemote = isGeo || Boolean.valueOf(ce.getAttributeValue("remote", "false")).booleanValue();
			CacheManager.register(Cacheable.class, ce.getAttributeValue("id"), maxSize, expires, isGeo, isRemote);
		}
	}
}