// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.input.*;

/**
 * A class to handle the fetching of common caches.
 * @author Luke
 * @version 3.6
 * @since 3.6
 */

public class CacheManager {
	
	private static final Logger log = Logger.getLogger(CacheManager.class);
	
	private static final ConcurrentMap<String, Cache<? extends Cacheable>> _caches = new ConcurrentHashMap<String, Cache<? extends Cacheable>>();

	// Singleton
	private CacheManager() {
		super();
	}
	
	/**
	 * Initilizes the Cache Manager.
	 * @param is the InputStream with the XML
	 * @throws IOException if an error occurs
	 */
	public static synchronized void init(InputStream is) throws IOException {
		
		// Load the XML
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(is);
		} catch (JDOMException je) {
			throw new IOException(je);
		}
		
		// Create the caches
		Element re = doc.getRootElement();
		for (Iterator<?> i = re.getChildren("cache").iterator(); i.hasNext(); ) {
			Element ce = (Element) i.next();
			String id = ce.getAttributeValue("id");
			if (id == null)
				continue;
			
			try {
				int expireTime = Integer.parseInt(ce.getAttributeValue("expires", "0"));
				int maxSize = Integer.parseInt(ce.getAttributeValue("max", "10"));
				
				// Build a cache depending on the type
				Cache<Cacheable> c = null;
				if (expireTime > 0)
					c = new ExpiringCache<Cacheable>(maxSize, expireTime);
				else
					c = new AgingCache<Cacheable>(maxSize);

				log.debug("Created cache " + id);
				_caches.putIfAbsent(id, c);
			} catch (Exception e) {
				log.error("Error initializing cache " + id + " -  " + e.getMessage(), e);
			}
		}
		
		log.info("Created " + _caches.size() + " caches");
	}
	
	/**
	 * Retrieves a Cache.
	 * @param id the Cache ID
	 * @return a Cache
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Cacheable> Cache<T> getCache(String id) {
		
		if (!_caches.containsKey(id))
		{
			Cache<T> cache = new AgingCache<T>(0);
			_caches.put(id, cache);
			return cache;
		}
		
		return (Cache<T>) _caches.get(id);
	}
	
	/**
	 * Returns all active cache instances.
	 * @return a Map of Caches, keyed by ID
	 */
	public static Map<String, Cache<?>> getCaches() {
		return new TreeMap<String, Cache<?>>(_caches);
	}
	
	/**
	 * Returns statistics about all active cache instances.
	 * @return a Collection of CacheStatistics beans
	 */
	public static Collection<CacheInfo> getStatistics() {
		Map<String, Cache<?>> caches = getCaches();
		Collection<CacheInfo> results = new TreeSet<CacheInfo>();
		for (Iterator<Map.Entry<String, Cache<?>>> i = caches.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String, Cache<?>> me = i.next();
			results.add(new CacheInfo(me.getKey(), me.getValue()));
		}
		
		return results;
	}
}