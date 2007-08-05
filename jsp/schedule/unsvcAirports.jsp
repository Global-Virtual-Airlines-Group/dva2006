<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Unserviced Airports</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!confirm("Are you sure you wish to continue?")) return false;

setSubmit();
disableButton('ReloadButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="usvcairports.do" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> UNSERVICED AIRPORTS</td>
</tr>
<tr>
 <td colspan="2" class="left">
<c:if test="${totalResults == 0}">
<div class="pri bld">There are no Airports listed for an Airline without at least one corresponding entry in the <content:airline /> 
Flight Schedule.</div>
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
 </td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="updateDB" idx="*" value="true" label="Update Airports in Database" /></td>
</tr>
</el:table>

<!-- Button bar -->
<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="ReloadButton" type="SUBMIT" className="BUTTON" label="UPDATE AIRPORTS" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
