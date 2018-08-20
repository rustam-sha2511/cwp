<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
<title>Case Worker Portal - My Dashboard</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/bootstrap.min.css" />
<link
	href="${pageContext.request.contextPath}/css/responsive-slider.css"
	rel="stylesheet" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/animate.css" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/font-awesome.min.css" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/default.css" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/message.css"
	media="screen" type="text/css" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/dataTables.bootstrap.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/buttons.dataTables.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/rowReorder.dataTables.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/responsive.dataTables.min.css">

<script src="${pageContext.request.contextPath}/js/jquery.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
<script
	src='${pageContext.request.contextPath}/js/jquery.dataTables.min.js'></script>
<script
	src='${pageContext.request.contextPath}/js/dataTables.bootstrap.min.js'></script>
<script
	src='${pageContext.request.contextPath}/js/dataTables.buttons.min.js'></script>
<script
	src='${pageContext.request.contextPath}/js/dataTables.rowReorder.min.js'></script>
<script
	src='${pageContext.request.contextPath}/js/dataTables.responsive.min.js'></script>

<script>
	$(document)
			.ready(
					function() {
						$('#mySpinner').addClass('spinner');
						$('#mySpinnerBackdrop').removeClass('hide');
						$('#mySpinnerBackdrop').addClass('show');

						var casesJsonObj = JSON.parse('${casesJsonObj}');
						$('#inputTabularData')
								.DataTable(
										{
											processing : true,
											"data" : casesJsonObj,
											"columns" : [
													{
														"data" : "id",
														"render" : function(
																data, type,
																row, meta) {
															if (type === 'display') {
																data = '<a href="openCase/' + data + '">'
																		+ data
																		+ '</a>';
															}

															return data;
														}
													},
													{
														"data" : "assignedCwName"
													}, {
														"data" : "date"
													}, {
														"data" : "desc"
													}, {
														"data" : "status"
													} ],
											responsive : true
										});

						$('#mySpinner').removeClass('spinner');
						$('#mySpinnerBackdrop').removeClass('show');
						$('#mySpinnerBackdrop').addClass('hide');

					});
</script>
<head>
<body class="dashboard-stroke">
	<div id="mySpinnerBackdrop" class="spinnerBackdrop"></div>
	<nav class="navbar navbar-inverse">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
					data-target="#myNavbar">
					<span class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="#"><img
					src="${pageContext.servletContext.contextPath}/img/EMT_logo.png"
					class="img-responsive"></a> <a
					class="navbar-brand navbar-brand-text emt-brand-text" href="#">Case
					<span class="switch">Worker </span>Portal
				</a>
			</div>
			<div class="collapse navbar-collapse" id="myNavbar">
				<ul class="nav navbar-nav w-100">
					<li class="active"><a
						href="${pageContext.request.contextPath}/cwDashboard">My
							Dashboard</a></li>
				</ul>
				<ul class="nav navbar-nav w-100">
					<li><a
						href="${pageContext.request.contextPath}/cwCaseCreation">Add
							New Case</a></li>
				</ul>
				<ul class="nav navbar-nav w-100">
					<li><a href="${pageContext.request.contextPath}/cwAppointment">My
							Appointments</a></li>
				</ul>
				<ul class="nav navbar-nav w-100">
					<li><a
						href="${pageContext.request.contextPath}/cwAccountDetails">My
							Account</a></li>
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li><a href="#">${welcomeMsg}</a></li>
					<li><a href="${pageContext.request.contextPath}/logout"><span>Logout
								&nbsp;<span class="glyphicon glyphicon-log-out"></span>
						</span></a></li>
				</ul>
			</div>
		</div>
	</nav>
	<!-- Container Starts -->
	<div class="container-fluid" role="main">
		<div id="mySpinner"></div>

		<div class="col-lg-12 col-md-12 col-sm-12 mrgTop15 emt-conv-err hide"
			id="convertorErrDiv"></div>
		<br>
		<div class="row">
			<div class="col-md-offset-3 col-md-6">
				<div class="section-header">
					<h2 class="section-heading animated" data-animation="bounceInUp">My&nbsp;&nbsp;
						Dashboard</h2>
				</div>
				<br>
				<c:if test="${not empty caseAccess}">
					<div class="isa_error">
						<i class="fa fa-times-circle"></i> ${caseAccess}
					</div><br>
				</c:if>
			</div>
		</div>
		<div class="row result-section">
			<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
				<table id="inputTabularData"
					class="table table-striped table-bordered dcm-datatables"
					cellspacing="0" width="100%">
					<thead>
						<tr>
							<th>CASE ID</th>
							<th>ASSIGNED CASE WORKER</th>
							<th>CASE CREATION DATE</th>
							<th>CASE DESCRIPTION</th>
							<th>CASE STATUS</th>
						</tr>
					</thead>
				</table>
			</div>
		</div>
	</div>
	<div>
        <ul class="nav pull-right voice-icon">
            <li>
            	<a href="javascript:void(0);" id="voice-icon">
	                <span class="fa-stack fa-2x">
		                <i class="fa fa-circle fa-stack-2x"></i>
		                <i aria-hidden="true" class="fa fa-microphone fa-stack-1x fa-x"></i>
	            	</span>
            	</a>
            </li>
        </ul>
    </div>
    <div id="overlay"></div>
	<div id="popup">
	    <!-- <div class="popupcontrols">
	        <span id="popupclose">X</span>
	    </div> -->
	    <div class="popupcontent">
	        <iframe class="popupIframe" src="https://console.dialogflow.com/api-client/demo/embedded/b812d869-6f01-41e1-97d9-c68b20cf4bf9">
	        </iframe>
	    </div>
	</div>
    <script type="text/javascript">
    // Initialize Variables
    //var closePopup = document.getElementById("popupclose");
    $(document).ready(function(){
    	var button = $("#voice-icon");
    	var overlay = $("#overlay");
        var popup = $("#popup");
    	$(button).on('click', function(){
    		$(overlay).css('display','block');
            $(popup).css('display','block');
            var closeIcon = '<i class="fa fa-times-circle-o pull-right"></i>';
       		//$('.b-agent-demo_header-wrapper').append(closeIcon);
       		//var myFrame = $(".popupIframe").contents().find('.b-agent-demo_header-wrapper');
        	//myFrame.html(closeIcon);
    	});
    });
</script>
</body>
</html>