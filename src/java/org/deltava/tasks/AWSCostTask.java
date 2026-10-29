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
import org.deltava.util.StringUtils;

/**
 * A Scheduled Task to download AWS cost data.
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class AWSCostTask extends Task {
	
	private static final int MAX_DAYS = 14;

	/**
	 * Creates the Task.
	 */
	public AWSCostTask() {
		super("AWS Costs", AWSCostTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		LocalDate today = LocalDate.now();
		LocalDate ld = today.minusDays(1);
		try {
			Connection con = ctx.getConnection();
			
			// Check for yesterday's cost data
			GetFinance fdao = new GetFinance(con);
			Collection<AWSCostEntry> entries = fdao.getAWSCosts(ld.getYear(), ld.getMonthValue());
			Collection<LocalDate> days = entries.stream().map(AWSCostEntry::getDate).map(i -> LocalDate.ofInstant(i, ZoneOffset.UTC)).collect(Collectors.toSet());
			log.info("Loaded data for {}", days);

			// Figure out how many days to pull to get missing data
			int daysBack = 0;
			for (int x = 1; x <= MAX_DAYS; x++) {
				LocalDate ld2 = today.minusDays(x);
				if (!days.contains(ld2)) {
					log.info("No data for {}", ld2);
					daysBack = x;
					if (x > 1)
						log.warn("Fetching AWS Costs for {}", StringUtils.format(ld2, "MM/dd/yyyy"));
				}
			}
			
			// Get the data
			log.info("Retrieving {} days of AWS costs", Integer.valueOf(daysBack));
			if (daysBack == 0) return;
			
			GetAWSCosts cdao = new GetAWSCosts();
			cdao.setConnectTimeout(1000);
			cdao.setReadTimeout(5000);
			cdao.setCompression(Compression.GZIP);
			Collection<AWSCostEntry> data = cdao.getCosts(daysBack);
			
			// Filter out costs for days we already have
			data.removeIf(ce -> days.contains(LocalDate.ofInstant(ce.getDate(), ZoneOffset.UTC)));
			data.stream().filter(ce -> StringUtils.isEmpty(ce.getDescription())).forEach(ce -> ce.setDescription(String.format("Imported on %s", StringUtils.format(today, "MM/dd/yyyy"))));
			
			// Save the data
			ctx.startTX();
			SetFinance fwdao = new SetFinance(con);
			for (AWSCostEntry ce : data)
				fwdao.write(ce);
			
			ctx.commitTX();
			log.info("Wrote {} AWS cost entries", Integer.valueOf(data.size()));
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}
		
		log.info("Complete");
	}
}