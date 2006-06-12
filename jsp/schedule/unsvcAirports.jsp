<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Unserviced Airports</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> UNSERVICED AIRPORTS</div>
<br />
<c:if test="${totalResults == 0}">
There are no Airports listed for an Airline without at least one corresponding entry in the <content:airline /> 
Flight Schedule.<br />
</c:if>
<c:forEach var="airline" items="${airlines}">
<c:set var="airports" value="${results[airline]}" scope="request" />
The following <fmt:int value="${fn:sizeof(airports)}" /> airports are no longer served by 
<span class="pri bld">${airline.name}</span>:<br />
<br />
<c:forEach var="airport" items="${airports}">
<el:cmd url="airport" linkID="${airport.IATA}" op="edit" className="bld">${airport.name}</el:cmd> (${airport.ICAO} / ${airport.IATA})<br />
</c:forEach>
<hr />
</c:forEach>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
