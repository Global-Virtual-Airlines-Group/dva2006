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
<title><content:airline /> Fleet Library Installers</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<view:table className="view" pad="default" space="default" cmd="doclibrary">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="25%">TITLE</td>
 <td width="10%">CODE</td>
 <td width="10%">SIZE</td>
 <td width="10%">VERSION</td>
 <td width="10%"><el:cmdbutton url="libedit" op="fleet" label="NEW INSTALLER" /></td>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="installer" items="${fleet}">
<view:row entry="${installer}">
 <td class="pri bld"><el:cmd url="libedit" linkID="${installer.fileName}" op="fleet">${installer.name}</el:cmd></td>
 <td>${installer.code}</td>
 <td class="sec small"><fmt:int value="${installer.size}" /> bytes</td>
 <td class="bld">${installer.version}</td>
 <td class="small left" colspan="2"><fmt:text value="${installer.description}" /></td>
</view:row>
</c:forEach>

<!-- Legend Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="85" labels="OK,Missing" classes=" ,warn" /></td>
</tr>
</view:table>
<content:copyright />
</div>
</body>
</html>
