// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao.file;

import java.io.File;

/**
 * An abstract class to support filesystem-based Data Access Objects.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class DAO {

   protected File _f;
   protected byte[] _buffer;
   
   private int _bufferSize = 16384;
   
   /**
    * Initializes the Data Access Object.
    * @param f the File to work with
    */
   public DAO(File f) {
      super();
      _f = f;
   }

   /**
    * Updates the I/O buffer size.
    * @param size the size of the buffer in bytes
    * @throws IllegalArgumentException if size is negative
    */
   public void setBufferSize(int size) {
      if (size < 0)
         throw new IllegalArgumentException("Invalid buffer size - " + size);
      
      _bufferSize = size;
   }
   
   /**
    * Initializes the I/O buffer.
    */
   protected void initBuffer() {
      _buffer = new byte[_bufferSize];
   }
}