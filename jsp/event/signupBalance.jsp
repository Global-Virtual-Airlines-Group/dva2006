<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Online Event Signup Balance - ${event.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.routeID, t:'Route to move Pilots to'});
golgotha.form.validate({f:f.signupID, min:1, t:'Pilots to move'});
golgotha.form.submit(f);
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
<el:form action="eventbalance.do" link="${event}" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">EVENT SIGNUP REBALANCING FOR ${event.name}</td>
</tr>
<c:forEach var="route" items="${event.routes}">
<c:set var="routeSignups" value="${fn:routeSignups(event, route.routeID)}" scope="page" />
<tr class="title caps">
 <td colspan="2">ROUTE #<fmt:int value="${route.routeID}" /><c:if test="${!empty route.name}"> - ${route.name}</c:if>
 (<fmt:int value="${fn:sizeof(routeSignups)}" /> SIGNED UP PILOTS)</td>
</tr>
<c:forEach var="signup" items="${routeSignups}">
<c:set var="loc" value="${userLocs[signup.pilotID]}" scope="page" />
<c:set var="pilot" value="${pilots[loc.ID]}" scope="page" />
<tr>
 <td class="label mid"><el:box name="signupID" value="${pilot.hexID}" label="" /></td>
 <td class="data"><el:profile location="${pilotLoc}"><span class="pri bld">${pilot.name}</span></el:profile>
<c:if test="${!empty pilot.pilotCode}"> (<span class="sec bld">${pilot.pilotCode})</span></c:if> ${pilot.rank.name}, ${pilot.equipmentType}</td>
</tr>
</c:forEach>
<c:if test="${empty routeSignups}">
<tr>
 <td colspan="2" class="pri bld mid">NO PILOTS ARE SIGNED UP FOR THIS ONLINE EVENT ROUTE</td>
</tr>
</c:if>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><span style="color:#ffffff" class="bld">MOVE TO ROUTE</span> <el:combo name="routeID" idx="*" firstEntry="-" options="${event.routes}" />
 <el:button ID="SaveButton" type="submit" label="MOVE" /></td>
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
