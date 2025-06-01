<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Load Statistics</title>
<content:css name="main" />
<content:css name="form" />
<content:googleAnalytics />
<content:js name="common" />
<content:googleJS module="charts" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<script async>
golgotha.local.update = function(cb) {
	golgotha.form.submit(document.forms[0]);
	const days = golgotha.form.getCombo(cb);
	return golgotha.local.load(days);
};

golgotha.local.revert = function() { golgotha.util.display('revertLink', false); return golgotha.local.loadModel(golgotha.local.defaultModel); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="loadinfo" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>ON-TIME / LOAD FACTOR DASHBOARD</td>
 <td class="right" style="width:20%;"><span class="nophone">TIME PERIOD&nbsp;</span><el:combo name="daysBack" size="1" idx="*" firstEntry="[ SELECT ]" value="${days}" options="${dayOpts}" onChange="void golgotha.local.update(this)" /></td>
</tr>
<tr class="title caps">
 <td colspan="3">LOAD FACTOR STATISICS</td>
</tr>
<tr>
 <td colspan="3"><div id="lfChart" style="width:100%; height:360px;"></div></td>
</tr>
<tr>
 <td class="label">Target Load Factor</td>
 <td class="data" colspan="2"><fmt:dec value="${econ.targetLoad}" fmt="##0.00%" className="pri bld" /><span class="small ita nophone"> (This is constant from day to day.)</span></td>
</tr>
<tr>
 <td class="label">Daily Target Load</td>
 <td class="data" colspan="2"><fmt:dec value="${dailyTargetLoad}" fmt="##0.00%" className="bld" /><span class="small ita nophone"> (This is the target load factor for <fmt:date date="${today}" fmt="d" />)</span>
</tr>
<tr>
 <td class="label">Minimum Load Factor</td>
 <td class="data" colspan="2"><fmt:dec value="${minimumLoad}" fmt="##0.00%" className="ter bld" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">LOAD FACTOR MODELING</td>
 <td class="right"><span class="nophone">TIME PERIOD&nbsp;</span><el:combo name="modelDays" size="1" idx="*" firstEntry="[ SELECT ]" value="${days}" options="${modelOpts}" />
 <el:button label="CALCULATE" onClick="void golgotha.local.model()" /></td>
</tr>
<tr id="lfmDiv" style="display:none;">
 <td colspan="3"><div id="lfmChart" style="width:100%; height:360px;"></div></td>
</tr>
<tr>
 <td class="label">Target Load</td>
 <td class="data" colspan="2"><el:text name="targetLoad" size="4" max="6" required="true" idx="*" className="pri bld" value="${econ.targetLoad}" />, Amplitude: <el:text name="targetAmp" size="4" max="6" required="true" idx="*" value="${econ.amplitude}" />
<span id="revertLink" style="display:none;"> - <a href="javascript:void golgotha.local.revert()">REVERT</a></span></td>
</tr>
<tr>
 <td class="label">Cycle Length</td>
 <td class="data" colspan="2"><el:text name="cycleLength" size="3" max="4" idx="*" value="${econ.cycleLength}" /> days</td>
</tr>
<tr>
 <td class="label">Minimum Load</td>
 <td class="data" colspan="2"><el:text name="minLoad" size="4" max="5" required="true" idx="*" className="sec bld" value="${econ.minimumLoad}" /></td>
</tr>
<tr class="title caps">
 <td colspan="3">ON TIME STATISTICS</td>
</tr>
<tr>
 <td colspan="3"><div id="otChart" style="width:100%; height:360px;"></div></td>
</tr>
<tr class="title caps">
 <td colspan="3">&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
golgotha.local.load = function(days) {
	golgotha.local.loadCount = 2;
	golgotha.form
	const xreq1 = new XMLHttpRequest();
	xreq1.timeout = 7500;
	xreq1.open('get', 'loadstats.ws?id=' + days, true);
	xreq1.onreadystatechange = function() {
		if (xreq1.readyState != 4) return false;
		if (xreq1.status != 200) {
			golgotha.local.loadComplete();
			return false;
		}

		const d = JSON.parse(xreq1.responseText);
		d.data.forEach(golgotha.charts.dateTX);
		const c = new google.visualization.LineChart(document.getElementById('lfChart'));
		const data = new google.visualization.DataTable();
		data.addColumn('date', 'Month');
		data.addColumn('number', 'Actual Load');
		data.addColumn('number', 'Target Load');
		data.addColumn('number', 'Passengers');
		data.addRows(d.data);

		const hX = {title:'Flight Date',textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
		const vA0 = {title:'Load Factor',maxValue:1,gridlines:{count:10},textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle,format:'percent'};
		const vA1 = {title:'Passengers',maxValue:d.maxPassengers,gridlines:{count:10},extStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
		const o = golgotha.charts.buildOptions({series:[{targetAxisIndex:0},{targetAxisIndex:0},{targetAxisIndex:1}],title:'Flights by Load Factor',width:'100%',colors:['blue','grey','green'],hAxis:hX,vAxes:[vA0,vA1]});
		c.draw(data, o);
		golgotha.local.loadComplete();
		return true;
	};
	
	const xreq2 = new XMLHttpRequest();
	xreq2.timeout = 7500;
	xreq2.open('get', 'otstats.ws?id=' + days, true);
	xreq2.onreadystatechange = function() {
		if (xreq2.readyState != 4) return false;
		if (xreq2.status != 200) {
			golgotha.local.loadComplete();
			return false;
		}

		const d = JSON.parse(xreq2.responseText);
		d.data.forEach(golgotha.charts.dateTX);
		const c = new google.visualization.ColumnChart(document.getElementById('otChart'));
		const data = new google.visualization.DataTable();
		data.addColumn('date', 'Month');
		data.addColumn('number', 'Early');
		data.addColumn('number', 'On Time');
		data.addColumn('number', 'Late');
		data.addRows(d.data);

		const vA = {title:'Flights',maxValue:d.maxLegs,textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
		const hA = {title:'Flight Date',textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
		const o = golgotha.charts.buildOptions({isStacked:true,title:'Flights by On-Time Status',width:'100%',colors:['blue','green','red'],vAxes:[vA],hAxis:hA});
		c.draw(data, o);
		golgotha.local.loadComplete();
		return true;
	};
	
	xreq1.send(null);
	xreq2.send(null);
	return true;
};

golgotha.local.model = function() {
	const f = document.forms[0];
	const xreq = new XMLHttpRequest();
	xreq.timeout = 2500;
	xreq.open('post', 'lfmodel.ws', true);
	xreq.onreadystatechange = function() {
		if ((xreq.readyState != 4) || (xreq.status != 200)) return false;
		
		const d = JSON.parse(xreq.responseText);
		d.data.forEach(golgotha.charts.dateTX);
		golgotha.local.defaultModel = d.defaultModel;
		const c = new google.visualization.LineChart(document.getElementById('lfmChart'));
		const data = new google.visualization.DataTable();		
		data.addColumn('date', 'Month');
		data.addColumn('number', 'Actual');
		data.addColumn('number', 'Model');
		data.addRows(d.data);
		
		const vA = {title:'Load Factor',maxValue:1,gridlines:{count:10},textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle,format:'percent'};
		const hA = {title:'Flight Date',textStyle:golgotha.charts.lgStyle,titleTextStyle:golgotha.charts.ttStyle};
		const o = golgotha.charts.buildOptions({title:'Modeled vs. Actual Load Factor',width:'100%',colors:['blue','grey','green'],hAxis:hA,vAxes:[vA]});
		golgotha.util.display('lfmDiv', true);
		golgotha.util.display('revertLink', true);
		c.draw(data, o);
		golgotha.local.loadModel(d.model);
		return true;
	};
	
	// Build parameters
	const fd = new FormData(f);
	fd.set('daysBack', fd.get('modelDays'));
	xreq.send(fd);
	return true;
};

golgotha.local.loadModel = function(m) {
	const f = document.forms[0];
	f.targetLoad.value = m.targetLoad;
	f.targetAmp.value = m.targetAmp;
	f.cycleLength.value = Math.round(365 / m.yearHZ);
	return true;
};

golgotha.local.loadComplete = function() {
	golgotha.local.loadCount--;
	if (golgotha.local.loadCount != 0) return false;
	golgotha.form.clear(document.forms[0]);
	return true;
};

google.charts.load('current',{'packages':['corechart']});
google.charts.setOnLoadCallback(function() {
	const f = document.forms[0];
	golgotha.form.submit(f);
	golgotha.local.load(golgotha.form.getCombo(f.daysBack));
	return true;
});
</script>
</body>
</html>
