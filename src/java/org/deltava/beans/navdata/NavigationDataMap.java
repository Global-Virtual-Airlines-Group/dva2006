// Copyright 2005, 2008, 2010, 2012, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.comparators.GeoComparator;

import org.deltava.util.cache.Cacheable;

/**
 * A &quot;map-like&quot; class to support multiple navigation data objects with the same code, and
 * return back a single bean based on distance from an arbitrary point. 
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class NavigationDataMap implements Cacheable {
   
   private final Map<String, SortedSet<NavigationDataBean>> _entries = new HashMap<String, SortedSet<NavigationDataBean>>();
   private Object _key;

   /**
    * Creates an empty NavigationDataMap bean.
    */
   public NavigationDataMap() {
	   super();
   }
   
   /**
    * Creates a pre-populaed NavigationDataMap bean.
    * @param entries the navigation aid beans to add
    */
   public NavigationDataMap(Collection<NavigationDataBean> entries) {
	   super();
	   entries.forEach(this::add);
   }
   
   /**
    * Adds a navigation aid to the map.
    * @param nd the navigation aid bean
    * @throws NullPointerException if nd is null
    */
   public void add(NavigationDataBean nd) {
	   if (nd == null)
		   return;
      
      // Check if we have the bean - if not, create a set to hold them
      if (!contains(nd.getCode()))
         _entries.put(nd.getCode(), new TreeSet<NavigationDataBean>());
      
      // Get the set and add the bean
      Collection<NavigationDataBean> beans = _entries.get(nd.getCode());
      beans.add(nd);
   }
   
   /**
    * Returns if the Map contains at least one Navigation Data bean with a particular code.
    * @param code the code
    * @return TRUE if at least one bean with this code is contained within the map, otherwise FALSE
    */
   public boolean contains(String code) {
      return (code == null) ? false : _entries.containsKey(code.toUpperCase());
   }
   
   /**
    * Returns all entries with a given code.
    * @param code the navigation aid code
    * @return a SortedSet of entries, which may be empty
    */
   public SortedSet<NavigationDataBean> getEntries(String code) {
      return contains(code) ? _entries.get(code.toUpperCase()) : new TreeSet<NavigationDataBean>();
   }
   
   /**
    * Returns a Navigation Aid with a given code. If more than one navigation aid exists with this code, then the 
    * first NavigationDataBean (using the class' native sorting) is returned.
    * @param code the navigation aid code
    * @return a NavigationDataBean, or null if not found
    * @see NavigationDataMap#get(String, GeoLocation) 
    */
   public NavigationDataBean get(String code) {
	   Collection<NavigationDataBean> codes = getEntries(code);
      return codes.isEmpty() ? null : codes.iterator().next();
   }

   /**
    * Returns a Navigation Aid with a given code. If more than one navigation aid exists with this code, then the 
    * closes navigation aid to the specified fixed point is returned.
    * @param code the navigation aid code
    * @param loc the reference location
    * @return a NavigationDataBean, or null if not found
    * @see NavigationDataMap#get(String)
    */
   public NavigationDataBean get(String code, GeoLocation loc) {
      Collection<NavigationDataBean> codes = new TreeSet<NavigationDataBean>(new GeoComparator(loc));
      codes.addAll(getEntries(code));
      return codes.isEmpty() ? null : codes.iterator().next();
   }
   
   /**
    * Returns all navigation aid beans contained within this object.
    * @return a Collection of NavigationDataBeans
    */
   public Collection<NavigationDataBean> getAll() {
      List<NavigationDataBean> results = new ArrayList<NavigationDataBean>();
      _entries.values().stream().forEach(results::addAll);
      return results;
   }
   
   /**
    * Returns whether the map is empty.
    * @return TRUE if the object is empty, otherwise FALSE
    */
   public boolean isEmpty() {
	   return _entries.isEmpty();
   }
   
   /**
    * Filters out navigation aids based on their type.
    * @param types a Collection of Integers with navigation aid type codes
    * @see NavigationDataBean#getType()
    */
   public void filter(Collection<Navaid> types) {
	   for (Iterator<? extends Set<NavigationDataBean>> i = _entries.values().iterator(); i.hasNext(); ) {
		   Set<NavigationDataBean> subEntries = i.next();
		   for (Iterator<NavigationDataBean> i2 = subEntries.iterator(); i2.hasNext(); ) {
			   NavigationDataBean nd = i2.next();
			   if (!types.contains(nd.getType()))
				   i2.remove();
		   }
		   
		   // Eliminate empty buckets
		   if (subEntries.isEmpty())
			   i.remove();
	   }
   }
   
   /**
    * Sets this bean's cache key. This is typically the query object used to generate this map.
    * @param key the cache key
    */
   public void setCacheKey(Object key) {
	   _key = key;
   }
   
   @Override
   public Object cacheKey() {
	   return _key;
   }
}