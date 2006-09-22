<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Academy Certification Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isUpdate || isNew}">
<!-- Flight Academy Certification Updated -->
<div class="updateHdr">Flight Academy Certification Updated</div>
<br />
The <content:airline /> Flight Academy <span class="pri bld">${cert.name}</span> Certification profile 
has been updated.<br />
</c:if>
<c:if test="${isNew}">To update the list of requirements for this Flight Academy certification, 
<el:cmd url="certreqs" linkID="${cert.name}" op="edit" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${isDelete}">
<!-- Flight Academy Certification Deleted -->
<div class="updateHdr">Flight Academy Certification Deleted</div>
<br />
The <content:airline /> Flight Academy <span class="pri bld">${cert.name}</span> Certification profile 
has been deleted.<br />
</c:if>
<c:if test="${updateReqs}">
<!-- Flight Academy Requirements Updated -->
<div class="updateHdr">Flight Academy Certification Requirements Updated</div>
<br />
The reqirements for the <content:airline /> Flight Academy <span class="pri bld">${cert.name}</span> 
Certification have been updated.<br />
</c:if>
To view the list of Flight Academy certifications, <el:cmd url="certs" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
