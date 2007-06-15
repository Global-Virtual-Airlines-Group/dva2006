<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Greaser Club</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateNumber(form.viewCount, 1, 'Number of Landings')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}

function update()
{
var f = document.forms[0];
f.submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="landings.do" method="post" validate="return validate(this)">
The members of <content:airline /> are a skilled group - and they can prove it. Below is a list of the 
smoothest landings, and the greaser pilots who made them:<br />
<br />
<el:table className="view" pad="default" space="default">
<!-- Table top Header bar -->
<tr class="title">
 <td class="left caps" colspan="3"><content:airline /> GREASED LANDING CLUB</td>
 <td class="right" colspan="4"><el:text name="viewCount" idx="*" size="1" max="2" value="${viewCount}" /> 
FLIGHTS WITHIN <el:combo name="days" idx="*" size="1" options="${dateFilter}" value="${param.days}" onChange="void update()" /> 
IN <el:combo name="eqType" idx="*" size="1" options="${eqTypes}" value="${param.eqType}" onChange="void update()" />
<el:button ID="SearchButton" type="submit" className="BUTTON" label="GO" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="5%">#</td>
 <td width="20%">PILOT NAME</td>
 <td width="10%">DATE</td>
 <td width="10%">FLIGHT #</td>
 <td width="10%">EQUIPMENT</td>
 <td width="10%">TOUCHDOWN</td>
 <td>AIRPORTS</td>
</tr>

<!-- Table Flight Report Data -->
<c:set var="entryNumber" value="0" scope="request" />
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
<tr>
 <td class="sec bld">${entryNumber}</td>
 <td>${pirep.firstName} ${pirep.lastName}</td>
 <td class="pri bld"><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td><el:cmd className="small bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="sec">${pirep.equipmentType}</td>
 <td class="bld"><fmt:int value="${pirep.landingVSpeed}" /> ft/min</td>
 <td class="small">${pirep.airportD.name} - ${pirep.airportA.name}</td>
</tr>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="7">&nbsp;</td>
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
