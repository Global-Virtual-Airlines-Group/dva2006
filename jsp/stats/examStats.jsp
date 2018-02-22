<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Examination / Check Ride Statistics</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<script type="text/javascript">
golgotha.local.toggleCombo = function(opt)
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
	golgotha.util.disable('crScorer');
	golgotha.util.disable('examScorer', false);
} else {
	golgotha.util.disable('examScorer');
	golgotha.util.disable('crScorer', false);
}

return true;	
};

golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.local.toggleCombo()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="examstats.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="4"><content:airline /> EXAMINATION / CHECK RIDE STATISTICS</td>
</tr>
<tr>
 <td class="label">Search Type</td>
 <td class="data"><el:check name="searchType" idx="*" type="radio" options="${searchTypes}" value="${param.searchType}" onChange="void golgotha.local.toggleCombo(this)" /></td>
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
<el:table className="bar">
<tr>
 <td><el:button type="submit" ID="SearchButton" label="SEARCH EXAMIANTIONS / CHECK RIDES" /></td>
</tr>
</el:table>

<c:if test="${doSearch}">
<view:table cmd="examstats">
<!-- Table Header bar -->
<tr class="title caps">
 <td style="width:30%">LABEL</td>
 <td style="width:30%">SUB-LABEL</td>
 <td style="width:10%">PILOTS</td>
 <td style="width:10%">TOTAL</td>
 <td style="width:10%">PASSED</td>
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
 <td colspan="6"><view:scrollbar><view:pgUp /> <view:pgDn /><br /></view:scrollbar>
<view:legend width="150" labels="All Passed,None Passed" classes="opt1,warn" /></td>
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
