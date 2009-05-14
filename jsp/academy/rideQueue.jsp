<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Academy Check Rides</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" space="default" pad="default" cmd="academyridequeue">
<tr class="title">
 <td colspan="6" class="left caps"><content:airline /> FLIGHT ACADEMY SUBMITTED CHECK RIDES</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="8%">DATE</td>
 <td width="7%">&nbsp;</td>
 <td width="20%">PILOT NAME</td>
 <td width="15%">COURSE NAME</td>
 <td width="10%">AIRCRAFT</td>
 <td class="left">COMMENTS</td>
</tr>
 
<!-- Table View data -->
<c:forEach var="ride" items="${viewContext.results}">
<c:set var="course" value="${courses[ride.courseID]}" scope="page" />
<c:set var="pilot" value="${pilots[ride.pilotID]}" scope="page" />
<tr>
 <td><el:cmd url="checkride" link="${ride}"><fmt:date date="${ride.submittedOn}" fmt="d" /></el:cmd></td>
 <td><el:cmdbutton url="crview" linkID="${ride.hexID}" label="SCORE" /></td>
 <td><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd></td>
 <td><el:cmd url="course" link="${course}">${course.name}</el:cmd></td>
 <td class="sec">${ride.equipmentType}</td>
 <td class="small left">${ride.comments}</td>
</tr>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
