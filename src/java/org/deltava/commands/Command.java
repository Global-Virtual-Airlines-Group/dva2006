package org.deltava.commands;

import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.jsp.PageContext;

/**
 * A web Command.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Command extends java.io.Serializable {
    
    public static final int APPLICATION = PageContext.APPLICATION_SCOPE;
    public static final int REQUEST = PageContext.REQUEST_SCOPE;
    public static final int SESSION = PageContext.SESSION_SCOPE;
    
    public static final int ID = 0;
    public static final int OPERATION = 1;
    
    /**
     * Initialize the Command. Since commands are instantiated via reflection, the init method is used to run
     * data that would ordinarily be in the constructor.
     * @param cmdName the name of the command
     * @throws CommandException if an error occurs during instantiation
     * @throws IllegalStateException if the command has already been initialized
     */
    public void init(String id, String cmdName) throws CommandException;

    /**
     * Set this Command's servlet context. This is used by commands to get access to shared data that is
     * stored as attributes within the servlet context.
     * @param ctx the servlet context
     */
    public void setContext(ServletContext ctx);

    /**
     * Execute the web Command.
     * @param ctx run-time information needed to execute
     * @throws CommandException if an error occurs
     */
    public void execute(CommandContext ctx) throws CommandException;

    /**
     * Return the name of the command.
     * @return the command name
     */
    public String getName();
    
    /**
     * Return the ID of the command. This is usually set by the command factory.
     * @return the command ID
     */
    public String getID();

    /**
     * Return the roles authorized to execute this command.
     * @return a Collection of role names
     */
    public Collection<String> getRoles();
    
    /**
     * Updates the roles authorized to execute this command. 
     * @param roleNames a List of role names
     * @throws IllegalStateException if the role names have already been set
     */
    void setRoles(Collection<String> roleNames) throws IllegalStateException;
}