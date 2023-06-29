// Copyright 2005, 2009, 2015, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;

import org.deltava.beans.fleet.*;

import org.deltava.commands.AbstractCommand;

/**
 * An abstract class to support Library-related Web Site Commands.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public abstract class AbstractLibraryCommand extends AbstractCommand {

	/**
	 * Helper method to append the database name to the end of the entry names.
	 * @param entries a Collection of LibraryEntry beans
	 * @param dbName the database name to append
	 */
	protected static void appendDB(Collection<? extends LibraryEntry> entries, String dbName) {
		for (LibraryEntry entry : entries) {
			entry.setName(entry.getName() + " - " + dbName.toUpperCase());
			if (entry instanceof Installer in)
				in.setCode(dbName.toUpperCase() + "." + in.getCode());
		}
	}
}