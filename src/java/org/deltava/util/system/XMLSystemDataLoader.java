// Copyright 2004, 2005, 2006, 2007, 2009, 2010, 2012, 2015, 2016, 2017, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.system;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;

import org.deltava.util.ConfigLoader;

/**
 * A SystemData loader that parses an XML file.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class XMLSystemDataLoader implements SystemDataLoader {

    private static final Logger log = LogManager.getLogger(XMLSystemDataLoader.class);
    private static final String XML_FILENAME = "/etc/systemConfig.xml";

    private Map<String, Object> _data;

    /**
     * Load XML data from the disk. This uses the class loader to load the default file, etc/systemConfig.xml.
     * @throws FileNotFoundException if the file cannot be found
     * @throws IOException if an error occurs reading the file
     */
    @Override
    public Map<String, Object> load() throws IOException {

        // Create the builder and load the file into an XML in-memory document
        Document doc = null;
        try (InputStream is = ConfigLoader.getStream(XML_FILENAME)) {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(is);
        } catch (JDOMException je) {
            throw new IOException("XML Parse Error", je);
        }

        // Get the root element
        Element root = doc.getRootElement();
        if (root == null)
            throw new IOException("Empty XML Document");

        // Log the configuration
        _data = new HashMap<String, Object>();
        _data.put(SystemData.CFG_NAME, root.getAttributeValue("env"));
        log.info("Loading configuration environemnt - {}", root.getAttributeValue("env"));

        // Parse through its children
        process("", root);

        // Return the data
        return _data;
    }

    /**
     * Return an XML element's value, converted to an object based in the <i>type </i> attribute. The class
     * specified in this attribute needs to implement a static valueOf(String) method.
     * @param e the XML element to process
     * @return the value of the element, primitives are wrapped within their object
     * @see String#valueOf(java.lang.Object)
     */
    protected static Object getElementWithType(Element e) {
        // Get the elemnt type, default is a string
        String eType = e.getAttributeValue("type", "java.lang.String");
        if (eType.indexOf('.') == -1)
            eType = "java.lang." + eType;

        // If the type is a String, then just get the value back - we don't need reflection
        if ("java.lang.String".equals(eType))
            return e.getValue();

        // Invoke the static "valueOf" method of the type
        try {
            Class<?> eClass = Class.forName(eType);
            Method m = eClass.getMethod("valueOf", new Class[] { String.class });
            return m.invoke(null, new Object[] { e.getValue() });
        } catch (Exception ex) {
            log.warn("Error loading {} - {}", e.getName(), ex.getClass().getName());
            return null;
        }
    }

    /**
     * Load a list from an XML element's children. The attribute <i>sorted </i> determines if the list will be
     * sorted. The attribute <i>attr </i> provides the name of the child elements to get. Internally, this
     * method uses a Set (HashSet or TreeSet) to ensure that all values are unique. This method
     * calls @link { XMLSystemDataLoader#getElementWithType(Element) } to do type conversions.
     * @param root the XML element to process
     * @return a List of child values
     * @see XMLSystemDataLoader#getElementWithType(Element)
     */
    @SuppressWarnings("unchecked")
    protected static List<? extends Object> processList(Element root) {
        boolean isSorted = Boolean.parseBoolean(root.getAttributeValue("sorted", "false"));
        boolean isUnique = Boolean.parseBoolean(root.getAttributeValue("unique", "false"));
        
        // Figure out the type of collection to return
        String className = "java.util.ArrayList";
        if (isSorted)
        	className = "java.util.TreeSet";
        else if (isUnique)
        	className = "java.util.LinkedHashSet";
        
        // If we're a sorted set, return a TreeSet instead of a HashSet
        Collection<Object> results = null;
        try {
            results = (Collection<Object>) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
        	results = new ArrayList<Object>();
        }

        // Get all elements with the given attribute name
        for (Element e : root.getChildren(root.getAttributeValue("attr")))
            results.add(getElementWithType(e));

        return new ArrayList<Object>(results);
    }

    /**
     * Load a map from an XML element's children. The attribute <i>attr </i> determines the name of the child
     * elements to get. Each of this child elements will be inserted into the map with the value of the child
     * element's <i>key </i> attribute if present; otherwise the child's value will be used as the key. This
     * method calls @link { getElementWithType(Element) } to do type conversions.
     * @param root the XML element to process
     * @return a Map of child values
     * @see XMLSystemDataLoader#getElementWithType(Element)
     */
    protected static Map<String, Object> processMap(Element root) {
        Map<String, Object> results = new LinkedHashMap<String, Object>();
        for (Element e: root.getChildren()) {
            Object value = getElementWithType(e);
            results.put(e.getName(), value);
        }

        return results;
    }

    /**
     * Recusrively process an XML entry that may have child entries.
     * @param rName the hierarchical root entry name
     * @param re the XML element to process
     */
    protected void process(String rName, Element re) {
    	String rootName = (rName.length() > 0) ? rName + "." : rName;
        for (Element e : re.getChildren()) {
            String eType = e.getName();

            /* The rules for recursive processing are this; if the element is a list or map, then we process
             * it and ignore the other children. If it doesn't have children, process it as a regular entry.
             * Otherwise, we call this method recursively to iterate through its children. */
            try {
                if ("list".equals(eType)) {
                    String eName = rootName + e.getAttributeValue("name", "$unNamedList");
                    log.debug("Processing List {}", eName);
                    _data.put(eName, processList(e));
                } else if ("map".equals(eType)) {
                    String eName = rootName + e.getAttributeValue("name", "$unNamedMap");
                    log.debug("Processing Map {}", eName);
                    _data.put(eName, processMap(e));
                } else if (e.getChildren().size() == 0) {
                    String eName = rootName + eType;
                    log.debug("Processing {}", eName);
                    _data.put(eName, getElementWithType(e));
                } else {
                    String eName = rootName + eType;
                    log.debug("Processing sub-entry {}", eName);
                    process(eName, e);
                }
            } catch (Exception ex) {
                log.warn("Error processing {} - {}", eType, ex.getMessage());
            }
        }
    }
}