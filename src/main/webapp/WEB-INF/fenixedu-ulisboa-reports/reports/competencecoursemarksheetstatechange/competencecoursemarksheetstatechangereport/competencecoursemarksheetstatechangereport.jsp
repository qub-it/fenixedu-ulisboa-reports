<%@page import="org.fenixedu.ulisboa.reports.services.report.competencecoursemarksheetstatechange.CompetenceCourseMarkSheetStateChangeReport"%>
<%@page import="org.fenixedu.ulisboa.specifications.dto.evaluation.markSheet.CompetenceCourseMarkSheetBean"%>
<%@page import="org.fenixedu.academic.domain.evaluation.markSheet.CompetenceCourseMarkSheetStateChange"%>
<%@page
        import="org.fenixedu.ulisboa.reports.ui.reports.competencecoursemarksheetstatechange.CompetenceCourseMarkSheetStateChangeReportController" %>
<%@page import="org.fenixedu.academic.domain.evaluation.season.EvaluationSeasonServices"%>        
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="datatables"
           uri="http://github.com/dandelion/datatables" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>

<jsp:include page="../../../commons/angularInclude.jsp"/>

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message
                code="label.org.fenixedu.ulisboa.reports.title.competenceCourseMarkSheetStateChange"/>
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
        from {
            transform: scale(1) rotate(0deg);
        }
        to {
            transform: scale(1) rotate(360deg);
        }
    }

    @-webkit-keyframes spin2 {
        from {
            -webkit-transform: rotate(0deg);
        }
        to {
            -webkit-transform: rotate(360deg);
        }
    }
</style>

<script type="text/javascript">

    angular.module('competenceCourseMarkSheetStateChangeReportApp',
        ['ngSanitize', 'ui.select', 'bennuToolkit']).controller(
        'CompetenceCourseMarkSheetStateChangeReportController', ['$scope', '$timeout', '$http',

            function ($scope, $timeout, $http) {

                $scope.booleanvalues = [{
                    name: '<spring:message code="label.org.fenixedu.ulisboa.reports.no"/>',
                    value: false
                }, {
                    name: '<spring:message code="label.org.fenixedu.ulisboa.reports.yes"/>',
                    value: true
                }];

                $scope.object = ${beanJson};
                $scope.form = {};
                $scope.form.object = $scope.object;

                $scope.postBack = createAngularPostbackFunction($scope);

                $scope.onBeanChange = function (model, field) {
                	$scope.object.competenceCourse = '';
                    $scope.postBack(model);
                }

                $scope.search = function () {

                    if ($scope.object.executionSemester !== null) {
                        $('#searchParamsForm').attr('action', '${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetStateChangeReportController.CONTROLLER_URL%>/search')
                        $('#searchParamsForm').submit();
                    }
                }

                $scope.exportReport = function () {
                    $scope.exportResult(
                        '${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetStateChangeReportController.CONTROLLER_URL%>/exportreport',
                        '${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetStateChangeReportController.CONTROLLER_URL%>/exportstatus/',
                        '${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetStateChangeReportController.CONTROLLER_URL%>/downloadreport/')
                }

                $scope.exportResult = function (reportUrl, reportStatusUrl, reportDownloadUrl) {

                    $scope.exportAborted = false;

                    $.ajax({
                        type: "POST",
                        url: reportUrl,
                        data: "bean=" + encodeURIComponent(JSON.stringify($scope.object)),
                        cache: false,
                        success: function (data, textStatus, jqXHR) {
                            $('#exportInProgress').modal({
                                backdrop: 'static',
                                keyboard: false
                            });

                            $scope.exportResultPooling(reportStatusUrl, reportDownloadUrl, data);

                        },
                        error: function (jqXHR, textStatus, errorThrown) {
                            alert('<spring:message code="label.org.fenixedu.ulisboa.reports.unexpected.error.occured" />');
                        },
                    });
                }

                $scope.exportResultPooling = function (reportStatusUrl, reportDownloadUrl, reportId) {

                    $.ajax({
                        url: reportStatusUrl + reportId,
                        type: "GET",
                        cache: false,
                        success: function (data, textStatus, jqXHR) {
                            if (data == 'true') {
                                $scope.hideProgressDialog();
                                $scope.downloadResult(reportDownloadUrl, reportId);
                            } else {
                                if (!$scope.exportAborted) {
                                    $timeout(function () {
                                        $scope.exportResultPooling(reportStatusUrl, reportDownloadUrl, reportId);
                                    }, 3000);
                                }
                            }
                        },
                        error: function (jqXHR, textStatus, errorThrown) {
                            alert('<spring:message code="label.org.fenixedu.ulisboa.reports.unexpected.error.occured" />');
                            $scope.hideProgressDialog();
                        },
                    });
                }

                $scope.hideProgressDialog = function () {
                    $scope.exportAborted = true;
                    $('#exportInProgress').modal('hide');
                }

                $scope.downloadResult = function (reportDownloadUrl, reportId) {
                    window.location.href = reportDownloadUrl + reportId;
                }

            }]);

