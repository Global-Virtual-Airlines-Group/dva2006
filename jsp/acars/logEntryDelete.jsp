<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>ACARS Log Entry Removal</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<c:if test="${infoDelete}">
<div class="updateHdr">ACARS Flight Log Entry Removed</div>
<br />
The ACARS log entry for Flight ID <fmt:int value="${info.ID}" /> has been successfully removed from 
the database. All Position reports for this Flight have also been removed.<br />
<br />
To return to the list of empty ACARS Flight Log entries, <el:cmd url="acarsempty" linkID="info" className="sec bld">Click here</el:cmd>.<br />
</c:if>
<c:if test="${pirep}">
<div class="updateHdr">Cannot remove ACARS Flight Log Entry</div>
<br />
This ACARS Flight has an associated Flight Report, and therefore cannot be removed from the ACARS 
log database.<br />
<br />
To view this Flight Report, <el:cmd url="pirep" linkID="0x${pirep.ID}" className="sec bld">Click here</el:cmd>.<br />
</c:if>
<c:if test="${conDelete}">
<div class="updateHdr">ACARS Connection Log Entry Removed</div>
<br />
The ACARS Connection log entry for Connection <fmt:hex value="${con.ID}" /> has been successfully 
removed from the database. All Text Messages for this Connection have also been removed.<br />
<br />
To return to the list of empty ACARS Connection Log entries, <el:cmd url="acarsempty" linkID="con" className="sec bld">Click here</el:cmd>.<br />
</c:if>
<c:if test="${!conDelete}">
<div class="updateHdr">Cannot remove ACARS Connection Log Entry</div>
<br />
This ACARS Connection has an associated Flight Information entry, and therefore cannot be removed from 
the ACARS log database. The Flight Information entry must be removed first.<br />
</c:if>
<c:if test="${!empty info}">
To view this flight's ACARS log entry, <el:cmd url="acarsinfo" linkID="0x${info.ID}" className="sec bld">Click here</el:cmd>.<br />
</c:if>
<br />
<content:copyright />
</div>
</body>
</html>
