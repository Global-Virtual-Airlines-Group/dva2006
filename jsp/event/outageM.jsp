<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_calendar.tld" prefix="calendar" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Online Data Feed Outages</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="calendar" />
<content:expire expires="30" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script async>
golgotha.local.switchType = function(combo) {
	self.location = '/networkoutages.do?op=' + escape(golgotha.form.getCombo(combo)) + '&startDate=<fmt:date fmt="d" d="MM/dd/yyyy" date="${startDate}" />';
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="networkoutages.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td style="width:80%" class="caps">ONLINE DATA FEED OUTAGES - <fmt:date fmt="d" date="${startDate}" d="MMMM yyyy" /></td>
 <td class="right">CALENDAR TYPE <el:combo name="op" size="1" idx="*" options="${typeOptions}" value="30" onChange="void golgotha.local.switchType(this)" /></td>
</tr>
</el:table>
<div class="mid">
<calendar:month date="cDate" startDate="${startDate}" entries="${outages}" topBarClass="dayHdr"	dayBarClass="dayHdr" tableClass="calendar" contentClass="contentM" scrollClass="scroll" cmd="networkoutages">
<calendar:entry name="outage"><span class="pri bld">${outage.network}</span><br />
<span class="small"><fmt:date fmt="t" t="HH:mm" date="${outage.startTime}" /> - <fmt:date fmt="t" t="HH:mm" date="${outage.endTime}" /><br />
(<fmt:duration duration="${outage.duration}" t="HH:mm" />)</span>
<calendar:spacer><hr /></calendar:spacer>
</calendar:entry>
<calendar:empty>-</calendar:empty>
</calendar:month>
</div>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
