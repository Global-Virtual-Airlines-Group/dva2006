// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.time.*;
import java.sql.Connection;
import java.util.Collection;
import java.util.stream.Collectors;

import org.deltava.beans.finance.AWSCostEntry;

import org.deltava.dao.*;
import org.deltava.dao.http.Compression;
import org.deltava.dao.http.GetAWSCosts;

import org.deltava.taskman.*;

/**
 * A Scheduled Task to download AWS cost data.
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class AWSCostTask extends Task {

	/**
	 * Creates the Task.
	 */
	public AWSCostTask() {
		super("AWS Costs", AWSCostTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Get yesterday's costs
		LocalDate ld = LocalDate.now().minusDays(1);

		try {
			Connection con = ctx.getConnection();
			
			// Check for yesterday's cost data
			GetFinance fdao = new GetFinance(con);
			Collection<AWSCostEntry> entries = fdao.getAWSCosts(ld.getYear(), ld.getMonthValue());
			Collection<Instant> days = entries.stream().map(AWSCostEntry::getDate).collect(Collectors.toSet());
			
			// TODO Figure out how many days to pull to get missing data
			
			// Get the data
			GetAWSCosts cdao = new GetAWSCosts();
			cdao.setConnectTimeout(1000);
			cdao.setReadTimeout(5000);
			cdao.setCompression(Compression.GZIP);
			Collection<AWSCostEntry> data = cdao.getCosts(1);
			
			// Save the data
			ctx.startTX();
			SetFinance fwdao = new SetFinance(con);
			for (AWSCostEntry ce : data)
				fwdao.write(ce);
			
			ctx.commitTX();
			log.info("Write {} AWS cost entries", Integer.valueOf(data.size()));
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}
		
		log.info("Complete");
	}
}