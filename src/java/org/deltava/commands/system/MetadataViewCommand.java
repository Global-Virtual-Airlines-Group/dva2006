// Copyright 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.Map;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view metadata.
 * @author Luke
 * @version 7.0
 * @since 5.1
 */

public class MetadataViewCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<String> vctxt = initView(ctx, String.class);
		try {
			GetMetadata mddao = new GetMetadata(ctx.getConnection());
			mddao.setQueryStart(vctxt.getStart());
			mddao.setQueryMax(vctxt.getCount());
			
			// Save in view cotnext so pageUp/pageDn tags work
			Map<String, String> results = mddao.getAll();
			ctx.setAttribute("data", results, REQUEST);
			vctxt.setResults(results.values());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/metadata.jsp");
		result.setSuccess(true);
	}
}