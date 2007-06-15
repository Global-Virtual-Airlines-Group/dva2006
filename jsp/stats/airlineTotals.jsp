<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Airline Totals</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">AIRLINE STATISTICS AS OF <fmt:date date="${effectiveDate}" d="EEEE MMMM dd yyyy" /></td>
</tr>
<tr>
 <td class="label">Total Flights</td>
 <td class="data"><fmt:int className="pri bld" value="${totals.totalLegs}" /> flights / 
 <fmt:int className="pri bld" value="${totals.totalHours}" /> hours / 
 <fmt:int className="pri bld" value="${totals.totalMiles}" /> miles</td>
</tr>
<tr>
 <td class="label">Total Pilots</td>
 <td class="data"><fmt:int value="${totals.totalPilots}" /> total,
 <fmt:int className="pri bld" value="${totals.activePilots}" /> active</td>
</tr>
<tr>
 <td class="label">Online Flights</td>
 <td class="data"><fmt:int className="sec bld" value="${totals.onlineLegs}" /> flights / 
 <fmt:int className="bld" value="${totals.onlineHours}" /> hours / 
 <fmt:int className="bld" value="${totals.onlineMiles}" /> miles</td>
</tr>
<tr>
 <td class="label">ACARS Flights</td>
 <td class="data"><fmt:int className="pri bld" value="${totals.ACARSLegs}" /> flights / 
 <fmt:int className="pri bld" value="${totals.ACARSHours}" /> hours /
 <fmt:int className="pri bld" value="${totals.ACARSMiles}" /> miles</td>
</tr>
<tr>
 <td class="label">per Pilot Average</td>
 <td class="data"><fmt:dec className="bld" value="${totals.totalLegs / totals.totalPilots}" /> flights / 
 <fmt:dec className="bld" value="${totals.totalHours / totals.totalPilots}" /> hours / 
 <fmt:int className="bld" value="${totals.totalMiles / totals.totalPilots}" /> miles</td>
</tr>
<tr>
 <td class="label">per Day Average</td>
 <td class="data"><fmt:dec className="bld" value="${totals.totalLegs / totals.age}" /> flights / 
 <fmt:dec className="bld" value="${totals.totalHours / totals.age}" /> hours / 
 <fmt:int className="bld" value="${totals.totalMiles / totals.age}" /> miles</td>
</tr>
<tr>
 <td class="label">Current Month</td>
 <td class="data"><fmt:int value="${totals.MTDLegs}" /> flights / <fmt:int value="${totals.MTDHours}" />
  hours / <fmt:int value="${totals.MTDMiles}" /> miles</td>
</tr>
<tr>
 <td class="label">Current Year</td>
 <td class="data"><fmt:int value="${totals.YTDLegs}" /> flights / <fmt:int value="${totals.YTDHours}" />
  hours / <fmt:int value="${totals.YTDMiles}" /> miles</td>
</tr>
<tr>
 <td class="label">Database Size</td>
 <td class="data"><fmt:int className="sec bld" value="${totals.DBRows}" /> rows / 
 <fmt:int className="sec bld" value="${totals.DBSize}" /> bytes</td>
</tr>
<tr class="title caps mid">
 <td colspan="2"><content:airline /> STATISTICS COMMENCE <fmt:int value="${totals.age}" /> DAYS AGO</td>
</tr>
</el:table>
<br />
<!-- Database Information Table -->
<el:table className="view" space="default" pad="default">
<tr class="title caps">
 <td width="35%">TABLE NAME</td>
 <td width="15%">ROWS</td>
 <td width="15%">TABLE SIZE</td>
 <td width="15%">INDEX SIZE</td>
 <td>AVG. ROW SIZE</td>
</tr>

<!-- Database Information Data -->
<c:forEach var="tableInfo" items="${tableStatus}">
<tr>
 <td class="pri bld caps">${tableInfo.name}</td>
 <td class="sec bld"><fmt:int value="${tableInfo.rows}" /></td>
 <td class="bld"><fmt:int value="${tableInfo.size}" /> bytes</td>
 <td class="sec bld"><fmt:int value="${tableInfo.indexSize}" /> bytes</td>
 <td><fmt:int value="${tableInfo.averageRowLength}" /> bytes/row</td>
</tr>
</c:forEach>

<!-- Footer Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
