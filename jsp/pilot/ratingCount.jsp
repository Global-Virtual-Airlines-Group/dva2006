<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Rating Counts</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.filter = function(cb) { return document.forms[0].submit(); };
golgotha.local.validate = function(f) { golgotha.form.comboSet(f.acType); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="ratingCount.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> Rating Counts</td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="acType" idx="*" required="true" firstEntry="[ SELECT AIRCRAFT ]" options="${allAircraft}" value="${aircraft}" onChange="void golgotha.local.filter(this)" /></td>
</tr>
<c:if test="${doSearch}">
<tr>
 <td class="label">Equipment Programs</td>
 <td class="data">The following equipment programs grant type ratings for the ${aircraft}: <span class="pri bld"><c:forEach var="eqType" items="${eqPrograms}" varStatus="st">${eqType.name}<c:if test="${!st.isLast()}">, </c:if></c:forEach></span></td>
</tr>
<c:if test="${empty ratedIDs && empty qualifyIDs}">
<tr class="title caps mid">
 <td colspan="2">No Pilots are currently rated to fly the ${param.acType}</td>
</tr>
</c:if>
<c:if test="${!empty ratedIDs || !empty qualifyIDs}">
<tr class="title caps">
 <td colspan="2">RATED / QUALIFIED PILOTS FOUND</td>
</tr>
<c:choose>
<c:when test="${!empty ratedPilots}">
<tr>
 <td colspan="2">The following <content:airline /> Pilots are rated to fly the <span class="pri bld">${aircraft.name}</span>:<br />
<br />
<c:forEach var="pilot" items="${ratedPilots}" varStatus="st">
${pilot.rank.name}&nbsp;<el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd><c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if><c:if test="${!st.isLast()}"><br /></c:if>
</c:forEach></td>
</tr>
</c:when>
<c:when test="${empty ratedPilots}">
<tr class="pri bld">
 <td colspan="2" class="mid"><fmt:int value="${ratedIDs.size()}" />&nbsp;<content:airline /> Pilots are rated in the ${aircraft.name}.</td>
</tr>
</c:when>
</c:choose>
<c:choose>
<c:when test="${!empty notYetRatedPilots}">
<tr class="mid">
 <td colspan="2"><span class="sec bld ita"><fmt:int value="${notYetRatedIDs.size()}" />&nbsp;<content:airline /> Pilots qualify for ratings, but are NOT rated in the <span class="pri">${aircraft.name}</span>:</span><br />
 <br />
 <c:forEach var="pilot" items="${notYetRatedPilots}" varStatus="st">
${pilot.rank.name}&nbsp;<el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd><c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if><c:if test="${!st.isLast()}"><br /></c:if>
</c:forEach></td>
</tr>
</c:when>
</c:choose>
<c:if test="${empty notYetRatedPilots}">
<tr class="pri bld">
 <td colspan="2" class="mid"><fmt:int value="${notYetRatedIDs.size()}" />&nbsp;<content:airline /> Pilots qualify for ratings, but are NOT rated in the ${aircraft.name}. ${notYetRatedIDs}</td>
</tr>
</c:if>
</c:if>
</c:if>
<tr class="title">
 <td colspan="2">&nbsp;</td>
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
