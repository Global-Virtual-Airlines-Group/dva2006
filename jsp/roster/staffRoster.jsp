<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Staff Roster</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<view:table className="view" pad="default" space="default" cmd="staff">
<tr class="title">
 <td colspan="4" class="left">STAFF ROSTER</td>
</tr>
<!-- Table Header Bar-->
<tr class="title">
 <td width="15%">NAME</td>
 <td width="18%">DUTIES</td>
 <td width="20%">E-MAIL</td>
 <td>BIOGRAPHY</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="staff" items="${staffRoster}">
<tr>
 <td class="pri bld">${staff.firstName} ${staff.lastName}</td>
 <td class="sec bld">${staff.title}</td>
 <td><el:link className="small" url="mailto:${staff.EMail}">${staff.EMail}</el:link></td>
 <td class="small left">${staff.body}</td>
</tr>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="4">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</div>
</body>
</html>
