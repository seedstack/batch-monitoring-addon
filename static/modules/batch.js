/*
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
define([
        'module',
        'jquery',
        '{lodash}/lodash',
        '{angular}/angular',
        '[text]!{batch}/templates/showStatus.html',
        '{batch}/lib/ngGrid/ngGrid',
        '{d3}/d3',
        '{w20-ui}/modules/notifications',
        '{w20-dataviz}/modules/charts/discretebar',
        '{w20-dataviz}/modules/charts/pie',
        '{angular-resource}/angular-resource',
        '[css]!{batch}/style/style.css',
        '{batch}/lib/d3-layout/d3.layout'
    ],
    function (_module, $, _, angular, showStatusTemplate, ngGrid, d3) {
        'use strict';

        var _config = _module && _module.config() || {},
             POLLING = 5000, //ms
             module = angular.module('batch', [ 'ngResource', 'ngGrid', 'ngRoute' ]);

        module.factory('BatchMonitorService', ['$resource', function ($resource) {
            return {
                jobs: $resource(_config.seedBatchMonitoringWebRestPrefix + '/jobs'),
                jobExecution: $resource(_config.seedBatchMonitoringWebRestPrefix + '/jobs/:jobName/job-executions'),
                jobExecutionStep: $resource(_config.seedBatchMonitoringWebRestPrefix + '/jobs/executions/:jobExecutionId/steps'),
                stepDetails: $resource(_config.seedBatchMonitoringWebRestPrefix + '/jobs/executions/:jobExecutionId/steps/:stepExecutionId'),
                stepProgress: $resource(_config.seedBatchMonitoringWebRestPrefix + '/jobs/executions/:jobExecutionId/steps/:stepExecutionId/progress'),
                tree: $resource(_config.seedBatchMonitoringWebRestPrefix + '/jobs/jobs-tree/:jobName')
            };
        }]);

        module.factory('PollingService', function() {
           var _isPolling = false;
            return {
                set: function(bool) { _isPolling = bool; },
                isPolling: function() { return _isPolling; }
            };
        });

        module.controller('JobsListController',
            [ '$scope', 'BatchMonitorService', 'NotificationService', '$location', '$timeout', /*provided by ngGrid*/ '$domUtilityService', 'PollingService', 'AuthorizationService', 'AuthenticationService',
            function ($scope, batchMonitorService, notifier, $location, $timeout, $domUtilityService, pollingService, authorizationService, authenticationService) {

                $scope.authorization = authorizationService;
                $scope.authentication = authenticationService;
                
                function getJobs(callback) {
                    batchMonitorService.jobs.get({pageSize: $scope.pagingOptionsJob.pageSize, pageIndex: $scope.pagingOptionsJob.currentPage},
                        function (jobs) {
                                if (jobs && jobs.totalItems && jobs.results) {
                                    if (jobs.results.length) {
                                        $scope.jobsList = jobs.results;
                                        $scope.jobsTotalServerItems = jobs.totalItems;
                                        //if (!$scope.$$phase) { $scope.$apply(); }
                                        $timeout(function () {
                                            if (!$scope.isPolling) {
                                                $scope.jobsListOptions.selectRow(0, true);
                                            } else {
                                                if ($scope.selectedJob.length) {
                                                    $scope.jobsListOptions.selectRow($scope.selectedJobRowId, true);
                                                } else {
                                                    $scope.jobsListOptions.selectRow(0, true);
                                                }
                                            }
                                            // for pie chart data
                                            getJobExecutions($scope.selectedJob[0].name);

                                            if (callback && typeof callback === 'function') {
                                                callback();
                                            }
                                        });
                                    } else {
                                        notifier.alert('jobs list is empty');
                                    }
                                } else {
                                    notifier.warn('Job resource was correctly called but has empty results');
                                }
                        },
                        function (err) {
                            notifier.alert('An error occurred while retrieving batch jobs. Status : ' + err.status);
                        }
                    );
                }

                function getJobExecutions(jobName) {
                    if (jobName) {
                        batchMonitorService.jobExecution.get({jobName: jobName},
                            function (jobExecution) {
                                if (jobExecution && jobExecution.results && typeof jobExecution.results === 'object') {
                                    formatJobExecutionToNvd3(jobExecution.results);
                                }
                            },
                            function () {
                                notifier.alert('An error occured while retrieving job execution for job ' + $scope. selectedJob[0].name);
                                $scope.jobExecution = [];
                            }
                        );
                    } else {
                        throw new Error('No job name selected');
                    }
                }

                // format data for the pie chart
                function formatJobExecutionToNvd3(data) {
                    if (data) {
                        $scope.chartOverallProgressData = {completed: 0, unknown: 0, failed: 0};
                        angular.forEach(data, function (jobExec) {
                            if (jobExec && jobExec.exitStatus && jobExec.exitStatus.exitCode) {
                                switch (jobExec.exitStatus.exitCode) {
                                    case 'COMPLETED':
                                        $scope.chartOverallProgressData.completed++;
                                        break;
                                    case 'UNKNOWN':
                                        $scope.chartOverallProgressData.unknown++;
                                        break;
                                    case 'FAILED':
                                        $scope.chartOverallProgressData.failed++;
                                        break;
                                    default:
                                        break;
                                }
                            } else {
                                throw new Error('Data is malformed');
                            }
                        });
                        updateChart();
                    } else {
                        throw new Error('No data');
                    }
                }

                var stopPoll;
                function poll() {
                    $scope.$broadcast('UpdateEvent');
                    stopPoll = $timeout(function () {
                        poll();
                    }, POLLING);
                }

                // Configure a pie chart f(job execution) = status
                function updateChart() {
                    $scope.pieData = [
                        { key: 'Completed', value: $scope.chartOverallProgressData.completed },
                        { key: 'Unknown', value: $scope.chartOverallProgressData.unknown },
                        { key: 'Failed', value: $scope.chartOverallProgressData.failed }
                    ];

                    $scope.pieConfig = {
                        data: $scope.pieData,
                        donut: false,
                        color: ['#5CB85C', '#F5F5F5', '#FB5858'],
                        showLabels: true,
                        interactive: false,
                        pieLabelsOutside: false,
                        showValues: true,
                        tooltips: true,
                        labelType: 'percent',
                        showLegend: true
                    };
                }


                // when a job is selected, retrieve update on jobs execution for the pie chart
                $scope.$watch('selectedJob', function () {
                    if ($scope.selectedJob.length) {
                        getJobExecutions($scope.selectedJob[0].name);
                    }
                }, true);

                $scope.jobsList = [];
                $scope.selectedJob = [];
                $scope.jobsTotalServerItems = 0;
                $scope.chartOverallProgressData = {completed: 0, unknown: 0, failed: 0};
                $scope.pagingOptionsJob = {
                    pageSizes: [5, 10, 20],
                    pageSize: 5,
                    currentPage: 1
                };

                // Update when navigating through pages
                $scope.$watch('pagingOptionsJob', function (newVal, oldVal) {
                    if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage && newVal.currentPage) {
                        getJobs();
                    }
                }, true);

                // Jobs grid config (ngGrid)
                $scope.jobsListOptions = {
                    data: 'jobsList',
                    selectedItems: $scope.selectedJob,
                    multiSelect: false,
                    showFilter: true,
                    enableColumnResize: true,
                    keepLastSelected: true,
                    pagingOptions: $scope.pagingOptionsJob,
                    enablePaging: true,
                    showFooter: true,
                    totalServerItems: 'jobsTotalServerItems',
                    afterSelectionChange: function (rowItem) {
                        $scope.selectedJobRowId = rowItem.rowIndex;
                    },
                    columnDefs: [
                        {field: 'name', displayName: 'Name'},
                        {field: 'executionCount', displayName: 'Exec count'},
                        {field: 'incrementable', displayName: 'Incrementable'},
                        {field: 'executions', displayName: 'See executions',
                         cellTemplate: '<button type="button" class="btn btn-sm btn-full-width btn-default" data-ng-click="goToJobExecution(row)"> <i class="fa fa-list-alt"></i> </button>'}
                    ]
                };

                // NgGrid do not resize automatically, call this
                $scope.resizeGrids = function () {
                    $domUtilityService.RebuildGrid($scope.jobsListOptions.$gridScope, $scope.jobsListOptions.ngGrid);
                };

                $scope.goToJobExecution = function(row) {
                    if (row && row.entity && row.entity.name) {
                        $location.path('/batch/jobs-list/' + row.entity.name);
                    } else {
                        throw new Error('job row selected is malformed');
                    }
                };

                try {
                    $scope.showMap = true;
                    $scope.isPolling = pollingService.isPolling();
                    $scope.delta = Math.round(POLLING / 1000);


                    // Activate polling
                    $scope.polling = function () {
                        if ($scope.isPolling) {
                            $timeout.cancel(stopPoll);
                            $scope.isPolling = false;
                            pollingService.set($scope.isPolling);
                            return;
                        }
                        $scope.isPolling = true;
                        pollingService.set($scope.isPolling);
                        poll();
                    };

                    if ($scope.isPolling) {
                        getJobs(poll);
                    } else {
                        getJobs();
                    }

                    $scope.$on('UpdateEvent', function() {
                        if ($scope.selectedJob[0] && typeof $scope.selectedJob[0].name !== 'undefined') {
                            getJobExecutions($scope.selectedJob[0].name);
                        } else {
                            notifier.warn('No job selected');
                        }
                    });

                    $scope.$on('$destroy', function () {
                        $timeout.cancel(stopPoll);
                    });

                } catch(e) {
                    throw new Error('Could not get jobs list ' + e.message);
                }
            }]);

        module.controller('JobsInstanceListController',
            [ '$scope', 'BatchMonitorService', 'NotificationService', '$location', '$routeParams', '$timeout', /*NgGrid*/ '$domUtilityService', 'PollingService', 'AuthorizationService', 'AuthenticationService',
            function ($scope, batchMonitorService, notifier, $location, $routeParams, $timeout, $domUtilityService, pollingService, authorizationService, authenticationService) {

                $scope.authorization = authorizationService;
                $scope.authentication = authenticationService;

                function graphJobExecution(jobExecutions) {
                    if (jobExecutions) {
                        $scope.chartJobExecutionProgressData = [];
                        $scope.chartJobExecutionStatus = [];
                        jobExecutions.forEach(function (jobExecution) {
                            if (jobExecution && jobExecution.exitStatus && jobExecution.exitStatus.exitCode) {
                                // convert to second
                                var durationArray = jobExecution.duration.split(':');
                                var durationInSecond = Number(durationArray[0]) * 3600 + Number(durationArray[1]) * 60 + Number(durationArray[2]);
                                $scope.chartJobExecutionProgressData.push([jobExecution.id, durationInSecond]);
                            }
                            $scope.chartJobExecutionStatus.push(jobExecution.exitStatus.exitCode === 'COMPLETED' ? '#5cb85c' : '#fb2222');
                        });
                        $scope.discreteBarData = [
                            {key: 'Job execution', values: $scope.chartJobExecutionProgressData}
                        ];

                        $scope.discreteBarJobInstanceConfig = {
                            data: $scope.discreteBarData,
                            tooltips: true,
                            showValues: true,
                            staggerLabels: false,
                            color: $scope.chartJobExecutionStatus,
                            valueFormat: d3.format('.d'),
                            yAxisTickFormat: d3.format('.d')
                        };

                    } else {
                        throw new Error('No job executions');
                    }
                }

                function getJobExecutions(jobName) {
                    if (jobName) {
                        batchMonitorService.jobExecution.get({
                                pageSize: $scope.pagingOptionsJobExecution.pageSize,
                                pageIndex: $scope.pagingOptionsJobExecution.currentPage,
                                jobName: jobName},
                            function (jobExecution) {
                                if (jobExecution) {
                                    if (jobExecution.totalItems && jobExecution.results) {
                                        $scope.jobExecution = jobExecution.results;
                                        $scope.jobsExecutionTotalServerItems = jobExecution.totalItems;
                                        $timeout(function () {
                                            if (!$scope.isPolling) {
                                                $scope.jobExecutionOptions.selectRow(0, true);
                                            } else {
                                                if ($scope.selectedJobExecution.length) {
                                                    $scope.jobExecutionOptions.selectRow($scope.selectedJobExecutionRowId, true);
                                                } else {
                                                    $scope.jobExecutionOptions.selectRow(0, true);
                                                }
                                            }
                                           graphJobExecution($scope.jobExecution);
                                        });
                                    }
                                } else {
                                    throw new Error('Could not get any job executions');
                                }
                            },
                            function () {
                                notifier.alert('An error occured while retrieving job execution for job ' + $scope.selectedJob.name);
                                $scope.jobExecution = [];
                            }
                        );
                    } else {
                        throw new Error('No job name');
                    }
                }

                var stopPoll;
                function poll() {
                    $scope.$broadcast('UpdateEvent');
                    stopPoll = $timeout(function () {
                        poll();
                    }, POLLING);
                }

                $scope.jobExecution = [];
                $scope.selectedJobExecution = [];

                $scope.pagingOptionsJobExecution = {
                    pageSizes: [5, 10, 20],
                    pageSize: 10,
                    currentPage: 1
                };
                $scope.jobsExecutionTotalServerItems = 0;

                $scope.jobExecutionOptions = {
                    data: 'jobExecution',
                    selectedItems: $scope.selectedJobExecution,
                    multiSelect: false,
                    showFilter: true,
                    enableColumnResize: true,
                    pagingOptions: $scope.pagingOptionsJobExecution,
                    totalServerItems: 'jobsExecutionTotalServerItems',
                    enablePaging: true,
                    showFooter: true,
                    afterSelectionChange: function (rowItem) {
                        $scope.selectedJobExecutionRowId = rowItem.rowIndex;
                    },
                    columnDefs: [
                        {field: 'id', displayName: 'ID', width: 40},
                        {field: 'jobParameters', displayName: 'Job Parameters'},
                        {field: 'startDate', displayName: 'Start Date' },
                        {field: 'startTime', displayName: 'Start Time'},
                        {field: 'duration', displayName: 'Duration'},
                        {field: 'exitStatus.exitCode', displayName: 'Status',
                            cellTemplate:
                                '<div data-ng-class="{bold: true, completed: row.getProperty(col.field) === \'COMPLETED\', failed: row.getProperty(col.field) === \'FAILED\'}">' +
                                '<div class="ngCellText">{{row.getProperty(col.field)}}</div>' +
                                '</div>'},
                        {field: 'stepExecutionCount', displayName: 'Steps Count'},
                        {field: 'steps', displayName: 'See steps',
                            cellTemplate: '<button type="button" class="btn btn-sm btn-full-width btn-default" data-ng-click="goToSteps(row)"> <i class="fa fa-bar-chart-o"></i> </button>'}
                    ]
                };

                $scope.$watch('pagingOptionsJobExecution', function (newVal, oldVal) {
                    if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage && newVal.currentPage) {
                        getJobExecutions($scope.selectedJob);
                    }
                }, true);

                $scope.resizeGrids = function () {
                    $domUtilityService.RebuildGrid($scope.jobExecutionOptions.$gridScope, $scope.jobExecutionOptions.ngGrid);
                };

                try {
                    $scope.showMap = false;
                    $scope.selectedJob = $routeParams.jobName;
                    $scope.delta = Math.round(POLLING / 1000);
                    $scope.isPolling = pollingService.isPolling();

                    // Activate polling
                    $scope.polling = function () {
                        if ($scope.isPolling) {
                            $timeout.cancel(stopPoll);
                            $scope.isPolling = false;
                            pollingService.set($scope.isPolling);
                            return;
                        }
                        $scope.isPolling = true;
                        pollingService.set($scope.isPolling);
                        poll();
                    };

                    getJobExecutions($scope.selectedJob);
                    if ($scope.isPolling) {
                        poll();
                    }

                    $scope.$on('$destroy', function () {
                        $timeout.cancel(stopPoll);
                    });

                    $scope.$on('UpdateEvent', function() {
                        if (typeof $scope.selectedJob !== 'undefined') {
                            getJobExecutions($scope.selectedJob);
                        } else {
                            notifier.warn('No job selected');
                        }
                    });

                    $scope.goToSteps = function(row) {
                        if (row && row.entity) {
                            $location.path('/batch/jobs-list/' + $scope.selectedJob + '/' + row.entity.id);
                        } else {
                            throw new Error('job row selected is malformed');
                        }
                    };
                } catch(e) {
                    throw new Error('Could not get the associated job ' + e.message);
                }

            }
        ]);

        module.controller('StepDetailsController',
            [ '$scope', 'BatchMonitorService', 'NotificationService', '$routeParams', '$timeout', /*NgGrid*/ '$domUtilityService', 'PollingService', 'AuthorizationService', 'AuthenticationService',
            function ($scope, batchMonitorService, notifier, $routeParams, $timeout, $domUtilityService, pollingService, authorizationService, authenticationService) {

                $scope.authorization = authorizationService;
                $scope.authentication = authenticationService;

                $scope.steps = [];
                $scope.selectedStep = [];
                $scope.progressSteps = [];
                $scope.stepExecutionProgress = [];
                $scope.chartStepsProgressData = [];
                $scope.announcements = [
                    {icon: 'fa-book', text: 'Read count', val:''},
                    {icon: 'fa-pencil', text: 'Write count', val:''},
                    {icon: 'fa-check', text: 'Commit count', val:''},
                    {icon: 'fa-times', text: 'Rollback count', val:''}
                ];

                function getSteps(jobExecutionId) {
                    batchMonitorService.jobExecutionStep.query({jobExecutionId: jobExecutionId},
                        function (steps) {
                            if (steps) {
                                $scope.steps = steps;
                                $timeout(function () {
                                    if (!$scope.isPolling) {
                                        $scope.stepsOptions.selectRow(0, true);
                                    } else {
                                        if ($scope.selectedStep.length) {
                                            $scope.stepsOptions.selectRow($scope.selectedStepRowId, true);
                                        } else {
                                            $scope.stepsOptions.selectRow(0, true);
                                        }
                                    }
                                    graphSteps($scope.steps);
                                    getStepDetails(jobExecutionId, $scope.selectedStep[0].id);
                                    getHistory(jobExecutionId, $scope.selectedStep[0].id);
                                });

                            }
                        },
                        function () {
                            notifier.alert('An error occured while retrieving job steps for job ' + $scope.selectedJob.name);
                            $scope.steps = [];
                        }
                    );
                }

                function getStepDetails(jobExecutionId, stepExecutionId) {
                    batchMonitorService.stepDetails.get({jobExecutionId: jobExecutionId, stepExecutionId: stepExecutionId},
                        function (stepDetails) {
                            if (stepDetails && stepDetails.stepExecutionDetailsRepresentaion) {
                                $scope.progressSteps = formatDetailsStepExecution(stepDetails.stepExecutionDetailsRepresentaion);
                                updateAnnoucements($scope.progressSteps);
                            }
                        },
                        function (err) {
                            notifier.alert('An error occured while retrieving job steps details for job ' + err.message);
                            $scope.steps = [];
                        }
                    );

                }

                function formatStepHistory(stepExecutionHistory) {
                    var array = [],
                        currentRow;
                    for (var key in stepExecutionHistory) {
                        if (stepExecutionHistory.hasOwnProperty(key)) {
                            currentRow = stepExecutionHistory[key];
                            if ((key !== 'count') && (key !== 'stepName')) {
                                array.push({property: key,
                                    count: currentRow.count,
                                    min: currentRow.min,
                                    max: currentRow.max,
                                    mean: currentRow.mean,
                                    sigma: currentRow.standardDeviation});
                            }
                        }
                    }
                    return array;
                }

                function getHistory(jobExecutionId, stepExecutionId) {
                    batchMonitorService.stepProgress.get({jobExecutionId: jobExecutionId, stepExecutionId: stepExecutionId},
                        function (stepProgress) {
                            if (stepProgress) {
                                $scope.stepExecutionProgress = stepProgress;
                                if (stepProgress.stepExecutionHistory) {
                                    $scope.historySteps = formatStepHistory(stepProgress.stepExecutionHistory);
                                }
                            }
                        },
                        function () {
                            notifier.alert('An error occured while retrieving job steps progress for job');
                            $scope.steps = [];
                        }
                    );
                }


                function updateAnnoucements(arrayDetail) {
                    angular.forEach(arrayDetail, function(detail) {
                       if (detail.property === 'readCount') {
                           $scope.announcements[0].val = detail.value;
                       }
                       if (detail.property === 'writeCount') {
                            $scope.announcements[1].val = detail.value;
                       }
                       if (detail.property === 'commitCount') {
                            $scope.announcements[2].val = detail.value;
                       }
                       if (detail.property === 'rollbackCount') {
                            $scope.announcements[3].val = detail.value;
                       }
                       $scope.announcements = _.uniq($scope.announcements);
                    });
                }

                $scope.showFullDetails = function() {
                    angular.element('#showDetails').modal();
                };

                $scope.showHistory = function() {
                    angular.element('#showHistory').modal();
                };

                function graphSteps(steps) {
                    if (steps) {
                        $scope.chartStepsProgressData = [];
                        $scope.chartStepStatus = [];
                        steps.forEach(function (step) {
                            if (step && step.name) {
                                $scope.chartStepsProgressData.push([step.name, step.durationMillis]);
                            }
                            $scope.chartStepStatus.push(step.status === 'COMPLETED' ? '#5cb85c' : '#fb2222');
                        });
                        $scope.discreteBarData = [
                            {key: 'Batch steps', values: $scope.chartStepsProgressData}
                        ];
                        $scope.discreteBarConfig = {
                            data: $scope.discreteBarData,
                            tooltips: true,
                            showValues: true,
                            staggerLabels: true,
                            color: $scope.chartStepStatus,
                            valueFormat: d3.format('.d'),
                            yAxisTickFormat: d3.format('.d')
                        };
                    } else {
                        throw new Error('No steps');
                    }
                }

                function formatDetailsStepExecution(stepExecutionDetailsRepresentaion) {
                    var array = [];
                    for (var key in stepExecutionDetailsRepresentaion) {
                        if (stepExecutionDetailsRepresentaion.hasOwnProperty(key)) {
                            if ((key !== 'executionContext') && (key !== 'failureExceptions')) {
                                array.push({property: key, value: stepExecutionDetailsRepresentaion[key]});
                            }
                        }
                    }
                    return array;
                }

                var stopPoll;
                function poll() {
                    $scope.$broadcast('UpdateEvent');
                    stopPoll = $timeout(function () {
                        poll();
                    }, POLLING);
                }

                // Grid : steps
                $scope.stepsOptions = {
                    data: 'steps',
                    multiSelect: false,
                    selectedItems: $scope.selectedStep,
                    showFilter: true,
                    enableColumnResize: true,
                    afterSelectionChange: function (rowItem) {
                        $scope.selectedStepRowId = rowItem.rowIndex;
                    },
                    columnDefs: [
                        {field: 'id', displayName: 'ID', width: 40},
                        {field: 'jobExecutionId', displayName: 'job execution', width: 120},
                        {field: 'name', displayName: 'Step name', width: 220},
                        {field: 'status', displayName: 'Status', cellTemplate: '<div data-ng-class="{bold: true, completed: row.getProperty(col.field) === \'COMPLETED\', failed: row.getProperty(col.field) === \'FAILED\'}">' +
                            '<div class="ngCellText">{{row.getProperty(col.field)}}</div>' +
                            '</div>'
                        }

                    ]
                };

                $scope.resizeGrids = function () {
                    $domUtilityService.RebuildGrid($scope.stepsOptions.$gridScope, $scope.stepsOptions.ngGrid);
                };

                try {
                    $scope.showMap = false;
                    $scope.selectedJob = $routeParams.jobName;
                    $scope.selectedJobExecutionId = $routeParams.jobExecutionId;
                    $scope.delta = Math.round(POLLING / 1000);
                    $scope.isPolling = pollingService.isPolling();

                    // Activate polling
                    $scope.polling = function () {
                        if ($scope.isPolling) {
                            $timeout.cancel(stopPoll);
                            $scope.isPolling = false;
                            pollingService.set($scope.isPolling);
                            return;
                        }
                        $scope.isPolling = true;
                        pollingService.set($scope.isPolling);
                        poll();
                    };

                    getSteps($scope.selectedJobExecutionId);
                    if ($scope.isPolling) {
                        poll();
                    }

                    $scope.$on('$destroy', function () {
                        $timeout.cancel(stopPoll);
                    });

                    $scope.$on('UpdateEvent', function() {
                        if (typeof $scope.selectedJobExecutionId !== 'undefined') {
                            getSteps($scope.selectedJobExecutionId);
                        } else {
                            notifier.warn('No job selected');
                        }
                    });

                    $scope.$watch('selectedStep', function () {
                        if ($scope.selectedStep.length) {
                            getStepDetails($scope.selectedJobExecutionId, $scope.selectedStep[0].id);
                            getHistory($scope.selectedJobExecutionId, $scope.selectedStep[0].id);
                        }
                    }, true);

                } catch(e) {
                    throw new Error('Could not get the associated job execution ' + e.message);
                }
            }
        ]);

        return {
            angularModules: [ 'batch' ]
        };
    });