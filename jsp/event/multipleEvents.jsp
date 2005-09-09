ww<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Events</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/event/header.jsp" %> 
<%@ include file="/jsp/event/sideMenu.jsp" %>
<!-- Main Body Frame -->
<div id="main">
<el:table className="view" pad="default" space="default">
<tr class="title caps">
 <td class="left" colspan="x">MULTIPLE <content:airline /> ONLINE EVENTS</td>
</tr>
<tr>
 <td class="pri bld left"><fmt:int value="${fn:sizeof(futureEvents)}" /> <content:airline /> 
Online Events have currently been scheduled, and are listed below. Please click on one of these
Online Event profiles to learn more about this <content:airline /> Event.<td/>
</tr>

<!-- Event View data -->
<c:forEach var="event" items="${futureEvents}">


</c:forEach>
