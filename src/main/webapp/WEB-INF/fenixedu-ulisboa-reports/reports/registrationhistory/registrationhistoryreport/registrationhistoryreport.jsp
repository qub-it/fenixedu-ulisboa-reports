<%@page
	import="org.fenixedu.ulisboa.reports.ui.reports.registrationhistory.NewRegistrationHistoryReportController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables"
	uri="http://github.com/dandelion/datatables"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<spring:url var="datatablesUrl"
	value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl"
	value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl"
	value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl"
	value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
${portal.angularToolkit()}
<%--${portal.toolkit()}--%>

<link
	href="${pageContext.request.contextPath}/static/fenixedu-ulisboa-reports/css/dataTables.responsive.css"
	rel="stylesheet" />
<script
	src="${pageContext.request.contextPath}/static/fenixedu-ulisboa-reports/js/dataTables.responsive.js"></script>
<link
	href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css"
	rel="stylesheet" />
<script
	src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link
	href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css"
	rel="stylesheet" />
<script
	src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script
	src="${pageContext.request.contextPath}/static/fenixedu-ulisboa-reports/js/omnis.js"></script>

<script
	src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script
	src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>


<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message
			code="label.reports.registrationHistory.registrationHistoryReport" />
		<small></small>
	</h1>
</div>


<%-- NAVIGATION --%>

<c:if test="${not empty infoMessages}">
	<div class="alert alert-info" role="alert">

		<c:forEach items="${infoMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon glyphicon-ok-sign"
					aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>

	</div>
</c:if>
<c:if test="${not empty warningMessages}">
	<div class="alert alert-warning" role="alert">

		<c:forEach items="${warningMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign"
					aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>

	</div>
</c:if>
<c:if test="${not empty errorMessages}">
	<div class="alert alert-danger" role="alert">

		<c:forEach items="${errorMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign"
					aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>

	</div>
</c:if>




<style>
	.glyphicon.spinning {
	    animation: spin 1s infinite linear;
	    -webkit-animation: spin2 1s infinite linear;
	}
	
	@keyframes spin {
	    from { transform: scale(1) rotate(0deg); }
	    to { transform: scale(1) rotate(360deg); }
	}
	
	@-webkit-keyframes spin2 {
	    from { -webkit-transform: rotate(0deg); }
	    to { -webkit-transform: rotate(360deg); }
	}	
</style>

