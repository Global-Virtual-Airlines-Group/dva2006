<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Flight Academy Certification Updated</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isUpdate || isNew}">
<div class="updateHdr">Flight Academy Certification Updated</div>
<br />
The <content:airline /> Flight Academy <span class="pri bld">${cert.name}</span> Certification profile 
has been updated.<br />
</c:if>
<c:if test="${isNew}">To update the list of requirements for this Flight Academy certification, <el:cmd url="certreqs" linkID="${cert.name}" op="edit" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${isDelete}">
<div class="updateHdr">Flight Academy Certification Deleted</div>
<br />
The <content:airline /> Flight Academy <span class="pri bld">${cert.name}</span> Certification profile has been deleted.<br />
</c:if>
<c:if test="${updateReqs}">
<div class="updateHdr">Flight Academy Certification Requirements Updated</div>
<br />
The reqirements for the <content:airline /> Flight Academy <span class="pri bld">${cert.name}</span> Certification have been updated.<br />
</c:if>
<br />
To view the list of Flight Academy certifications, <el:cmd url="certs" className="sec bld">Click Here</el:cmd>.<br />
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
