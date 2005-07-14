package org.deltava.commands;

/**
 * A class to support form editing/saving web site commands.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractFormCommand extends AbstractCommand {

    public static final int READ = 0;
    public static final int EDIT = 1;
    public static final int SAVE = 2;
    
    public static final String[] OPS = {"read", "edit", "save"};
    
    /**
     * Returns the operation we wish to perform on the bean.
     */
    private int getOperation(CommandContext ctx) {
        String opName = (String) ctx.getCmdParameter(Command.OPERATION, "read");
        for (int x = 0; x < AbstractFormCommand.OPS.length; x++) {
            if (AbstractFormCommand.OPS[x].equalsIgnoreCase(opName))
                return x;
        }
        
        // default is read
        return AbstractFormCommand.READ;
    }
    
    /**
     * Executes the command, and based on the operation calls an implementation method for
     * the particular operation.
     * @param ctx the Command Context
     * @throws CommandException if an unhandled error occurs
     */
    public final void execute(CommandContext ctx) throws CommandException {
        int cmdOp = getOperation(ctx);
        switch (cmdOp) {
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
    
    protected abstract void execSave(CommandContext ctx) throws CommandException;
    
    protected abstract void execEdit(CommandContext ctx) throws CommandException;
    
    protected abstract void execRead(CommandContext ctx) throws CommandException;
}