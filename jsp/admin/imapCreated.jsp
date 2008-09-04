<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>IMAP Mailbox Created</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="IMAPServer" name="smtp.imap.server" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">IMAP Mailbox Created</div>
<br />
An IMAP mailbox for ${pilot.name} has been created on the <content:airline /> IMAP server ${IMAPServer}. 
${pilot.name}'s e-mail address has been set to ${imap.address}, with a mailbox quota of <fmt:int value="${imap.quota}" /> bytes.<br />
<br />
<c:if test="${!empty scriptResults}">
Mailbox creation script output:<br />
<c:forEach var="outData" items="${scriptResults}">
${outData}<br />
</c:forEach>
<br />
</c:if>
To make changes to this address or to update ${pilot.name}'s mailbox aliases, you may edit his or her Pilot profile. 
To do so, <el:cmd url="profile" link="${pilot}" className="sec bld">Click Here</el:cmd>.<br />
To view all IMAP mailbox profiles, <el:cmd url="imaplist" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
