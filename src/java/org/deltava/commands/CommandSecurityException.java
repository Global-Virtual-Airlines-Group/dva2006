// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands;

/**
 * An exception thrown on a security contraint violation during command execution.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandSecurityException extends CommandException {

	private String _cmdName;

	/**
	 * Create a new security exception.
	 * @param msg the exception message
	 * @param cmdName the name of the command being invoked
	 */
	public CommandSecurityException(String msg, String cmdName) {
		super(msg);
		_cmdName = cmdName;
	}

	/**
	 * Sets the command name.
	 * @param cmd the command
	 * @throws NullPointerException if cmd is null
	 */
	public void setCommand(Command cmd) {
		_cmdName = cmd.getName();
	}

	/**
	 * Returns the Command name.
	 * @return the command
	 */
	public String getCommand() {
		return _cmdName;
	}
}