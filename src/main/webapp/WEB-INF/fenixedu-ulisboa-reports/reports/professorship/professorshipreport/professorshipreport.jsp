<%@page
	import="org.fenixedu.ulisboa.reports.ui.reports.professorship.ProfessorshipReportController"%>
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
			code="label.reports.professorship.professorshipReport" />
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


	angular.module('professorshipReportApp',
			[ 'ngSanitize', 'ui.select', 'bennuToolkit' ]).controller(
			'ProfessorshipReportController', [ '$scope','$timeout', '$http',

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
					$scope.postBack(model);
				}

				$scope.search = function() {
					
					if ($scope.object.executionYear !== null) {
						$('#searchParamsForm').attr('action', '${pageContext.request.contextPath}<%=ProfessorshipReportController.CONTROLLER_URL%>/search')
						$('#searchParamsForm').submit();
					}
				}
				
				$scope.exportReport = function() {
					$scope.exportResult(
							'${pageContext.request.contextPath}<%=ProfessorshipReportController.CONTROLLER_URL%>/exportreport', 
							'${pageContext.request.contextPath}<%=ProfessorshipReportController.CONTROLLER_URL%>/exportstatus/', 
							'${pageContext.request.contextPath}<%=ProfessorshipReportController.CONTROLLER_URL%>/downloadreport/')
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
	name="form" ng-app="professorshipReportApp"
	ng-controller="ProfessorshipReportController" novalidate>

	<input name="bean" type="hidden" value="{{ object }}" /> <input
		name="postback" type="hidden"
		value='${pageContext.request.contextPath}<%=ProfessorshipReportController.POSTBACK_URL%>' />
	<div class="panel panel-primary">
		<div class="panel-body">

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.ProfessorshipReportParametersBean.executionYear" />
				</div>
				<div class="col-sm-6">
					<select id="executionYearSelect" name="executionYear"
						class="form-control" ng-model="object.executionYear"
						ng-options="executionYear.id as executionYear.text for executionYear in object.executionYearsDataSource">
						<option></option>
					</select>
				</div>
			</div>
		</div>

		<div class="panel-footer">
			<button type="button" class="btn btn-primary" ng-click="search()">
				<spring:message code="label.event.reports.professorship.search" />
			</button>
            <button type="button" class="btn btn-primary"
                ng-click="exportReport()">
                <spring:message code="label.event.reports.professorship.export" />
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
								code="label.event.reports.professorship.exportResult" />
						</h4>
					</div>
					<div class="modal-body">
						<p>
							<spring:message
								code="label.event.reports.professorship.exportResult.in.progress" />
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
			<th><spring:message code="label.ProfessorshipReport.teacher" /></th>
			<th><spring:message code="label.ProfessorshipReport.teacherUsername" /></th>
			<th><spring:message code="label.ProfessorshipReport.executionYear" /></th>
			<th><spring:message code="label.ProfessorshipReport.executionSemester" /></th>
			<th><spring:message code="label.ProfessorshipReport.executionCourse" /></th>
			<!--<th><spring:message code="label.ProfessorshipReport.classes" /></th>-->
			<th><spring:message code="label.ProfessorshipReport.shift" /></th>
			<th><spring:message code="label.ProfessorshipReport.shiftType" /></th>
			<th><spring:message code="label.ProfessorshipReport.totalHours" /></th>
			<th><spring:message code="label.ProfessorshipReport.allocationPercentage" /></th>
			<th><spring:message code="label.ProfessorshipReport.workload" /></th>
	</tr>
	</thead>
	<tbody>
		<c:forEach var="result" items="${results}" varStatus="loop">
			<c:if test="${loop.index < 500}">
				<tr>
					<td>
						<c:if test="${not empty result.teacherName}">
							<c:out value="${result.teacherName}"></c:out>
						</c:if>
					</td>
					<td>
						<c:if test="${not empty result.teacherUsername}">
							<c:out value="${result.teacherUsername}"></c:out>
						</c:if>
					</td>
					<td>
						<c:if test="${not empty result.executionYearName}">
							<c:out value="${result.executionYearName}"></c:out>
						</c:if>
					</td>
					<td>
						<c:if test="${not empty result.executionSemesterName}">
							<c:out value="${result.executionSemesterName}"></c:out>
						</c:if>
					</td>
					<td>
						<c:if test="${not empty result.executionCourseName}">
							<c:out value="${result.executionCourseName}"></c:out>
						</c:if>
					</td>
					<!--<td>
						<c:if test="${not empty result.classesName}">
							<c:out value="${result.classesName}"></c:out>
						</c:if>
					</td>-->
					<td>
						<c:if test="${not empty result.shiftName}">
							<c:out value="${result.shiftName}"></c:out>
						</c:if>
					</td>
					<td>
						<c:if test="${not empty result.shiftTypeName}">
							<c:out value="${result.shiftTypeName}"></c:out>
						</c:if>
					</td>
					<td>
						<c:if test="${not empty result.totalHours}">
							<c:out value="${result.totalHours}"></c:out>
						</c:if>
					</td>
					<td>
						<c:if test="${not empty result.allocationPercentage}">
							<c:out value="${result.allocationPercentage}"></c:out>
						</c:if>
					</td>
					<td>
						<c:if test="${not empty result.teacherHours}">
							<c:out value="${result.teacherHours}"></c:out>
						</c:if>
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



