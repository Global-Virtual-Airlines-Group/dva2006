<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Cost Ledger Updated</title>
<content:css name="main" />
<content:js name="common" />
<content:googleAnalytics />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isCreated}">
<div class="updateHdr">Cost Ledger Created</div>
<br>
A new entry has been added to the <content:airline /> Cost Ledger and saved to the database.<br>
</c:when>
<c:otherwise>
<div class="updateHdr">Cost Ledger Updated</div>
<br>
An entry has been updated in the <content:airline /> Cost Ledger and saved to the database.<br>
</c:otherwise>
</c:choose>
<br>
To view this <content:airline /> Cost Ledger entry, <el:cmd url="cost" link="${entry}" className="sec bld">Click Here</el:cmd>.<br>
To view the <content:airline /> Cost Ledger, <el:cmd url="costledger" className="sec bld">Click Here</el:cmd>.<br>
To view the <content:airline /> AWS Cost Ledger, <el:cmd url="awsledger" className="sec bld">Click Here</el:cmd>.<br>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
