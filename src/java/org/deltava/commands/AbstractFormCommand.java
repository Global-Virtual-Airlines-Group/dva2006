// Copyright 2005, 2006, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

/**
 * A class to support form editing/saving web site commands.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public abstract class AbstractFormCommand extends AbstractCommand {

	private enum Operation {
		READ, EDIT, SAVE
	}
	
    /**
     * Returns the operation we wish to perform on the bean.
     */
    private static Operation getOperation(CommandContext ctx) {
        try {
        	String opName = (String) ctx.getCmdParameter(Command.OPERATION, "read");
        	return Operation.valueOf(opName.toUpperCase());
        } catch (Exception e) {
        	return Operation.READ;	
        }
    }
    
    /**
     * Executes the command, and based on the operation calls an implementation method for
     * the particular operation.
     * @param ctx the Command Context
     * @throws CommandException if an unhandled error occurs
     */
    @Override
    public final void execute(CommandContext ctx) throws CommandException {
        switch (getOperation(ctx)) {
        	case SAVE :
        	    execSave(ctx);
        	    break;
        		    
        	case EDIT:
        	    execEdit(ctx);
        	    break;
        		    
        	default :
        	    execRead(ctx);
        }
    }
    
    /**
     * Method called when saving the form.
     * @param ctx the Command Context
     * @throws CommandException if an unhandled error occurs
     */
    protected abstract void execSave(CommandContext ctx) throws CommandException;
    
    /**
     * Method called when editing the form.
     * @param ctx the Command Context
     * @throws CommandException if an unhandled error occurs
     */
    protected abstract void execEdit(CommandContext ctx) throws CommandException;
    
    /**
     * Method called when reading the form.
     * @param ctx the Command Context
     * @throws CommandException if an unhandled error occurs
     */
    protected abstract void execRead(CommandContext ctx) throws CommandException;
}