</script>


<form method="post" class="form-horizontal" id="searchParamsForm"
      name="form" ng-app="competenceCourseMarkSheetStateChangeReportApp"
      ng-controller="CompetenceCourseMarkSheetStateChangeReportController" novalidate>

    <input name="bean" type="hidden" value="{{ object }}"/> <input
        name="postback" type="hidden"
        value='${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetStateChangeReportController.POSTBACK_URL%>'/>
    <div class="panel panel-primary">
        <div class="panel-body">

			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.org.fenixedu.ulisboa.reports.CompetenceCourseMarkSheetReport.executionSemester" />
				</div>

				<div class="col-sm-6">
					<ui-select	id="executionSemesterSelect" name="executionSemester" ng-model="$parent.object.executionSemester" theme="bootstrap" on-select="onBeanChange($model)" on-remove="onBeanChange($model)">
						<ui-select-match allow-clear="true">{{$select.selected.text}}</ui-select-match> 
						<ui-select-choices	repeat="executionSemester.id as executionSemester in object.executionSemesterDataSource | filter: $select.search">
							<span ng-bind-html="executionSemester.text | highlight: $select.search"></span>
						</ui-select-choices> 
					</ui-select>

				</div>
			</div>
			<div class="form-group row">
				<div class="col-sm-2 control-label">
					<spring:message code="label.CompetenceCourseMarkSheet.competenceCourse" />
				</div>
				<div class="col-sm-6">
					<ui-select	id="competenceCourseSelected" name="competenceCourse" ng-model="$parent.object.competenceCourse" theme="bootstrap">
						<ui-select-match allow-clear="true">{{$select.selected.text}}</ui-select-match> 
						<ui-select-choices	repeat="competenceCourse.id as competenceCourse in object.competenceCourseDataSource | filter: $select.search">
							<span ng-bind-html="competenceCourse.text | highlight: $select.search"></span>
						</ui-select-choices> 
					</ui-select>
	
				</div>
			</div>			
			
        </div>

        <div class="panel-footer">
        	<button type="button" class="btn btn-primary" ng-click="search()"><spring:message code="label.search" /></button>
            <button type="button" class="btn btn-primary" ng-click="exportReport()">
                <spring:message code="label.org.fenixedu.ulisboa.reports.course.event.export"/>
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
                                    code="label.org.fenixedu.ulisboa.reports.course.event.exportResult"/>
                        </h4>
                    </div>
                    <div class="modal-body">
                        <p>
                            <spring:message
                                    code="label.org.fenixedu.ulisboa.reports.course.event.exportResult.in.progress"/>
                            <span class="glyphicon glyphicon-refresh spinning"></span>
                        </p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default"
                                ng-click="hideProgressDialog()">
                            <spring:message code="label.org.fenixedu.ulisboa.reports.cancel"/>
                        </button>
                    </div>
                </form>
            </div>
            <!-- /.modal-content -->
        </div>
        <!-- /.modal-dialog -->
    </div>
    <!-- /.modal -->
	<c:choose>
		<c:when test="${not empty searchcompetencecoursemarksheetstatechangeResultsDataSet}">
			<spring:message code="label.yes" var="yesLabel"/>
			<spring:message code="label.no" var="noLabel"/>
		
			<table id="searchcompetencecoursemarksheetTable" class="table table-bordered table-hover" width="100%">
				<thead>
					<tr>
						<th><spring:message code="label.CompetenceCourseMarkSheet.creationDate" /></th>
