<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Fleet Library Installers</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="doclibrary">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:25%">TITLE</td>
 <td class="nophone" style="width:10%">CODE</td>
 <td class="nophone" style="width:10%">SIZE</td>
 <td class="nophone" style="width:10%">VERSION</td>
 <td style="width:15%"><el:cmdbutton url="fleetlib" op="edit" label="NEW INSTALLER" /></td>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="installer" items="${fleet}">
<view:row entry="${installer}">
 <td class="pri bld"><el:cmd url="fleetlib" linkID="${installer.fileName}" op="edit">${installer.name}</el:cmd></td>
 <td class="nophone">${installer.code}</td>
 <td class="sec small nopgone"><fmt:int value="${installer.size}" /> bytes</td>
 <td class="bld nophone">${installer.version}</td>
 <td class="small left" colspan="2"><fmt:text value="${installer.description}" /></td>
</view:row>
</c:forEach>

<!-- Legend Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="85" labels="OK,Missing" classes=" ,warn" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
