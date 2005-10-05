// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.comparators.GeoComparator;

/**
 * A &quot;map-like&quot; class to support multiple navigation data objects with the same code, and
 * return back a single bean based on distance from an arbitrary point. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NavigationDataMap implements java.io.Serializable {
   
   private Map _entries = new HashMap();

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
         _entries.put(nd.getCode(), new TreeSet());
      
      // Get the set and add the bean
      Set beans = (Set) _entries.get(nd.getCode());
      beans.add(nd);
   }
   
   /**
    * Adds a number of navigation aids to the map.
    * @param beans a Collection of NavigationDataBeans
    */
   public void addAll(Collection beans) {
      for (Iterator i = beans.iterator(); i.hasNext(); )
         add((NavigationDataBean) i.next());
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
    * @return a Set of entries, which may be empty
    */
   public Set getEntries(String code) {
      return contains(code) ? (Set) _entries.get(code.toUpperCase()) : Collections.EMPTY_SET;
   }
   
   /**
    * Returns a Navigation Aid with a given code. If more than one navigation aid exists with this code, then the 
    * first NavigationDataBean (using the class' native sorting) is returned.
    * @param code the navigation aid code
    * @return a NavigationDataBean, or null if not found
    * @see NavigationDataMap#get(String, GeoLocation) 
    */
   public NavigationDataBean get(String code) {
      Set codes = getEntries(code);
      return codes.isEmpty() ? null : (NavigationDataBean) codes.iterator().next();
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
      Set codes = new TreeSet(new GeoComparator(loc));
      codes.addAll(getEntries(code));
      return codes.isEmpty() ? null : (NavigationDataBean) codes.iterator().next();
   }
   
   /**
    * Returns all navigation aid beans contained within this object.
    * @return a Collection of NavigationDataBeans
    */
   public Collection getAll() {
      List results = new ArrayList();
      for (Iterator i = _entries.values().iterator(); i.hasNext(); )
         results.addAll((Set) i.next());
      
      return results;
   }
}