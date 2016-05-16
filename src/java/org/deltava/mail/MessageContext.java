// Copyright 2004, 2007, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.system.*;

import org.deltava.util.StringUtils;
import org.deltava.util.bbcode.*;
import org.deltava.util.system.SystemData;

/**
 * A class to store and retrieve message context data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class MessageContext {
    
    private static final Logger log = Logger.getLogger(MessageContext.class);

    private final AirlineInformation _aInfo;
    
    private final BBCodeHandler _bbHandler = new BBCodeHandler();
    
    private MessageTemplate _mt;
    private String _subject;
    private String _body;
    private EMailAddress _recipient;
    private final Map<String, Object> _data = new HashMap<String, Object>();
    
    /**
     * Initializes the Message Context, and sets any pre-defined context elements.
     */
    public MessageContext() {
    	this(SystemData.get("airline.code"));
    }
    
    /**
     * Initializes the Message Context, and sets any pre-defined context elements.
     * @param aCode the airline code
     */
    public MessageContext(String aCode) {
    	super();
    	
    	// Initialize predefined variables
    	_aInfo = SystemData.getApp(aCode);
    	_data.put("airline", _aInfo.getName());
   		_data.put("url", (_aInfo.getSSL() ? "https" : "http") + "://www." + _aInfo.getDomain() + "/");
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
    
    private String eval(CharSequence s) {
    	final StringBuilder buf = new StringBuilder(s);
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
        
        return buf.toString();
    }
    
    /**
     * Returns the message subject.
     * @return the subject prepended by the Airline Name
     */
    public String getSubject() {
       StringBuilder buf = new StringBuilder(_aInfo.getName()).append(' ');
       if (_subject != null)
    	   buf.append(_subject);
       else
    	   buf.append((_mt == null) ? "" : _mt.getSubject());
       
       return eval(buf);
    }
    
    /**
     * Formats the message by replacing arguments in the message template with values from the context.
     * @return the formatted message body text
     * @throws IllegalStateException if no template exists
     */
    public String getBody() {
       if ((_mt == null) && (_body == null))
          throw new IllegalStateException("Message Template or Body not loaded");

       // Load the Message template and eval
       String body = eval((_mt != null) ? _mt.getBody() : _body);
       
       // If we have bbCode, evaluate it
       boolean hasBBCode = (_mt != null) && (body.indexOf('[') > -1) && (body.indexOf(']', body.indexOf('[')) > -1);
       boolean isHTML = (_mt != null) && _mt.getIsHTML();
       if (hasBBCode) {
    	   _bbHandler.init();
    	   for (BBCode bb : _bbHandler.getAll())
				body = body.replaceAll(bb.getRegex(), bb.getReplace());    	   
       }
       
       // Convert line breaks to HTML
       if (isHTML) {
    	   StringBuilder buf = new StringBuilder();
    	   StringTokenizer tkns = new StringTokenizer(body, " \n\r", true);
    	   while (tkns.hasMoreTokens()) {
    		   String token = tkns.nextToken();
				if (token.equals("\n"))
					buf.append("<br />\n");
				else
					buf.append(token);
    	   }
    	   
    	   body = buf.toString();
       }
       
       return body;
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
     * Sets the recipient of this email message.
     * @param to an EMailAddress
     */
    public void setRecipient(EMailAddress to) {
    	_recipient = to;
    	addData("recipient", to);
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
    
    /*
     * Helper method to determine if an object contains a particuar field.
     */
    private static boolean hasField(Object obj, String fieldName) {
        try {
            obj.getClass().getField(fieldName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
     * Helper method to determine if an object contains a particular method.
     */
    private static boolean hasMethod(Object obj, String methodName) {
        try {
            obj.getClass().getMethod(methodName, (Class []) null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /*
     * Helper method to determine if an object contains a particular property.
     */
    private static boolean hasProperty(Object obj, String propertyName) {
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
    	if (log.isDebugEnabled())
    		log.debug("Evaluating " + arg);
    	
    	FormatType fmtType = FormatType.RAW;
        StringTokenizer tkns = new StringTokenizer(arg, ".");
        
        // Get the object name
        String objName = tkns.nextToken();
        if (!tkns.hasMoreTokens() && (objName.indexOf('$') > -1)) {
        	fmtType = FormatType.getType(objName);
        	objName = objName.substring(0, objName.lastIndexOf('$'));
        }

        if (!hasData(objName)) {
        	if (_mt == null)
        		log.warn("Cannot evaluate " + objName);
        	else
        		log.warn("Cannot evaluate " + objName + " in " + _mt.getName());
        	
        	return "";
        }
        
        // Get the object value - if we have more tokens then we get the next token and assume it's a method
        // name to be called upon the object
        Object objValue = _data.get(objName);
        while (tkns.hasMoreTokens()) {
            String methodName = tkns.nextToken();
            
            // Determine format options
            if (!tkns.hasMoreTokens() && (methodName.indexOf('$') > -1)) {
            	fmtType = FormatType.getType(methodName);
            	methodName = methodName.substring(0, methodName.lastIndexOf('$'));
            }
            
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
            	if (_mt == null)
            		log.warn("Cannot evaluate " + objName + " at " + methodName);
            	else
            		log.warn("Cannot evaluate " + objName + " at " + methodName + " in " + _mt.getName());
            	
                return "";
            }
            
            // If we're going to invoke again, then save the last method name as the object name
            objName = methodName;
        }
        
        // If we have a recipient that supports formatting, do so
        if ((objValue != null) && (_recipient instanceof FormattedEMailRecipient)) {
        	FormattedEMailRecipient to = (FormattedEMailRecipient) _recipient;
        	switch (objValue.getClass().getSimpleName()) {
        	case "Long":
        	case "Integer":
        		String fmt = to.getNumberFormat();
        		if (fmt.indexOf('.') > -1)
        			fmt = fmt.substring(0, fmt.indexOf('.'));
        		
        		long value = ((Number) objValue).longValue();
        		if (fmtType == FormatType.DISTANCE) {
        			double cv = value * to.getDistanceType().getFactor();
        			StringBuilder buf = new StringBuilder(StringUtils.format(cv, fmt));
        			buf.append(' ').append(to.getDistanceType().getUnitName());
        			if (cv >= 2)
        				buf.append('s');
        			
        			objValue = buf.toString(); 		
        		} else
        			objValue = StringUtils.format(value, fmt);
        		
        		break;
        		
        	case "Double":
        		objValue = StringUtils.format(((Double) objValue).doubleValue(), to.getNumberFormat());
        		break;
        		
        	case "Instant":
        		objValue = ZonedDateTime.ofInstant((Instant) objValue, to.getTZ().getZone());
        		
				//$FALL-THROUGH$
			case "ZonedDateTime":
        		ZonedDateTime ldt = (ZonedDateTime) objValue; 
        		StringBuilder buf = new StringBuilder();
        		switch (fmtType) {
        		case TIME:
        			buf.append(StringUtils.format(ldt, to.getTimeFormat()));
        			buf.append(' ').append(to.getTZ().getAbbr());
        			objValue = buf.toString();
        			break;

        		case DATE:
        			objValue = StringUtils.format(ldt, to.getDateFormat());
        			break;
        			
        		default:
        			buf.append(StringUtils.format(ldt, to.getDateFormat() + " " + to.getTimeFormat()));
        			buf.append(' ').append(to.getTZ().getAbbr());
        			objValue = buf.toString();
        		}
        		
        		break;
        		
        	case "Airport":
        		Airport a = (Airport) objValue;
        		objValue = (to.getAirportCodeType() == Airport.Code.IATA) ? a.getIATA() : a.getICAO();
        		break;
        		
        	default:
        		break;
        	}
        }

        // Get the last object value, convert to a String and return
        return String.valueOf(objValue);
    }
}