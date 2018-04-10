<%@page
        import="org.fenixedu.ulisboa.reports.ui.reports.competencecoursemarksheet.CompetenceCourseMarkSheetReportController" %>
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
                code="label.org.fenixedu.ulisboa.reports.competenceCourseMarkSheet.competenceCourseMarkSheetReport"/>
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

    angular.module('competenceCourseMarkSheetReportApp',
        ['ngSanitize', 'ui.select', 'bennuToolkit']).controller(
        'CompetenceCourseMarkSheetReportController', ['$scope', '$timeout', '$http',

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
                    $scope.postBack(model);
                }

                $scope.search = function () {

                    if ($scope.object.executionSemester !== null) {
                        $('#searchParamsForm').attr('action', '${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetReportController.CONTROLLER_URL%>/search')
                        $('#searchParamsForm').submit();
                    }
                }

                $scope.exportReport = function () {
                    $scope.exportResult(
                        '${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetReportController.CONTROLLER_URL%>/exportreport',
                        '${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetReportController.CONTROLLER_URL%>/exportstatus/',
                        '${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetReportController.CONTROLLER_URL%>/downloadreport/')
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
      name="form" ng-app="competenceCourseMarkSheetReportApp"
      ng-controller="CompetenceCourseMarkSheetReportController" novalidate>

    <input name="bean" type="hidden" value="{{ object }}"/> <input
        name="postback" type="hidden"
        value='${pageContext.request.contextPath}<%=CompetenceCourseMarkSheetReportController.POSTBACK_URL%>'/>
    <div class="panel panel-primary">
        <div class="panel-body">

            <div class="form-group row">
                <div class="col-sm-2 control-label">
                    <spring:message
                            code="label.org.fenixedu.ulisboa.reports.CompetenceCourseMarkSheetReport.executionSemester"/>
                </div>
                <div class="col-sm-6">
                    <select id="executionSemesterSelect" name="executionSemester"
                            class="form-control" ng-model="object.executionSemester"
                            ng-options="executionSemester.id as executionSemester.text for executionSemester in object.executionSemestersDataSource">
                        <option></option>
                    </select>
                </div>
            </div>
        </div>

        <div class="panel-footer">
            <button type="button" class="btn btn-primary"
                    ng-click="exportReport()">
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

</form>

