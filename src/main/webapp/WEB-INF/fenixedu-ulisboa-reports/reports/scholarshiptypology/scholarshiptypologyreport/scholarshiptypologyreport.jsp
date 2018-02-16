<%@page
	import="org.fenixedu.ulisboa.reports.ui.reports.scholarshiptypology.ScholarshipTypologyReportController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables"
	uri="http://github.com/dandelion/datatables"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<jsp:include page="../../../commons/angularInclude.jsp" />

<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message
			code="label.org.fenixedu.ulisboa.reports.scholarshipTypology.scholarshipTypologyReport" />
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


	angular.module('scholarshipTypologyReportApp',
			[ 'ngSanitize', 'ui.select', 'bennuToolkit' ]).controller(
			'ScholarshipTypologyReportController', [ '$scope','$timeout', '$http',

			function($scope,$timeout,$http) {
				
				$scope.booleanvalues = [ {
					name : '<spring:message code="label.org.fenixedu.ulisboa.reports.no"/>',
					value : false
				}, {
					name : '<spring:message code="label.org.fenixedu.ulisboa.reports.yes"/>',
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
						$('#searchParamsForm').attr('action', '${pageContext.request.contextPath}<%=ScholarshipTypologyReportController.CONTROLLER_URL%>/search')
						$('#searchParamsForm').submit();
					}
				}
				
				$scope.exportReport = function() {
					$scope.exportResult(
							'${pageContext.request.contextPath}<%=ScholarshipTypologyReportController.CONTROLLER_URL%>/exportreport', 
							'${pageContext.request.contextPath}<%=ScholarshipTypologyReportController.CONTROLLER_URL%>/exportstatus/', 
							'${pageContext.request.contextPath}<%=ScholarshipTypologyReportController.CONTROLLER_URL%>/downloadreport/')
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
							alert('<spring:message code="label.org.fenixedu.ulisboa.reports.unexpected.error.occured" />');
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
									alert('<spring:message code="label.org.fenixedu.ulisboa.reports.unexpected.error.occured" />');
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
	name="form" ng-app="scholarshipTypologyReportApp"
	ng-controller="ScholarshipTypologyReportController" novalidate>

	<input name="bean" type="hidden" value="{{ object }}" /> <input
		name="postback" type="hidden"
		value='${pageContext.request.contextPath}<%=ScholarshipTypologyReportController.POSTBACK_URL%>' />
	<div class="panel panel-primary">
		<div class="panel-body">

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.org.fenixedu.ulisboa.reports.ScholarshipTypologyReportParametersBean.executionYear" />
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
				<spring:message code="label.org.fenixedu.ulisboa.reports.scholarshipTypology.event.search" />
			</button>
            <button type="button" class="btn btn-primary"
                ng-click="exportReport()">
                <spring:message code="label.org.fenixedu.ulisboa.reports.scholarshipTypology.event.export" />
            </button>
		</div>
	</div>


	<div class="modal fade" id="exportInProgress">
		<div class="modal-dialog">
			<div class="modal-content">
				<form method="POST" action="target">
					<div class="modal-header">
						<h4 class="modal-title">
							<spring:message
								code="label.org.fenixedu.ulisboa.reports.scholarshipTypology.event.exportResult" />
						</h4>
					</div>
					<div class="modal-body">
						<p>
							<spring:message
								code="label.org.fenixedu.ulisboa.reports.scholarshipTypology.event.exportResult.in.progress" />
							<span class="glyphicon glyphicon-refresh spinning"></span>
						</p>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default"
							ng-click="hideProgressDialog()">
							<spring:message code="label.org.fenixedu.ulisboa.reports.cancel" />
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
			<spring:message code="label.org.fenixedu.ulisboa.reports.limitexceeded.use.export"
				arguments="500;${fn:length(results)}" argumentSeparator=";"
				htmlEscape="false" />
		</p>

	</div>
</c:if>


<table id="resultsTable" class="table table-bordered table-hover">
	<thead>
		<tr>
			<th><spring:message code="label.org.fenixedu.ulisboa.reports.ScholarshipTypologyReport.executionYear" /></th>
			<th><spring:message code="label.org.fenixedu.ulisboa.reports.ScholarshipTypologyReport.studentNumber" /></th>
			<th><spring:message code="label.org.fenixedu.ulisboa.reports.ScholarshipTypologyReport.personName" /></th>
			<th><spring:message code="label.org.fenixedu.ulisboa.reports.ScholarshipTypologyReport.degreeMinistryCode" /></th>
			<th><spring:message code="label.org.fenixedu.ulisboa.reports.ScholarshipTypologyReport.grantOwnerType" /></th>
			<th><spring:message code="label.org.fenixedu.ulisboa.reports.ScholarshipTypologyReport.grantOwnerProvider" /></th>
			<th> </th>
	</tr>
	</thead>
	<tbody>
		<c:forEach var="result" items="${results}" varStatus="loop">
			<c:if test="${loop.index < 500}">
				<tr>
					<td><c:out value="${result.executionYear.qualifiedName}"></c:out></td>
					<td><c:out value="${result.registration.student.number}"></c:out></td>
					<td><c:out value="${result.registration.student.person.firstAndLastName}"></c:out></td>
					<td><c:out value="${result.registration.degree.code}"></c:out></td>
					<td>
						<c:if test="${not empty result.grantOwnerType}">
							<spring:message code="label.org.fenixedu.ulisboa.reports.GrantOwnerType.${result.grantOwnerType}" />
						</c:if>
					</td>
					<td>
						<c:if test="${not empty result.grantOwnerProvider}">
							<c:out value="${result.grantOwnerProvider.nameI18n.content}"></c:out>
						</c:if>
					</td>
					<td>
						<a  class="btn btn-default btn-xs" href="${fr:checksumLink(pageContext.request,'/academicAdministration/student.do?method=visualizeRegistration&registrationID='.concat(result.registration.externalId))}">
							<spring:message code='label.org.fenixedu.ulisboa.reports.view'/>
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