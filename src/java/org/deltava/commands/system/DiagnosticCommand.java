package org.deltava.commands.system;

import org.deltava.commands.*;

import org.deltava.jdbc.ConnectionPool;
import org.deltava.taskman.TaskScheduler;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display diagnostic infomration.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DiagnosticCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
    public void execute(CommandContext ctx) throws CommandException {

       // Get the Connection Pool data
       ConnectionPool cPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
       ctx.setAttribute("jdbcPoolInfo", (cPool == null) ? null : cPool.getPoolInfo(), REQUEST);

       // Get the Task Scheduler data
       TaskScheduler tSched = (TaskScheduler) SystemData.getObject(SystemData.TASK_POOL);
       ctx.setAttribute("taskInfo", (tSched == null) ? null : tSched.getTaskInfo(), REQUEST);
       
       // Get Virtual Machine properties
       Runtime rt = Runtime.getRuntime();
       ctx.setAttribute("cpuCount", new Integer(rt.availableProcessors()), REQUEST);
       ctx.setAttribute("totalMemory", new Long(rt.totalMemory()), REQUEST);
       ctx.setAttribute("maxMemory", new Long(rt.maxMemory()), REQUEST);
       
       // Get Servlet context properties
       ctx.setAttribute("serverInfo", _ctx.getServerInfo(), REQUEST);
       ctx.setAttribute("servletContextName", _ctx.getServletContextName(), REQUEST);
       ctx.setAttribute("majorServletAPI", new Integer(_ctx.getMajorVersion()), REQUEST);
       ctx.setAttribute("minorServletAPI", new Integer(_ctx.getMinorVersion()), REQUEST);
       
       // Get System properties
       ctx.setAttribute("sys", System.getProperties(), REQUEST);
       
       // Forward to the JSP
       CommandResult result = ctx.getResult();
       result.setURL("/jsp/admin/diagnostics.jsp");
       result.setSuccess(true);
    }
}