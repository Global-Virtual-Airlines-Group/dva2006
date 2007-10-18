// Copyright 2004, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

import java.lang.reflect.*;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.system.MessageTemplate;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A class to store and retrieve message context data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MessageContext {
    
    private static final Logger log = Logger.getLogger(MessageContext.class);

    private MessageTemplate _mt;
    private String _subject;
    private String _body;
    private final Map<String, Object> _data = new HashMap<String, Object>();
  
    /**
     * Initializes the Message Context, and sets any pre-defined context elements.
     */
    public MessageContext() {
    	super();
    	
    	// Initialize predefined variables
    	_data.put("airline", SystemData.get("airline.name"));
    	String url = SystemData.get("airline.url");
    	if (url == null)
    		_data.put("url", "http://www." + SystemData.get("airline.domain") + "/");
    	else
    		_data.put("url", "http://" + url + "/");
    }
    
    /**
     * Adds an object to the context data.
     * @param name the name of the object
     * @param obj the object
     * @throws NullPointerException if name is null
     */
    public void addData(String name, Object obj) {
        _data.put(name.trim(), obj);
    }
    
    /**
     * Overrides the subject used in the message, instead of using the Message Template subject.
     * @param subj the new subject
     */
    public void setSubject(String subj) {
    	_subject = subj;
    }
    
    /**
     * Returns the message subject.
     * @return the subject prepended by the Airline Name
     */
    public String getSubject() {
       StringBuilder buf = new StringBuilder(SystemData.get("airline.name"));
       buf.append(' ');
       if (_subject != null)
    	   buf.append(_subject);
       else
    	   buf.append((_mt == null) ? "" : _mt.getSubject());
       
       return buf.toString();
    }
    
    /**
     * Formats the message by replacing arguments in the message template with values from the context.
     * @return the formatted message body text
     * @throws IllegalStateException if no template exists
     */
    public String getBody() {
       if ((_mt == null) && (_body == null))
          throw new IllegalStateException("Message Template or Body not loaded");

       // Load the Message template
       StringBuilder buf = new StringBuilder((_mt == null) ? _mt.getBody() : _body);

       // Parse the message template with data from the MessageContext
       int spos = buf.indexOf("${");
       while (spos != -1) {
          int epos = buf.indexOf("}", spos);

          // Only format if the end token can be found
          if (epos > spos) {
             String token = buf.substring(spos + 2, epos);
             buf.replace(spos, epos + 1, execute(token));
             spos = buf.indexOf("${");
          } else
             spos = buf.indexOf("${", spos);
       }

       // Return the message body
       return buf.toString();
    }
    
    /**
     * Checks if a particular named object exists within this context.
     * @param name the name of the object
     * @return TRUE if the object exists within this context, otherwise false
     * @throws NullPointerException if name is null
     */
    boolean hasData(String name) {
        return _data.containsKey(name.trim());
    }
    
    /**
     * Sets the message body to use, if no template is used.
     * @param body the message body
     */
    public void setBody(String body) {
    	_body = body;
    }
    
    /**
     * Sets the message template to use.
     * @param mt the Message Template
     * @see MessageContext#getTemplate()
     */
    public void setTemplate(MessageTemplate mt) {
    	_mt = mt;
    }
    
    /**
     * Returns the Message template in use.
     * @return a MessageTemplate object
     * @see MessageContext#getTemplate()
     */
    public MessageTemplate getTemplate() {
    	return _mt;
    }
    
    /**
     * Helper method to determine if an object contains a particuar field.
     */
    private boolean hasField(Object obj, String fieldName) {
        try {
            obj.getClass().getField(fieldName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper method to determine if an object contains a particular method.
     */
    private boolean hasMethod(Object obj, String methodName) {
        try {
            obj.getClass().getMethod(methodName, (Class []) null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Helper method to determine if an object contains a particular property.
     */
    private boolean hasProperty(Object obj, String propertyName) {
    	return hasMethod(obj, StringUtils.getPropertyMethod(propertyName));
    }
    
    /**
     * Determines the value of a given macro argument via recursive reflection. The argument is in the following format: 
     * <i>name</i>.[<i>method</i> OR <i>field</i> ...] which can be repeated numerous times. For example the
     * macro pirep.getAirportA.getIATA will get the named object called "pirep", and then execute the getAirportA()
     * method on this object. The method getIATA() method will be returned on the result of the first method call.
     * If no method matching a given name is found, a field with this name will be used if present. If no method or field
     * matching a given component is found, then execution fails.
     * @param arg the argument macro
     * @return the value of the argument macro, or an empty String ("") if execution fails
     */
    String execute(String arg) {
    	log.debug("Evaluating " + arg);
        StringTokenizer tkns = new StringTokenizer(arg, ".");
        
        // Get the object name
        String objName = tkns.nextToken();
        if (!hasData(objName)) {
        	log.warn("Cannot evaluate " + objName);
        	return "";
        }
        
        // Get the object value - if we have more tokens then we get the next token and assume it's a method
        // name to be called upon the object
        Object objValue = _data.get(objName);
        while (tkns.hasMoreTokens()) {
            String methodName = tkns.nextToken();
            if (hasProperty(objValue, methodName)) {
                try {
                    Method m = objValue.getClass().getMethod(StringUtils.getPropertyMethod(methodName), (Class []) null);
                    objValue = m.invoke(objValue, (Object []) null);
                } catch (Exception e) {
                    log.warn("Error reading " + objName + "." + methodName + " - " + e.getClass().getName());
                    return "";
                }
            } else if (hasMethod(objValue, methodName)) {
                try {
                    Method m = objValue.getClass().getMethod(methodName, (Class []) null);
                    objValue = m.invoke(objValue, (Object []) null);
                } catch (Exception e) {
                    log.warn("Error invoking " + objName + "." + methodName + "() - " + e.getClass().getName());
                    return "";
                }
            } else if (hasField(objValue, methodName)) {
                try {
                    Field f = objValue.getClass().getField(methodName);
                    objValue = f.get(objValue);
                } catch (Exception e) {
                    log.warn("Error getting " + objName + "." + methodName + " - " + e.getClass().getName());
                    return "";
                }
            } else {
            	log.warn("Cannot evaluate " + objName + " at " + methodName);
                return "";
            }
            
            // If we're going to invoke again, then save the last method name as the object name
            objName = methodName;
        }

        // Get the last object value, convert to a String and return
        return String.valueOf(objValue);
    }
}