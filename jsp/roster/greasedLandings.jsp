<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Greaser Club</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:table className="view" pad="default" space="default">
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="5%">#</td>
 <td width="20%">PILOT NAME</td>
 <td width="10%">DATE</td>
 <td width="15%">EQUIPMENT</td>
 <td width="10%">TOUCHDOWN</td>
 <td width="10%">FLIGHT NUMBER</td>
 <td>AIRPORTS</td>
</tr>

<!-- Table Flight Report Data -->
<c:set var="entryNumber" value="0" scope="request" />
<c:forEach var="pirep" items="${pireps}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
<tr>
 <td class="sec bld">${entryNumber}</td>
 <td>${pirep.firstName} ${pirep.lastName}</td>
 <td class="pri bld"><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td><el:cmd className="bld" url="pirep" linkID="0x${pirep.ID}">${pirep.flightCode}</el:cmd></td>
 <td class="sec">${pirep.equipmentType}</td>
 <td class="bld"><fmt:int value="${pirep.landingVSpeed}" /> ft/min</td>
 <td class="small">${pirep.airportD.name} - ${pirep.airportA.name}</td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7">&nbsp;</td>
</tr>
</el:table>
<br />
<content:copyright />
</div>
</body>
</html>
