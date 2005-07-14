<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Schedule - Airlines</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/schedule/header.jsp" %> 
<%@include file="/jsp/schedule/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<view:table className="view" pad="default" space="default" cmd="airlines">

<!-- Table Header Bar -->
<tr class="title">
 <td width="50%">AIRLINE NAME</td>
 <td width="20%">AIRLINE CODE</td>
 <td><el:cmdbutton url="airline" op="edit" label="NEW AIRLINE" /></td>
</tr>

<!-- Table Airline Data -->
<c:forEach var="airline" items="${airlines}">
<view:row entry="${airline}">
 <td class="pri bld"><el:cmd url="airline" linkID="${airline.code}" op="edit">${airline.name}</el:cmd></td>
 <td class="bld">${airline.code}</td>
<c:if test="${airline.active}">
 <td class="ter bld">Airline is currently Active</td>
</c:if>
<c:if test="${!airline.active}">
 <td class="warn bld">Airline is currently Inactive</td>
</c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="3">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</div>
</body>
</html>
