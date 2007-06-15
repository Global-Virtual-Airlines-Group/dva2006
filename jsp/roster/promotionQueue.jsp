<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Promotion Queue</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view" pad="default" space="default">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">&nbsp;</td>
 <td width="10%">PILOT CODE</td>
 <td width="25%">PILOT NAME</td>
 <td width="10%">EQUIPMENT</td>
 <td width="10%">RANK</td>
 <td width="8%">FLIGHTS</td>
 <td width="8%">HOURS</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${queue}">
<view:row entry="${pilot}">
 <td><el:cmdbutton url="promote" link="${pilot}" label="PROMOTE" /></td>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}" op="edit" className="bld">${pilot.name}</el:cmd></td>
 <td class="sec bld">${pilot.equipmentType}</td>
 <td class="bld">${pilot.rank}</td>
 <td><fmt:int value="${pilot.legs}" /></td>
 <td><fmt:int value="${pilot.hours}" /></td>
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="8">&nbsp;</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
