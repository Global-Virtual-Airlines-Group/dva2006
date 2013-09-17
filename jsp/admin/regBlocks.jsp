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
<title><content:airline /> Registration Blocks</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="regblocks">
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:5%">#</td>
 <td style="width:15%">USER NAME</td>
 <td style="width:15%">ADDRESS</td>
 <td style="width:25%">HOSTNAME</td>
 <td class="left">COMMENTS</td>
</tr>

<!-- Table data -->
<c:forEach var="block" items="${viewContext.results}">
<view:row entry="${block}">
 <td><el:cmd url="regblock" link="${block}" className="sec bld"><fmt:int value="${block.ID}" /></el:cmd></td>
 <td class="small"><fmt:text value="${block.firstName}" default="-" /> <fmt:text value="${block.lastName}" default="-" /></td>
 <td><fmt:ip4 address="${block.address}" /> / <fmt:ip4 address="${block.netMask}" /></td>
 <td class="small"><fmt:text value="${block.hostName}" default="-" /></td>
 <td class="left">${block.comments}</td>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar> <el:cmdbutton url="regblock" op="edit" label="NEW REGISTRATION BLOCK" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
