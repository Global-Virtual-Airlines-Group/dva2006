<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="eliteName" name="econ.elite.name" />
<html lang="en">
<head>
<title><content:airline />&nbsp;${eliteName}<c:if test="${!empty lvl}"> - ${lvl.name}</c:if></title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="jsColor" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.name, l:3, t:'Level Name'});
	golgotha.form.validate({f:f.year, min:2003, t:'Program Year'});
	golgotha.form.validate({f:f.color, l:6, t:'Label Color'});
	golgotha.form.validate({f:f.targetPct, min:1, t:'Target Percentile'});
	golgotha.form.validate({f:f.legs, min:1, t:'Minimum Legs'});
	golgotha.form.validate({f:f.distance, min:1, t:'Minimum Distance'});
	golgotha.form.validate({f:f.pts, min:0, t:'Minimum Points'});
	golgotha.form.validate({f:f.bonus, min:1, t:'Point Bonus'});
	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="elitelevel.do" method="post" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">${eliteName} STATUS<c:if test="${!empty lvl}"> - ${lvl.name} (${lvl.year})</c:if></td>
</tr>
<tr>
 <td class="label">Name</td>
 <td class="data"><el:text name="name" idx="*" size="24" max="32" required="true" className="pri bld" value="${lvl.name}" /></td>
</tr>
<tr>
 <td class="label">Program Year</td>
 <td class="data"><el:text name="year" idx="*" size="4" max="4" required="true" className="bld" value="${lvl.year}" /></td>
</tr>
<tr>
 <td class="label">Label Color</td>
 <td class="data"><el:text name="color" idx="*" className="color bld req" size="6" max="8" value="${lvl.hexColor}" />&nbsp;<span class="small">Click on the text box for a color picker.</span></td>
</tr>
<tr>
 <td class="label">Point Bonus</td>
 <td class="data"><el:text name="bonus" idx="*" size="2" max="3" value="${Math.round(lvl.bonusFactor * 100)}" />% <span class="small nophone">The point bonus is added at the end of any score calculates for Pilots at this level.</span></td>
</tr>
<tr>
 <td class="label">Target Percentile</td>
 <td class="data"><el:text name="targetPct" idx="*" size="2" max="3" value="${lvl.targetPercentile}" />&nbsp;<span class="small nophone">When levels are recalculated each year, flight leg requirements will be set to approximately this percentile.</span> 
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isVisible" idx="*" checked="${lvl.isVisible}" className="small" label="This level is visible to Pilots at lower levels" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">ELIGIBILITY REQUIREMENTS</td>
</tr>
<tr>
 <td class="label">Minimum Legs</td>
 <td class="data"><el:text name="legs" idx="*" size="3" max="4" required="true" className="bld" value="${lvl.legs}" /></td>
</tr>
<tr>
 <td class="label">Minimum Distance</td>
 <td class="data"><el:text name="distance" idx="*" size="6" max="7" required="true" value="${lvl.distance}" /> miles</td>
</tr>
<tr>
 <td class="label">Minimum Points</td>
 <td class="data"><el:text name="pts" idx="*" size="6" max="7" required="true" value="${lvl.points}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE ${eliteName} PROFILE" /></td>
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
