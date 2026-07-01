// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.io.StringWriter;
import java.time.Instant;
import java.time.format.*;

import org.deltava.util.StringUtils;

/**
 * A class to generate CSV-formatted raw schedule entries.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class CSVScheduleFormatter implements ScheduleFormatter {
	
	private final DateTimeFormatter _DF = new DateTimeFormatterBuilder().appendPattern("dd-MMM").toFormatter();
	private final DateTimeFormatter _TF = new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter();

	@Override
	public ScheduleFormat getFormat() {
		return ScheduleFormat.CSV;
	}
	
	@Override
	public String format(ScheduleEntry se) {
		RawScheduleEntry rse = (RawScheduleEntry) se;
		
        StringBuilder buf = new StringBuilder(se.getSource().name());
        buf.append(',');
        buf.append(rse.getLineNumber());
        buf.append(',');
        buf.append(_DF.format(rse.getStartDate()));
        buf.append(',');
        buf.append(_DF.format(rse.getEndDate()));
        buf.append(',');
        buf.append(rse.getDayCodes());
        buf.append(',');
        buf.append(se.getAirline().getCode());
        buf.append(',');
        buf.append(StringUtils.format(se.getFlightNumber(), "#000"));
        buf.append(',');
        buf.append(String.valueOf(se.getLeg()));
        buf.append(',');
        buf.append(se.getEquipmentType());
        buf.append(',');
        buf.append(se.getAirportD().getICAO());
        buf.append(',');
        buf.append(_TF.format(se.getTimeD()));
        buf.append(',');
        buf.append(se.getAirportA().getICAO());
        buf.append(',');
        buf.append(_TF.format(se.getTimeA()));
        buf.append(',');
        buf.append(se.getDistance());
        buf.append(',');
        buf.append(se.getHistoric());
        buf.append(',');
        buf.append(rse.getForceInclude());
        buf.append(',');
        buf.append(se.getAcademy());
        buf.append(',');
        buf.append(rse.getIsUTC());
        buf.append(',');
        buf.append(StringUtils.addCSVQuotes(se.getRemarks()));
        buf.append(',');
        buf.append(StringUtils.addCSVQuotes(rse.getComments()));
        return buf.toString();
	}
	
	@Override
	public String getHeader() {
		StringWriter out = new StringWriter();
		out.write("; Flight Schedule - exported on " + StringUtils.format(Instant.now(), "MM/dd/yyyy HH:mm:ss") + " UTC\n");
		out.write("; SOURCE,LINE,STARTS,ENDS,DAYS,AIRLINE,NUMBER,LEG,EQTYPE,FROM,DTIME,TO,ATIME,DISTANCE,HISTORIC,FORCE,ACADEMY,ISUTC,REMARKS,COMMENTS\n");
		return out.toString();
	}
	
	@Override
	public String getSeparator() {
		return "";
	}
	
	@Override
	public String getFooter() {
		return "";
	}
}