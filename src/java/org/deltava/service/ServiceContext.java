// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.*;

import javax.servlet.http.*;

/**
 * An invocation/security context object for Web Services.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class ServiceContext extends org.deltava.commands.HTTPContext {
   
   private final OutputBuffer _buf = new OutputBuffer();
   
   protected class OutputBuffer {
      
      private final StringBuilder _buffer = new StringBuilder(256);
      
      public void print(CharSequence value) {
         _buffer.append(value);
      }
      
      public void println(CharSequence value) {
    	 _buffer.append(value);
         _buffer.append("\r\n");
      }
      
      public int length() {
         return _buffer.length();
      }
      
      public String toString() {
         return _buffer.toString();
      }
   }
   
   /**
    * Intiailizes the Web Service context.
    * @param req the HTTP servlet request
    * @param rsp the HTTP servlet response
    * @see ServiceContext#getRequest()
    * @see ServiceContext#getResponse()
    */
   public ServiceContext(HttpServletRequest req, HttpServletResponse rsp) {
      super(req, rsp);
   }

   /**
    * Prints a string to the output buffer.
    * @param data the string to print
    * @see ServiceContext#println(String)
    * @see ServiceContext#commit()
    */
   public void print(String data) {
      _buf.print(data);
   }
   
   /**
    * Prints a string and a trailing newline to the output buffer.
    * @param data the string to print
    * @see ServiceContext#print(String)
    * @see ServiceContext#commit()
    */
   public void println(String data) {
      _buf.println(data);
   }
   
   /**
    * Writes the output buffer to the HTTP servlet response, setting the Content-length header.
    * @throws IOException if an I/O error occurs
    * @see ServiceContext#print(String)
    * @see ServiceContext#println(String)
    */
   public void commit() throws IOException {
	   HttpServletResponse rsp = getResponse();
      rsp.setBufferSize((_buf.length() < 32768) ? _buf.length() + 16 : 32768);
      rsp.setContentLength(_buf.length());
      rsp.getWriter().print(_buf);
      rsp.flushBuffer();
   }
}