<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Search</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics />
<content:js name="common" />
<content:cspHeader />
<script async>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
const hasName = (f.firstName.value.length > 2) || (f.lastName.value.length > 2);
const hasCode = (f.pilotCode.value.length > 1);
const hasEMail = ((f.eMail) && (f.eMail.value.length > 5));
let hasRating = true;
try {
	golgotha.form.validate({f:f.ratings, min:1, t:''});
} catch (e) {
	hasRating = false;
}

if (hasName || hasCode || hasEMail || hasRating) {
	golgotha.form.submit(f);
	return true;
}
	
throw new golgotha.event.ValidationError('Please provide a First or Last Name, Pilot Code, or an e-mail address.', f.firstName);
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
<el:form action="pilotsearch.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<!-- Search Criteria -->
<el:table className="form">
<tr class="title caps">
 <td colspan="2">PILOT SEARCH</td>
</tr>
<tr>
 <td class="label">Pilot Code</td>
 <td class="data"><el:text name="pilotCode" idx="*" size="7" max="8" value="${param.pilotCode}" /><br />
<span class="small"><b>NOTE:</b> Pilot Code searches override Name / E-Mail criteria.</span></td>
</tr>
<tr>
 <td class="label">First / Last Names</td>
 <td class="data"><el:text name="firstName" idx="*" size="12" max="24" value="${param.firstName}" />
&nbsp;<el:text name="lastName" idx="*" size="18" max="36" value="${param.lastName}" /></td>
</tr>
<content:filter roles="HR,Operations,Examination,PIREP">
<tr>
 <td class="label">E-Mail Address</td>
 <td class="data"><el:addr name="eMail" idx="*" size="24" max="64" value="${param.eMail}" /></td>
</tr>
<content:filter roles="HR,Operations">
<tr>
 <td class="label top">Equipment Ratings</td>
 <td class="data"><el:check name="ratings" idx="*" width="95" newLine="true" className="small" checked="${ratings}" options="${allEQ}" /></td>
 </tr>
</content:filter>
</content:filter>
<tr>
 <td class="label">Maximum Results</td>
 <td class="data"><el:text name="maxResults" idx="*" size="2" max="2" value="${maxResults}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data sec small"><el:box name="exactMatch" idx="*" value="true" label="Exact Matches only" checked="${param.exactMatch == '1'}" />
<content:filter roles="HR"><br />
<el:box name="allAirlines" idx="*" value="true" label="Search all Airlines" /></content:filter></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="PILOT SEARCH" /></td>
</tr>
</el:table>

<!-- Search Results -->
<c:if test="${!noResults}">
<view:table cmd="">
<tr class="title caps">
 <td colspan="8" class="left">SEARCH RESULTS</td>
</tr>
<c:choose>
<c:when test="${empty results}">
<!-- No Pilots Found -->
<tr>
 <td colspan="8" class="pri bld left">No Pilots matching your search criteria were found.</td>
</tr>
</c:when>
<c:otherwise>
<!-- Table Header Bar-->
<tr class="title">
 <td colspan="2" width="25%">&nbsp;</td>
 <td style="width:20%">PILOT NAME</td>
 <td style="width:10%">PILOT ID</td>
 <td style="width:10%">JOINED ON</td>
 <td style="width:10%">LAST FLIGHT</td>
 <td style="width:10%">FLIGHTS</td>
 <td>HOURS</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${results}">
<c:set var="access" value="${accessMap[pilot.ID]}" scope="page" />
<view:row entry="${pilot}">
<c:set var="cspan" value="2" scope="page" />
<content:filter roles="HR">
<c:if test="${access.canActivate || access.canChangeSignature}">
 <td><el:cmdbutton url="cmdlog" link="${pilot}" label="VIEW LOG" /></td>
<c:set var="cspan" value="1" scope="page" />
</c:if>
</content:filter>
<c:choose>
<c:when test="${access.canActivate}">
 <td colspan="${cspan}"><el:cmdbutton url="activate" link="${pilot}" label="ACTIVATE PILOT" /></td>
</c:when>
<c:when test="${access.canChangeSignature}">
 <td colspan="${cspan}"><el:cmdbutton url="sigupdate" link="${pilot}" label="EDIT SIGNATURE" /></td>
</c:when>
<c:otherwise>
 <td colspan="${cspan}">&nbsp;</td>
</c:otherwise>
</c:choose>
 <td class="pri bld"><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td class="small"><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
<c:if test="${pilot.legs > 0}">
 <td class="small"><fmt:date fmt="d" date="${pilot.lastFlight}" /></td>
 <td class="bld"><el:cmd url="logbook" op="log" link="${pilot}"><fmt:int value="${pilot.legs}" /></el:cmd></td>
 <td class="pri bld"><el:cmd url="logbook" op="log" link="${pilot}"><fmt:dec value="${pilot.hours}" /></el:cmd></td>
</c:if>
<c:if test="${pilot.legs == 0}">
 <td colspan="3" class="bld">NO FLIGHTS LOGGED</td>
</c:if>
</view:row>
</c:forEach>

<!-- Table Legend Bar -->
<tr class="title">
 <td colspan="8"><view:legend width="100" labels="Active,Inactive,Retired,On Leave,Transferred,Suspended" classes=" ,opt2,opt3,warn,opt1,err" /></td>
</tr>
</c:otherwise>
</c:choose>
</view:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