<%-- 						<th><spring:message code="label.CompetenceCourseMarkSheet.competenceCourse" /></th> --%>
						<th><spring:message code="label.CompetenceCourseMarkSheet.evaluationSeason" /></th>
						<th><spring:message code="label.CompetenceCourseMarkSheet.evaluationDate" /></th>
						<th><spring:message code="label.CompetenceCourseMarkSheet.state" /></th>
<%-- 						<th><spring:message code="label.CompetenceCourseMarkSheet.certifier" /></th> --%>
<%-- 						<th><spring:message code="label.CompetenceCourseMarkSheet.shifts" /></th> --%>
						<th><spring:message code="label.CompetenceCourseMarkSheet.grades" /></th>
						<%-- Operations Column --%>
<!-- 						<th></th> -->
					</tr>
				</thead>
				<tbody>
					<c:forEach var="searchResult" items="${searchcompetencecoursemarksheetstatechangeResultsDataSet}">
					<tr>
						<td><joda:format value="${searchResult.creationDate}" pattern="yyyy-MM-dd HH:mm" /></td>
<%-- 						<td><c:out value="${searchResult.competenceCourse.code}" /> - <c:out value="${searchResult.competenceCourse.nameI18N.content}" /></td> --%>
						<td><c:out value="${searchResult.evaluationSeason.name.content}"></c:out></td>
<%-- 						<td><c:out value="<%=EvaluationSeasonServices.getDescriptionI18N(((CompetenceCourseMarkSheetStateChangeReport)pageContext.getAttribute("searchResult")).getEvaluationSeason()).getContent()%>"></c:out></td> --%>
						<td><c:out value="${searchResult.evaluationDatePresentation}"/></td>
						<td><c:out value='${searchResult.state}'/></td>
<%--                         <td><c:out value="<%=CompetenceCourseMarkSheetBean.getPersonDescription(((CompetenceCourseMarkSheetStateChangeReport)pageContext.getAttribute("searchResult")).getCertifier())%>"></c:out></td> --%>
<%-- 						<td><c:out value='${searchResult.shiftsDescription}'/></td> --%>
						<td><c:out value='${fn:length(searchResult.enrolmentEvaluationSet)}'/>
							<c:if test="${not empty searchResult.lastPendingChangeRequest}">
								<a title="<spring:message code="label.event.evaluation.manageMarkSheet.changeRequests" />" href="${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetStateChangeReportController.SEARCH_CHANGE_REQUESTS_URL%>${searchResult.externalId}">
									<span class="badge"><strong><c:out value="${fn:length(searchResult.pendingChangeRequests)}"/></strong></span>
								</a>
							</c:if>
						</td>
<%-- 						<td>
							<a  class="btn btn-default btn-xs" href="${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetStateChangeReportController.SEARCH_TO_VIEW_ACTION_URL%>${searchResult.externalId}"><spring:message code='label.view'/></a>
						</td> --%>
					</tr>
					</c:forEach>
				</tbody>
			</table>
			<script type="text/javascript">
				createDataTables('searchcompetencecoursemarksheetTable',true /*filterable*/, false /*show tools*/, true /*paging*/, "${pageContext.request.contextPath}","${datatablesI18NUrl}");
			</script>
					
		</c:when>
		
		<c:otherwise>
			<div class="alert alert-warning" role="alert">
	
				<p>
					<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
					<spring:message code="label.noResultsFound" />
				</p>
	
			</div>
	
		</c:otherwise>
	</c:choose>
</form>

