<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Security Cookie Data</title>
<content:css name="main" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><span class="nophone"><content:airline />&nbsp;</span>Security Cookie Data</div>
<br />
<c:if test="${!empty cd}">
<span class="pri bld">FROM SESSION</span><br />
<br />
${cd} - <span class="sec bld">${cd.signatureAlgorithm}</span><br />
Login at <fmt:date date="${cd.loginDate}" />, expires on <fmt:date date="${cd.expiryDate}" /><br />
<br />
</c:if>
<c:if test="${!empty cd2}">
<span class="pri bld">FROM COOKIE</span><br />
<br />
${cd2} - <span class="sec bld">${cd2.signatureAlgorithm}</span><br />
Login at <fmt:date date="${cd2.loginDate}" />, expires on <fmt:date date="${cd2.expiryDate}" /><br />
</c:if>
<c:if test="${!empty ex}">
<span class="error bld">COOKIE DESCRYPTION ERROR - ${ex.message}</span><br />
</c:if>
<br /> 
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
