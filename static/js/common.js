
function getElement(eName)
{
return (document.all) ? document.all(eName) : document.getElementById(eName);
}

function disableButton(btnName)
{
var btn = getElement(btnName);
if (btn) btn.disabled = true;
return true;
}

function checkSubmit()
{
if (document.isSubmit) {
	alert('This page is already being submitted. Please be patient.');
	return false;
}
 
return true;
}

function setSubmit()
{
document.isSubmit = true;
return true;
}

function cmdPost(url)
{
var form = document.forms[0];
form.action = url;
 
// Execute the form validation - if any
var sFunc = form.onsubmit;
if (sFunc != null) {
	submitOK = sFunc();
	if (!submitOK)
		return false;
}
  
setSubmit();
form.submit();
return true;
}

function cmdGet(url)
{
setSubmit();
self.location = '/' + url;
return true;
}


function validateText(text, min, title)
{
if (!text) return true;
if (text.value.length < min) {
	alert('Please provide the ' + title + '.');
	text.focus();
	return false;
}

return true;
}

function validateNumber(text, minValue, title)
{
if (!text) return true;
intValue = parseFloat(text.value);
if ((text.value.length < 1) || (intValue == Number.NaN)) {
	alert('Please provide a numeric ' + title + '.');
	text.focus();
	return false;
}

if (intValue < minValue) {
	alert('The ' + title + ' must be greater than ' + minValue + '.');
	text.focus();
	return false;
}

return true;
}

function validateCombo(combo, title)
{
if (!combo) return true;
if (combo.selectedIndex == 0) {
	alert('Please provide the ' + title + '.');
	combo.focus();
	return false;
}

return true;
}

function validateFile(fileName, extType, title)
{
if ((!fileName) || (fileName.value.length == 0)) return true;
extTypes = extType.toLowerCase().split(',');

fName = fileName.value;
ext = fName.substring(fName.length - 3, fName.length).toLowerCase();
for (x = 0; x < extTypes.length; x++) {
	if (ext == extTypes[x])
		return true;
}

alert('The ' + title + ' must be a ' + extType.toUpperCase() + ' file.');
fileName.focus();
return false;
}

function validateCheckBox(checkbox, minSelected, title)
{
if (!checkbox) return true;
var checkCount = 0;
for (x = 0; x < checkbox.length; x++) {
	if (checkbox[x].checked)
		checkCount++;
}

if (checkCount >= minSelected) return true;
alert('At least ' + minSelected + ' ' + title + ' must be selected.');
checkbox[0].focus();
return false;
}

function setDaysInMonth(combo)
{
var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
var month = parseInt(combo.options[combo.selectedIndex].value);

var dCombo = document.forms[0].dateD;
dCombo.options.length = daysInMonth[month];
for (x = 1; x <= daysInMonth[month]; x++)
	dCombo.options[x-1] = new Option(x);
	
return true;
}

function initDateCombos(mCombo, dCombo, d)
{
mCombo.selectedIndex = d.getMonth();
setDaysInMonth(mCombo);
dCombo.selectedIndex = (d.getDate() - 1);
return true;
}
