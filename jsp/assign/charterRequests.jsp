<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Charter Flight Requests<c:if test="${!empty pilot}"> - ${pilot.name}</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:attr attr="showAuthors" roles="HR,Operations,PIREP" value="true" />
<c:if test="${showAuthors}">
<script>
golgotha.local.updateAuthor = function(cb) {
	self.location = (cb.selectedIndex < 1) ? 'chreqs.do' : ('chreqs.do?id=' + golgotha.form.getCombo(cb));
	return true;
};
</script></c:if>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="chreqs.do" method="get" validate="return false">
<view:table cmd="chreqs">
<!-- Table Title Bar -->
<tr class="title caps">
 <td colspan="2" class="left"><span class="nophone"><content:airline />&nbsp;</span>CHARTER FLIGHT REQUESTS<c:if test="${!empty pilot}"><span class="nophone"> - ${pilot.name} (${pilot.pilotCode})</span></c:if></td>
 <td class="nophone">&nbsp;</td>
 <td class="nophone"><el:cmdbutton url="chreq" label="NEW CHARTER REQUEST" op="edit" /></td>
<c:choose><c:when test="${showAuthors}"><td class="right"><el:combo name="author" size="1" firstEntry="[ SELECT PILOT ]" value="${pilot}" options="${authors}" onChange="void golgotha.local.updateAuthor(this)" /></td></c:when>
<c:otherwise><td>&nbsp;</td></c:otherwise></c:choose>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:14%">PILOT</td>
 <td class="nophone" style="width:37%">ROUTE</td>
 <td style="width:10%">AIRCRAFT</td>
 <td class="nophone" style="width:15%">REQUESTED</td>
 <td>STATUS</td>
</tr>

<!-- Table Request Data -->
<c:forEach var="chreq" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[chreq.authorID]}" scope="page" />
<c:set var="disposedBy" value="${pilots[chreq.disposalID]}" scope="page" />
<view:row entry="${chreq}">
 <td class="pri bld"><el:cmd url="chreq" link="${chreq}">${pilot.name}</el:cmd></td>
 <td class="small nophone">${chreq.airportD.name} (<fmt:airport airport="${chreq.airportD}" />) - ${chreq.airportA.name} (<fmt:airport airport="${chreq.airportA}" />)</td>
 <td class="sec">${chreq.equipmentType}</td>
 <td class="nophone"><fmt:date date="${chreq.createdOn}" t="HH:mm" /></td>
<c:if test="${empty chreq.disposedOn}">
 <td>-</td>
</c:if>
<c:if test="${!empty chreq.disposedOn}">
 <td><span class="bld">${disposedBy.name}</span> on <fmt:date date="${chreq.disposedOn}" t="HH:mm" /></td>
</c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar Row -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar><view:legend width="100" classes=" opt1,warn, " labels="Pending,Rejected,Approved" /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
