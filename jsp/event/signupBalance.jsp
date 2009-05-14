<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Online Event Signup Balance - ${event.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.routeID, 'Route to move Pilots to')) return false;
if (!validateCheckBox(form.signupID, 1, 'Pilots to move')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/event/header.jspf" %> 
<%@ include file="/jsp/event/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="eventbalance.do" link="${event}" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
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
<c:if test="${!empty pilot.pilotCode}"> (<span class="sec bld">${pilot.pilotCode})</span></c:if> ${pilot.rank}, ${pilot.equipmentType}</td>
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
<el:table className="bar" space="default" pad="default">
<tr>
 <td><span style="color:#ffffff" class="bld">MOVE TO ROUTE</span> <el:combo name="routeID" idx="*" firstEntry="-" options="${event.routes}" />
 <el:button ID="SaveButton" type="submit" className="BUTTON" label="MOVE" /></td>
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
