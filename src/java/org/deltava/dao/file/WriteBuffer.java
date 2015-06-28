// Copyright 2005, 2006, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to write a buffer to the file system.
 * @author Luke
 * @version 4.1
 * @since 1.0
 */

public class WriteBuffer extends WriteableDAO {
	
	protected final File _f;

   /**
    * Initializes the Data Access Object.
    * @param f the file to write to
    */
   public WriteBuffer(File f) {
      super(null);
      _f = f;
   }
   
   /**
    * Initializes the Data Access Object.
    * @param path the path to the destination file
    * @param fileName the destination filename
    */
   public WriteBuffer(String path, String fileName) {
      this(new File(path, fileName));
   }

   /**
    * Writes a buffer to the filesystem.
    * @param buffer the buffer data
    * @throws DAOException if an I/O error occurs
    */
   public void write(byte[] buffer) throws DAOException {
      try (OutputStream os = new FileOutputStream(_f)) {
         os.write(buffer);
      } catch (IOException ie) {
         throw new DAOException(ie);
      }
   }
}