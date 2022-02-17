<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><!doctype html>
<html lang="ko">
<head>
<title>Elibrary - CrossDomain</title>
<script type="text/javascript" src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script type="text/javascript">
function cross_ajax() {
	$.ajax({
		crossDomain: true, xhrFields: { withCredentials: true },
    	beforeSend : function (request) {
			try { request.setRequestHeader("Origin-Headers", "{ 'aa': 1, 'bb': 'cc' }"); } catch (e) { }
    	},
		url: "http://core.gamble.nextabs.kr:<%= request.getServerPort() %>/base/ajax.do", type: "POST",
		data: { parameters: new Date() }, success: function(result) {
			console.log(result);
		}
	});
}
</script>
</head>

<body>

<button type="button" onclick="cross_ajax()">Call xDomain</button>

</body>

</html>