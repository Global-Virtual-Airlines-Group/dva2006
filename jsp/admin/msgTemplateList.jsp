<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Message Templates</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<view:table className="view" pad="default" space="default" cmd="msgtemplates">
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="20%">TEMPLATE NAME</td>
 <td width="30%">MESSAGE SUBJECT</td>
 <td>DESCRIPTION</td>
</tr>

<!-- Table data -->
<c:forEach var="template" items="${templates}">
<view:row entry="${template}">
 <td><el:cmd url="msgtemplate" className="pri bld" linkID="${template.name}">${template.name}</el:cmd></td>
 <td class="sec">${template.subject}</td>
 <td class="left">${template.description}</td>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="3">&nbsp;
<c:if test="${access.canCreate}">
<el:cmdbutton url="msgtemplate" op="new" label="NEW MESSAGE TEMPLATE" />
</c:if>
 </td>
</tr>
</view:table>
<content:copyright />
</div>
</body>
</html>