<script type="text/javascript">


	angular.module('registrationHistoryReportApp',
			[ 'ngSanitize', 'ui.select', 'bennuToolkit' ]).controller(
			'NewRegistrationHistoryReportController', [ '$scope','$timeout', '$http',

			function($scope,$timeout,$http) {
				
				$scope.booleanvalues = [ {
					name : '<spring:message code="label.no"/>',
					value : false
				}, {
					name : '<spring:message code="label.yes"/>',
					value : true
				} ];

				$scope.object = ${beanJson};
				$scope.form = {};
				$scope.form.object = $scope.object;

				$scope.postBack = createAngularPostbackFunction($scope);

				$scope.onBeanChange = function(model, field) {

					if (field == 'degreeTypes') {
						$scope.object.degrees = [];
						$scope.object.programConclusions = [];
					}

					$scope.postBack(model);
				}

				$scope.search = function() {
					
					if ($scope.object.executionYears.length != 0) {
						$('#searchParamsForm').attr('action', '${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/search')
						$('#searchParamsForm').submit();
					}
				}
				
				$scope.exportRegistrations = function() {
					$scope.exportResult(
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportregistrations', 
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportstatus/', 
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/downloadreport/')
				}

                $scope.exportBlueRecordData = function() {
                    $scope.exportResult(
                            '${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportbluerecord', 
                            '${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportstatus/', 
                            '${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/downloadreport/')
                }

				
				$scope.exportApprovals = function() {
					$scope.exportResult(
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportapprovals', 
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportstatus/', 
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/downloadreport/')
				}
				
				$scope.exportEnrolments = function() {
					$scope.exportResult(
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportenrolments', 
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportstatus/', 
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/downloadreport/')
				}
				
				$scope.exportRegistrationsByStatute = function() {
					$scope.exportResult(
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportregistrationsbystatute', 
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/exportstatus/', 
							'${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.CONTROLLER_URL%>/downloadreport/')
				}
				
				$scope.exportResult = function(reportUrl,reportStatusUrl,reportDownloadUrl) {
					
					$scope.exportAborted = false;
					
					$.ajax({
						type : "POST",
						url : reportUrl,
						data : "bean=" + encodeURIComponent(JSON.stringify($scope.object)),
						cache : false,
						success : function(data, textStatus, jqXHR) {
							$('#exportInProgress').modal({
							    backdrop: 'static',
							    keyboard: false
							});
							
							$scope.exportResultPooling(reportStatusUrl,reportDownloadUrl,data);
							
						},
						error : function(jqXHR, textStatus, errorThrown) {
							alert('<spring:message code="label.unexpected.error.occured" />');
						},
					});
				}
				
				$scope.exportResultPooling = function(reportStatusUrl,reportDownloadUrl,reportId) {

					$.ajax({
						url : reportStatusUrl + reportId,
						type : "GET",
						cache : false,
						success : function(data, textStatus, jqXHR) {
							if (data == 'true'){								
								$scope.hideProgressDialog();
								$scope.downloadResult(reportDownloadUrl, reportId);
							} else {
								if (!$scope.exportAborted) {
									$timeout(function() { 
										$scope.exportResultPooling(reportStatusUrl,reportDownloadUrl, reportId); 
										}, 3000);
								}
							}
						},
						error : function(jqXHR, textStatus, errorThrown) {
									alert('<spring:message code="label.unexpected.error.occured" />');
									$scope.hideProgressDialog();
								},
						});
				}
				
				
				
				
				$scope.hideProgressDialog = function() {
					$scope.exportAborted = true;
					$('#exportInProgress').modal('hide');
				}
				
				$scope.downloadResult = function(reportDownloadUrl, reportId) {
					window.location.href = reportDownloadUrl + reportId;
				}

		}]);
	
</script>




<form method="post" class="form-horizontal" id="searchParamsForm"
	name="form" ng-app="registrationHistoryReportApp"
	ng-controller="NewRegistrationHistoryReportController" novalidate>

	<input name="bean" type="hidden" value="{{ object }}" /> <input
		name="postback" type="hidden"
		value='${pageContext.request.contextPath}<%=NewRegistrationHistoryReportController.POSTBACK_URL%>' />
	<div class="panel panel-primary">
		<div class="panel-body">

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.executionYears" />
				</div>
				<div class="col-sm-6">
					<ui-select id="executionYearsSelect" name="executionYears"
						ng-model="$parent.object.executionYears" theme="bootstrap"
						multiple="true"> <ui-select-match>{{$item.text}}</ui-select-match>
					<ui-select-choices
						repeat="executionYear.id as executionYear in object.executionYearsDataSource | filter: $select.search">
					<span ng-bind-html="executionYear.text | highlight: $select.search"></span>
					</ui-select-choices> </ui-select>
				</div>
			</div>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.degreeTypes" />
				</div>
				<div class="col-sm-6">
					<ui-select id="degreeTypesSelect" name="degreeTypes"
						ng-model="$parent.object.degreeTypes" theme="bootstrap"
						on-select="onBeanChange($model,'degreeTypes')"
						on-remove="onBeanChange($model,'degreeTypes')" multiple="true">
					<ui-select-match>{{$item.text}}</ui-select-match> <ui-select-choices
						repeat="degreeType.id as degreeType in object.degreeTypesDataSource | filter: $select.search">
					<span ng-bind-html="degreeType.text | highlight: $select.search"></span>
					</ui-select-choices> </ui-select>
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.degrees" />
				</div>
				<div class="col-sm-6">
					<ui-select id="degreesSelect" name="degrees"
						ng-model="$parent.object.degrees" theme="bootstrap"
						multiple="true"> <ui-select-match>{{$item.text}}</ui-select-match>
					<ui-select-choices
						repeat="degree.id as degree in object.degreesDataSource | filter: $select.search">
					<span ng-bind-html="degree.text | highlight: $select.search"></span>
					</ui-select-choices> </ui-select>
				</div>
			</div>
			
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.withEnrolments" />
				</div>
				<div class="col-sm-2">
					<div class="col-sm-10" style="padding-left: 0;">
						<input type="checkbox" name="withEnrolments" ng-model="object.withEnrolments">
					</div>
				</div>

				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.firstTimeOnly" />
				</div>
				<div class="col-sm-2">
					<select id="firstTimeOnlySelect" name="firstTimeOnly"
						class="form-control" ng-model="object.firstTimeOnly"
						ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
						<option></option>
					</select>
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.improvementEnrolmentsOnly" />
				</div>
				<div class="col-sm-2">
					<select id="improvementEnrolmentsOnlySelect"
						name="improvementEnrolmentsOnly" class="form-control"
						ng-model="object.improvementEnrolmentsOnly"
						ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
						<option></option>
					</select>
				</div>

				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.dismissalsOnly" />
				</div>
				<div class="col-sm-2">
					<select id="dismissalsOnlySelect" name="dismissalsOnly"
						class="form-control" ng-model="object.dismissalsOnly"
						ng-options="bvalue.value as bvalue.name for bvalue in booleanvalues">
						<option></option>
					</select>
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.registrationStateTypes" />
				</div>
				<div class="col-sm-2">
					<ui-select id="registrationStateTypesSelect"
						name="registrationStateTypes"
						ng-model="$parent.object.registrationStateTypes" theme="bootstrap"
						multiple="true"> <ui-select-match>{{$item.text}}</ui-select-match>
					<ui-select-choices
						repeat="registrationStateType.id as registrationStateType in object.registrationStateTypesDataSource | filter: $select.search">
					<span
						ng-bind-html="registrationStateType.text | highlight: $select.search"></span>
					</ui-select-choices> </ui-select>
				</div>

				<div class="col-sm-1 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.regimeTypes" />
				</div>
				<div class="col-sm-3">
					<ui-select id="regimeTypesSelect" name="regimeTypes"
						ng-model="$parent.object.regimeTypes" theme="bootstrap"
						multiple="true"> <ui-select-match>{{$item.text}}</ui-select-match>
					<ui-select-choices
						repeat="regimeType.id as regimeType in object.regimeTypesDataSource | filter: $select.search">
					<span ng-bind-html="regimeType.text | highlight: $select.search"></span>
					</ui-select-choices> </ui-select>
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.statuteTypes" />
				</div>
				<div class="col-sm-2">
					<ui-select id="statuteTypesSelect" name="statuteTypes"
						ng-model="$parent.object.statuteTypes" theme="bootstrap"
						on-select="classback($model)" on-remove="callback($model)"
						multiple="true"> <ui-select-match>{{$item.text}}</ui-select-match>
					<ui-select-choices
						repeat="statuteType.id as statuteType in object.statuteTypesDataSource | filter: $select.search">
					<span ng-bind-html="statuteType.text | highlight: $select.search"></span>
					</ui-select-choices> </ui-select>
				</div>
				
				<div class="col-sm-1 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.ingressionTypes" />
				</div>
				<div class="col-sm-3">
					<ui-select id="ingressionTypesSelect" name="ingressionTypes"
						ng-model="$parent.object.ingressionTypes" theme="bootstrap"
						multiple="true"> <ui-select-match>{{$item.text}}</ui-select-match>
					<ui-select-choices
						repeat="ingressionType.id as ingressionType in object.ingressionTypesDataSource | filter: $select.search">
					<span
						ng-bind-html="ingressionType.text | highlight: $select.search"></span>
					</ui-select-choices> </ui-select>
				</div>
			</div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.registrationProtocols" />
				</div>
				<div class="col-sm-6">
					<ui-select id="registrationProtocolsSelect"
						name="registrationProtocols"
						ng-model="$parent.object.registrationProtocols" theme="bootstrap"
						multiple="true"> <ui-select-match>{{$item.text}}</ui-select-match>
					<ui-select-choices
						repeat="registrationProtocol.id as registrationProtocol in object.registrationProtocolsDataSource | filter: $select.search">
					<span
						ng-bind-html="registrationProtocol.text | highlight: $select.search"></span>
					</ui-select-choices> </ui-select>
				</div>
			</div>

	  	   	<div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.RegistrationHistoryReportParametersBean.programConclusions" />
                </div>
                <div class="col-sm-2">
                	<ui-select	id="programConclusionsSelect" name="programConclusions" ng-model="$parent.object.programConclusions" theme="bootstrap"  multiple="true">
						<ui-select-match>{{$item.text}}</ui-select-match> 
						<ui-select-choices	repeat="programConclusion.id as programConclusion in object.programConclusionsDataSource | filter: $select.search">
							<span ng-bind-html="programConclusion.text | highlight: $select.search"></span>
						</ui-select-choices> 
					</ui-select>
                </div>

				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.graduatedExecutionYears" />
				</div>
				<div class="col-sm-2">
					<ui-select id="executionYearsSelect" name="graduatedExecutionYears"
						ng-model="$parent.object.graduatedExecutionYears" theme="bootstrap"
						multiple="true"> <ui-select-match>{{$item.text}}</ui-select-match>
						<ui-select-choices
							repeat="executionYear.id as executionYear in object.executionYearsDataSource | filter: $select.search">
							<span ng-bind-html="executionYear.text | highlight: $select.search"></span>
						</ui-select-choices> 
					</ui-select>
				</div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message code="label.RegistrationHistoryReportParametersBean.graduationPeriodStartDate" />
                </div>
                <div class="col-sm-2">
                    <input class="form-control" type="text" bennu-date="object.graduationPeriodStartDate" />
                </div>

                <div class="col-sm-2 control-label">
                    <spring:message code="label.RegistrationHistoryReportParametersBean.graduationPeriodEndDate" />
                </div>
                <div class="col-sm-2">
                    <input class="form-control" type="text" bennu-date="object.graduationPeriodEndDate" />
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                        code="label.RegistrationHistoryReportParametersBean.studentNumber" />
                </div>
                <div class="col-sm-2">
                    <input id="studentNumber" class="form-control" type="number" ng-model="object.studentNumber" name="studentNumber" />
                </div>
            </div>

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.RegistrationHistoryReportParametersBean.exportWith" />
				</div>

				<div class="col-sm-1" style="padding-left: 0;">
					<input type="checkbox" name="exportExecutionYearData" ng-model="object.exportExecutionYearData">
					<spring:message code="label.RegistrationHistoryReportParametersBean.exportExecutionYearData" />
				</div>
				<div class="col-sm-1" style="padding-left: 0;">
					<input type="checkbox" name="exportConclusionData" ng-model="object.exportConclusionData">
					<spring:message code="label.RegistrationHistoryReportParametersBean.exportConclusionData" />
				</div>
				<div class="col-sm-1" style="padding-left: 0;">
					<input type="checkbox" name="exportQualificationAndOriginInfo" ng-model="object.exportQualificationAndOriginInfo">
					<spring:message code="label.RegistrationHistoryReportParametersBean.exportQualificationAndOriginInfo" />
				</div>
				<div class="col-sm-1" style="padding-left: 0;">
					<input type="checkbox" name="exportPersonalInfo" ng-model="object.exportPersonalInfo">
					<spring:message code="label.RegistrationHistoryReportParametersBean.exportPersonalInfo" />
				</div>
				<div class="col-sm-1" style="padding-left: 0;">
					<input type="checkbox" name="exportContacts" ng-model="object.exportContacts">
					<spring:message code="label.RegistrationHistoryReportParametersBean.exportContacts" />
				</div>
			</div>
		</div>

		<div class="panel-footer">
			<button type="button" class="btn btn-primary" ng-click="search()">
				<spring:message code="label.event.reports.registrationHistory.search" />
			</button>
            <button type="button" class="btn btn-primary"
                ng-click="exportRegistrations()">
                <spring:message code="label.event.reports.registrationHistory.exportRegistrations" />
            </button>
            <button type="button" class="btn btn-primary"
                ng-click="exportEnrolments()">
                <spring:message code="label.event.reports.registrationHistory.exportEnrolments" />
            </button>
            <button type="button" class="btn btn-primary"
                 ng-click="exportApprovals()">
                 <spring:message code="label.event.reports.registrationHistory.exportApprovals" />
             </button>
             <button type="button" class="btn btn-primary" 
             	 ng-click="exportBlueRecordData()">
                 <spring:message code="label.event.reports.registrationHistory.exportBlueRecordData" />
             </button>
             <button type="button" class="btn btn-primary" 
             	 ng-click="exportRegistrationsByStatute()">
                 <spring:message code="label.event.reports.registrationHistory.exportRegistrationsByStatute" />
             </button>

<%-- reconsider if more options are added
             <div class="btn-group">
                <button type="button" class=" btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    <span class="glyphicon glyphicon-export" aria-hidden="true"></span>&nbsp;
                    <spring:message code="label.event.reports.registrationHistory.export" />
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">
                   <li>
                        <a class="" href="#" ng-click="exportBlueRecordData()">
                            <span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span>
                            &nbsp;
                            <spring:message code="label.event.reports.registrationHistory.exportBlueRecordData" />
                        </a>
                        <a class="" href="#" ng-click="exportRegistrationsByStatute()">
                            <span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span>
                            &nbsp;
                            <spring:message code="label.event.reports.registrationHistory.exportRegistrationsByStatute" />
                        </a>
                    </li>
                </ul>
            </div>   
 --%>
		</div>
	</div>


	<div class="modal fade" id="exportInProgress">
		<div class="modal-dialog">
			<div class="modal-content">
				<form method="POST" action="target">
					<div class="modal-header">
						<h4 class="modal-title">
							<spring:message
								code="label.event.reports.registrationHistory.exportResult" />
						</h4>
					</div>
					<div class="modal-body">
						<p>
							<spring:message
								code="label.event.reports.registrationHistory.exportResult.in.progress" />
							<span class="glyphicon glyphicon-refresh spinning"></span>
						</p>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default"
							ng-click="hideProgressDialog()">
							<spring:message code="label.cancel" />
						</button>
					</div>
				</form>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal-dialog -->
	</div>
	<!-- /.modal -->

</form>


<c:if test="${fn:length(results) > 500}">
	<div class="alert alert-warning" role="alert">

		<p>
			<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
			<spring:message code="label.limitexceeded.use.export"
				arguments="500;${fn:length(results)}" argumentSeparator=";"
				htmlEscape="false" />
		</p>

	</div>
</c:if>


<table id="resultsTable" class="table table-bordered table-hover">
	<thead>
		<tr>
			<th><spring:message code="label.RegistrationHistoryReport.executionYear" /></th>
			<th><spring:message code="label.Student.number" /></th>
			<th><spring:message code="label.Registration.number" /></th>
			<th><spring:message code="label.Person.name" /></th>
			<th><spring:message code="label.Degree.code" /></th>
			<th><spring:message code="label.Degree.name" /></th>
			<th><spring:message code="label.Registration.ingressionType" /></th>
			<th><spring:message code="label.Registration.registrationProtocol" /></th>
			<th><spring:message code="label.RegistrationHistoryReport.lastRegistrationState" /></th>
			<th> </th>
	</tr>
	</thead>
	<tbody>
		<c:forEach var="result" items="${results}" varStatus="loop">
			<c:if test="${loop.index < 500}">
				<tr>
					<td><c:out value="${result.executionYear.qualifiedName}"></c:out></td>
					<td><c:out value="${result.registration.student.number}"></c:out></td>
					<td><c:out value="${result.registration.number}"></c:out></td>
					<td><c:out
							value="${result.registration.student.person.firstAndLastName}"></c:out></td>
					<td><c:out value="${result.registration.degree.code}"></c:out></td>
					<td><c:out
							value="${result.registration.degree.presentationName}"></c:out></td>
					<td><c:out
							value="${result.registration.ingressionType.description.content}"></c:out></td>
					<td><c:out
							value="${result.registration.registrationProtocol.description.content}"></c:out></td>
					<td><c:out value="${not empty result.lastRegistrationState ? result.lastRegistrationState.stateType.description : ''}"></c:out></td>
					<td>
						<a  class="btn btn-default btn-xs" href="${fr:checksumLink(pageContext.request,'/academicAdministration/student.do?method=visualizeRegistration&registrationID='.concat(result.registration.externalId))}">
							<spring:message code='label.view'/>
						</a>
					</td>
				</tr>
			</c:if>
		</c:forEach>
	</tbody>
</table>
<script type="text/javascript">
	createDataTables('resultsTable', true /*filterable*/,
			false /*show tools*/, true /*paging*/,
			"${pageContext.request.contextPath}", "${datatablesI18NUrl}");
</script>



