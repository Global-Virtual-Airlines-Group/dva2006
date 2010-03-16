<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Examination / Check Ride Statistics</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function toggleCombo(opt)
{
var lbl;
if (!opt) {
	var f = document.forms[0];
	for (var x = 0; x < f.searchType.length; x++) {
		if (f.searchType[x].checked) {
			lbl = f.searchType[x].value;
			break;
		}
	}
} else
	lbl = opt.value;
	
if (lbl == 'Examinations') {
	enableElement('crScorer', false);
	enableElement('examScorer', true);
} else {
	enableElement('examScorer', false);
	enableElement('crScorer', true);
}

return true;	
}

function validate(from)
{
if (!checkSubmit()) return false;

setSubmit();
disableButton('SearchButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void toggleCombo()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="examstats.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4"><content:airline /> EXAMINATION / CHECK RIDE STATISTICS</td>
</tr>
<tr>
 <td class="label">Search Type</td>
 <td class="data"><el:check name="searchType" idx="*" type="radio" options="${searchTypes}" value="${param.searchType}" onChange="void toggleCombo(this)" /></td>
 <td class="label">Flight Academy</td>
 <td class="data"><el:box name="academyOnly" idx="*" value="true" label="Flight Academy Only" checked="${param.academyOnly}" /></td>
</tr>
<tr>
 <td class="label">Grouping</td>
 <td class="data"><el:combo name="label" size="1" idx="*" options="${groupOpts}" value="${label}" /></td>
 <td class="label">Sub-Grouping</td>
 <td class="data"><el:combo name="subLabel" size="1" idx="*" options="${groupOpts}" value="${subLabel}" /></td>
</tr>
<tr>
 <td class="label">Exam Scorer</td>
 <td class="data"><el:combo ID="examScorer" name="examScorer" size="1" idx="*" firstEntry="-" options="${examScorers}" value="${param.examScorer}" /></td>
 <td class="label">Check Ride Scorer</td>
 <td class="data"><el:combo ID="crScorer" name="crScorer" size="1" idx="*" firstEntry="-" options="${crScorers}" value="${param.crScorer}" /></td>
</tr>
</el:table>

<!-- Table Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button type="submit" ID="SearchButton" className="BUTTON" label="SEARCH EXAMIANTIONS / CHECK RIDES" /></td>
</tr>
</el:table>

<c:if test="${doSearch}">
<view:table className="view" pad="default" space="default" cmd="examstats">
<!-- Table Header bar -->
<tr class="title caps">
 <td width="30%">LABEL</td>
 <td width="30%">SUB-LABEL</td>
 <td width="10%">PILOTS</td>
 <td width="10%">TOTAL</td>
 <td width="10%">PASSED</td>
 <td>PERCENT</td>
</tr>

<!-- Table Statistics Data -->
<c:forEach var="stat" items="${viewContext.results}">
<view:row entry="${stat}">
 <td class="pri bld">${stat.label}</td>
 <td class="bld">${stat.subLabel}</td>
 <td><fmt:int value="${stat.users}" /></td>
 <td><fmt:int value="${stat.total}" /></td>
 <td class="sec bld"><fmt:int value="${stat.passed}" /></td>
 <td class="pri bld"><fmt:dec value="${stat.passed * 100 / stat.total}" />%</td>
</view:row>
</c:forEach>

<!-- Table Footer Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp /> <view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</c:if>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
