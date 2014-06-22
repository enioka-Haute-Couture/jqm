'use strict';

var jqmControllers = angular.module('jqmControllers');

jqmControllers.controller('µHistoryCtrl', function($scope, $http, $modal, µQueueDto)
{
    $scope.data = null;
    $scope.selected = [];
    $scope.query = {};
    $scope.queues = µQueueDto.query();

    $scope.refresh = function()
    {
        $scope.selected.length = 0;
        $scope.data = µHistoryDto.query();
    };

    $scope.getDataOk = function(data, status, headers, config)
    {
        $scope.data = data.instances;
        $scope.totalServerItems = data.resultSize;
    };

    $scope.getDataAsync = function()
    {
        // Paging options
        $scope.query.firstRow = ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize;
        $scope.query.pageSize = $scope.pagingOptions.pageSize;

        // Sort options
        $scope.query.sortby = [];
        var sl = $scope.sortInfo.columns.length;
        for ( var i = 0; i < sl; i++)
        {
            var col = $scope.sortInfo.columns[i];
            var way = ($scope.sortInfo.directions[i] === "desc" ? "DESCENDING" : "ASCENDING");
            if (col.colDef.sortField)
            {
                col = col.colDef.sortField;
            }
            else
            {
                col = col.field.toUpperCase();
            }
            $scope.query.sortby.push({
                col : col,
                order : way
            });
        }

        // Go
        $http.post("ws/client/ji/query", $scope.query).success($scope.getDataOk);
    };

    $scope.filterOptions = {
        filterText : "",
        useExternalFilter : true
    };

    $scope.pagingOptions = {
        pageSizes : [ 10, 15, 20, 30, 40, 50, 100 ],
        pageSize : 15,
        currentPage : 1
    };

    $scope.sortInfo = {
        fields : [ 'id', ],
        directions : [ 'desc' ],
        columns : [],
    };

    $scope.gridOptions = {
        data : 'data',
        enableCellSelection : false,
        enableRowSelection : true,
        enableCellEditOnFocus : false,
        multiSelect : false,
        selectedItems : $scope.selected,

        filterOptions : $scope.filterOptions,
        pagingOptions : $scope.pagingOptions,
        sortInfo : $scope.sortInfo,
        useExternalSorting : true,
        enablePaging : true,
        showFooter : true,
        totalServerItems : 'totalServerItems',

        columnDefs : [ {
            field : 'id',
            displayName : 'ID',
            width : '*',
        }, {
            field : 'applicationName',
            displayName : 'Application',
            width : '***',
        }, {
            field : 'queue.name',
            displayName : 'Queue',
            width : '**',
            sortField : 'QUEUENAME',
        }, {
            field : 'state',
            displayName : 'Status',
            width : '*',
            sortField : 'STATUS',
        }, {
            field : 'enqueueDate',
            displayName : 'Enqueued',
            cellFilter : 'date : "dd/MM HH:mm:ss"',
            width : '**',
            sortField : 'DATEENQUEUE',
        }, {
            field : 'beganRunningDate',
            displayName : 'Began',
            cellFilter : 'date : "dd/MM HH:mm:ss"',
            width : '**',
            sortField : 'DATEEXECUTION',
        }, {
            field : 'endDate',
            displayName : 'Ended',
            cellFilter : 'date : "dd/MM HH:mm:ss"',
            width : '**',
            sortField : 'DATEEND',
        }, {
            field : 'user',
            displayName : 'User',
            width : '**',
            sortField : 'USERNAME',
        }, {
            field : 'parent',
            displayName : 'Parent',
            width : '*',
            sortField : 'PARENTID',
        }, {
            field : 'progress',
            displayName : 'Progress',
            width : '*',
            sortable : false,
        }, {
            field : 'sessionID',
            displayName : 'Session ID',
            width : '*',
            sortable : false,
        }, ]
    };

    // Option modif => query again
    $scope.$watch('pagingOptions', function(newVal, oldVal)
    {
        if (newVal !== oldVal)
        {
            $scope.getDataAsync();
        }
    }, true);
    $scope.$watch('filterOptions', function(newVal, oldVal)
    {
        if (newVal !== oldVal)
        {
            $scope.getDataAsync();
        }
    }, true);
    $scope.$watch('sortInfo', function(newVal, oldVal)
    {
        if (newVal !== oldVal)
        {
            $scope.getDataAsync();
        }
    }, true);

    $scope.latest = function(hours)
    {
        $scope.query.enqueuedAfter = new Date();
        $scope.query.enqueuedAfter.setHours($scope.query.enqueuedAfter.getHours() - hours);
        $scope.getDataAsync();
    };

    $scope.showDetail = function()
    {
        $modal.open({
            templateUrl : './template/history_detail.html',
            controller : 'historyDetail',
            size : 'lg',

            resolve : {
                ji : function()
                {
                    return $scope.selected[0];
                }
            },
        });
    };

    $scope.relaunch = function()
    {
        var ji = $scope.selected[0];
        $http.post("ws/client/ji/" + ji.id).success($scope.getDataAsync);
    };

    // Init data
    $scope.getDataAsync();
});

jqmApp.controller('historyDetail', function($scope, $http, ji)
{
    $scope.ji = ji;
    $scope.dels = [];

    $scope.getdel = function()
    {
        $http.get("ws/client/ji/" + $scope.ji.id + "/files").success($scope.getdelOk);
    };

    $scope.getdelOk = function(data, status, headers, config)
    {
        $scope.dels = data;
        console.debug(data);
    };

    $scope.getdel();
});
