<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Landing Statistics</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.update = function() { return document.forms[0].submit(); };
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.legCount, min:5, t:'Minimum Number of Landings'});
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="landingstats.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
The members of <content:airline /> are a skilled group - and they can prove it. Below is a list of pilots who consistently achieve the smoothest landings. This list is ordered by both average touchdown speed (which  
counts for 30% of the ordering) and the standard deviation of those speeds (20%), the distance from the runway threshold (30% if available) and the standard deviation of those distances (the remaining 20%).<br />
<br />
<el:table className="view">
<!-- Table top Header bar -->
<tr class="title">
 <td class="left caps" colspan="3"><content:airline /> LANDING STATISTICS</td>
 <td class="right" colspan="6">TOP <el:text name="viewCount" idx="*" size="2" max ="3" value="${viewContext.count}" /> PILOTS WITH A MINIMUM OF <el:text name="legCount" idx="*" size="2" max="3" value="${legCount}" /> FLIGHTS
 WITHIN <el:combo name="days" idx="*" size="1" options="${dateFilter}" value="${dayFilter}" /> IN <el:combo name="eqType" idx="*" size="1" options="${eqTypes}" value="${param.eqType}" /><el:button type="submit" label="GO" /></td>
</tr>
<c:if test="${!empty viewContext.results}">
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:5%">#</td>
 <td style="width:20%">PILOT NAME</td>
 <td style="width:16%">RANK</td>
 <td style="width:7%">FLIGHTS</td>
 <td style="width:7%">HOURS</td>
 <td style="width:14%">AVG. SPEED</td>
 <td style="width:10%">STD. DEVIATION</td>
 <td style="width:10%">AVG. DISTANCE</td>
 <td>STD. DEVIATION</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[entry.ID]}" scope="page" />
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr>
 <td class="sec bld">${entryNumber}</td>
 <td class="pri bld"><content:pilotName pilot="${pilot}">${pilot.name}</content:pilotName></td>
 <td>${pilot.rank.name}, ${pilot.equipmentType}</td>
 <td><fmt:int value="${entry.legs}" /></td>
 <td><fmt:dec value="${entry.hours}" /></td>
 <td class="pri bld"><fmt:dec value="${entry.averageSpeed}" fmt="#0.00" /> ft/min</td>
 <td class="sec"><fmt:dec value="${entry.stdDeviation}" fmt="#0.00" /> ft/min</td>
<c:choose>
<c:when test="${entry.distanceStdDeviation < 1}">
 <td colspan="2">N / A</td>
</c:when>
<c:otherwise>
 <td class="bld"><fmt:dec value="${entry.averageDistance}" fmt="#0" /> ft</td>
 <td><fmt:dec value="${entry.distanceStdDeviation}" fmt="#0.0" /> ft</td>
</c:otherwise>
</c:choose>
</tr>
</c:forEach>
</c:if>
<c:if test="${empty viewContext.results}">
<tr>
 <td colspan="9" class="pri bld mid caps">No <content:airline /> Pilots meeting your criteria were found</td>
</tr>
</c:if>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="9">&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
