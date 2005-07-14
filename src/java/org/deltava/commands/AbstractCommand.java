package org.deltava.commands;

import java.util.*;

import javax.servlet.ServletContext;

/**
 * A class to support Web Site Commands.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractCommand implements Command {

   private String _id;
    private String _name;
    private List _roles;
    
    /**
     * Reference to the current servlet context.
     */
    protected ServletContext _ctx;
    
    /**
     * Initializes this command.
     * @param cmdName the name of the command
     * @throws CommandException if the command name is null
     * @throws IllegalStateException if the command has already been initialized
     */
    public void init(String id, String cmdName) throws CommandException {
        if (_name != null)
            throw new IllegalStateException(_name + " Command already initialized");
        
        try {
           _id = id.trim();
            _name = cmdName.trim();
        } catch (NullPointerException npe) {
            throw new CommandException("Command ID/Name cannot be null");
        }
    }

    /**
     * Update the servlet context.
     * @param ctx the servlet context
     */
    public final void setContext(ServletContext ctx) {
        _ctx = ctx;
    }

    /**
     * Returns the Command name.
     * @return the name of the command
     */
    public final String getName() {
        return _name;
    }
    
    /**
     * Returns the Command ID.
     * @return the command ID
     */
    public final String getID() {
       return _id;
    }
    
    /**
     * Return the roles authorized to execute this command. If setRoles() has not been called,
     * this will return an empty List. Commands defined to be executed by all users should have a
     * wildcard entry (*) as an authorized role.
     * @return a List of role names
     * @see AbstractCommand#setRoles(List)
     */
    public final List getRoles() {
        return (_roles == null) ? Collections.EMPTY_LIST : new ArrayList(_roles);
    }
    
    /**
     * Updates the roles authorized to execute this command. This will make a copy of the List object
     * provided (ie. making it immutable) for security reasons.
     * @param roles the List of role names
     * @throws IllegalStateException if setRoles() has already been called
     * @see AbstractCommand#getRoles()
     */
    public final void setRoles(List roles) {
        if (_roles != null)
            throw new IllegalStateException("Roles for " + getName() + " already set");
        
        _roles = new ArrayList(roles);
    }
}