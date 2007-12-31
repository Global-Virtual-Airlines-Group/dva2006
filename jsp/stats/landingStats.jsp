<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Landing Statistics</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateNumber(form.legCount, 5, 'Minimum Number of Landings')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}

function update()
{
document.forms[0].submit();
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
<el:form action="landingstats.do" method="post" validate="return validate(this)">
The members of <content:airline /> are a skilled group - and they can prove it. Below is a list of pilots 
who consistently achieve the smoothest landings. This list is ordered by both average touchdown speed (which 
counts for 60% of the ordering) and the standard deviation of those speeds (the remaining 40%).<br />
<br />
<el:table className="view" pad="default" space="default">
<!-- Table top Header bar -->
<tr class="title">
 <td class="left caps" colspan="2"><content:airline /> LANDING STATISTICS</td>
 <td class="right" colspan="5">MINIMUM <el:text name="legCount" idx="*" size="1" max="2" value="${legCount}" /> 
FLIGHTS WITHIN <el:combo name="days" idx="*" size="1" options="${dateFilter}" value="${param.days}" onChange="void update()" /> 
IN <el:combo name="eqType" idx="*" size="1" options="${eqTypes}" value="${param.eqType}" onChange="void update()" />
<el:button ID="SearchButton" type="submit" className="BUTTON" label="GO" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td width="5%">#</td>
 <td width="22%">PILOT NAME</td>
 <td width="18%">RANK</td>
 <td width="10%">FLIGHTS</td>
 <td width="10%">HOURS</td>
 <td width="15%">AVERAGE SPEED</td>
 <td>STD. DEVIATION</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="entryNumber" value="0" scope="request" />
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[entry.ID]}" scope="request" />
<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
<tr>
 <td class="sec bld">${entryNumber}</td>
 <td class="pri bld">${pilot.name}</td>
 <td>${pilot.rank}, ${pilot.equipmentType}</td>
 <td><fmt:int value="${entry.legs}" /></td>
 <td><fmt:dec value="${entry.hours}" /></td>
 <td class="pri bld"><fmt:dec value="${entry.averageSpeed}" fmt="#0.00" /> ft/min</td>
 <td class="sec"><fmt:dec value="${entry.stdDeviation}" fmt="#0.00" /> ft/min</td>
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
