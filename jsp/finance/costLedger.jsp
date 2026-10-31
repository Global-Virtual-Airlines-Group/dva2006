<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /><c:if test="${isAWS}"> AWS</c:if> Cost Ledger</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:js name="common" />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
<script nonce="${contentSecurity.nonce}">
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="cmdName" value="${isAWS ? 'aws' : 'cost'}ledger" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="${cmdName}.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<view:table cmd="${cmdName}">
<tr class="title caps">
 <td colspan="6" class="left"><c:if test="${isAWS}">AWS&nbsp;</c:if>COST LEDGER</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:15%">DATE</td>
 <td style="width:30%">${isAWS ? 'SERVICE' : 'CATEGORY'}</td>
 <td style="width:25%">AUTHOR</td>
 <td style="width:15%">AMOUNT</td>
 <td class="left">DESCRIPTION</td>
 <td><el:cmd url="cost">NEW COST ENTRY</el:cmd></td>
</tr>

<!-- Table Ledger Data -->
<c:forEach var="ce" items="${viewContext.results}">
<c:set var="author" value="${authors[ce.authorID]}" scope="page" />
<view:row entry="${ce}">
 <td class="pri bld"><fmt:date date="${ce.date}" fmt="d" /></td>
 <td>${isAWS ? ce.service : ce.category}</td>
 <td class="sec bld">${(ce.authorID == 0) ? 'SYSTEM' : author.name}</td>
 <td class="pri bld"><fmt:dec value="${ce.amount / 100.0}" fmt="$##0.00" /></td>
 <td colspan="2" class="left">${ce.description}</td>
</view:row>
</c:forEach>

<!-- Table Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
</tr>
</view:table>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
