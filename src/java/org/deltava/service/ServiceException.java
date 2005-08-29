// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

/**
 * An Exception thrown by Web Services. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServiceException extends Exception {
   
   private int _httpCode;

   /**
    * 
    * @param code
    * @param msg
    * @param t
    */
   ServiceException(int code, String msg, Throwable t) {
      super(msg, t);
      _httpCode = code;
   }

   /**
    * 
    * @param code
    * @param msg
    */
   ServiceException(int code, String msg) {
      super(msg);
      _httpCode = code;
   }
   
   /**
    * Returns the HTTP result code.
    * @return the HTTP result code
    */
   public int getCode() {
      return _httpCode;
   }
}
