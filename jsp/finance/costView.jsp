<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Cost Entry - ${entry.category}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:googleAnalytics />
<content:js name="common" />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">COST LEDGER ENTRY</td>
</tr>
<tr>
	<td class="label">Category</td>
	<td class="data pri bld">${entry.category}</td>
</tr>
<tr>
	<td class="label">Date</td>
	<td class="data sec bld"><fmt:date date="${entry.date}" fmt="d" /></td>
</tr>
<tr>
	<td class="label">Amount</td>
	<td class="data"><fmt:dec value="${entry.amount / 100.0}" fmt="$#,##0.00" /> USD</td>
</tr>
<tr>
	<td class="label">Description</td>
	<td class="data"><fmt:msg value="${entry.description}" /></td>
</tr>
<tr>
	<td class="label">Created by</td>
	<td class="data bld">${author.name} (${author.pilotCode})</td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="cost" link="${entry}" label="EDIT COST LEDGER ENTRY" /></td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
