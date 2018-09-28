'use strict';

var jqmControllers = angular.module('jqmControllers');

jqmControllers.filter('epoch2date', function() 
{
	return function(epochms)
	{
		return new Date(epochms);
	};
});

jqmControllers.controller('µHistoryCtrl', function($scope, $http, $uibModal, µQueueDto, epoch2dateFilter)
{
    $scope.data = null;
    $scope.selected = [];
    $scope.query = {};
    $scope.queues = µQueueDto.query();
    $scope.target = "hist";
    $scope.filterDate = false;
    
    // Default range is three hours from now
    $scope.now = (new Date()).getTime();
    $scope.daterangemin = $scope.now - 36000000;
    $scope.daterangemax = $scope.now;
    $scope.datemin = $scope.now - 3600000 * 3;
    $scope.datemax = $scope.now;
    $scope.step = 600000;
    $scope.scale = 86400000;

    $scope.refresh = function()
    {
        $scope.selected.length = 0;
        $scope.data = µHistoryDto.query();
    };

    $scope.getDataOk = function(data, status, headers, config)
    {
        $scope.data = data.instances;
        $scope.gridOptions.totalItems = data.resultSize;
        
        // Reset the time slider to avoid time drift.
        if ($scope.now < (new Date()).getTime() -30000)
    	{
	        $scope.now = (new Date()).getTime();
	        if ($scope.datemax === $scope.daterangemax)
	        	$scope.datemax = $scope.now;
	        $scope.daterangemax = $scope.now;
	        scale($scope.scale);
    	}
    };

    $scope.getDataAsync = function()
    {
        // Paging options
        $scope.query.firstRow = ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize;
        $scope.query.pageSize = $scope.pagingOptions.pageSize;

        // History or queues?
        if ($scope.target === "hist")
        {
            $scope.query.queryLiveInstances = false;
            $scope.query.queryHistoryInstances = true;
        }
        else
        {
            $scope.query.queryLiveInstances = true;
            $scope.query.queryHistoryInstances = false;
        }
        
        // KO only?
        if ($scope.target === "hist" && $scope.ko)
        {
            $scope.query.statuses = ['CRASHED', 'KILLED',];
        }
        else
        {
            $scope.query.statuses = [];
        }
        
        // Running only?
        if ($scope.target === "queues" && $scope.running)
        {
            $scope.query.statuses = ['RUNNING',];
        }

        // Sort options
        $scope.query.sortby = $scope.sortInfo;        
        
        // Time filters
        delete $scope.query.enqueuedBefore;
        delete $scope.query.enqueuedAfter;
        if ($scope.filterDate)
    	{
	        if ($scope.datemax !== $scope.daterangemax)
	    	{
	        	$scope.query.enqueuedBefore = new Date($scope.datemax);
	    	}
	        else
	    	{
	        	delete $scope.query.enqueuedBefore;
	    	}
	        $scope.query.enqueuedAfter = new Date($scope.datemin);
    	}        
        
        // Lists
        if ($scope.query.applicationName && $scope.query.applicationName.indexOf(',') > -1)
    	{
        	$scope.query.applicationName = $scope.query.applicationName.split(',');
    	}

        // Go
        $http.post("ws/client/ji/query", $scope.query).success($scope.getDataOk);
    };

    $scope.pagingOptions = {
        pageSize : 15,
        currentPage : 1
    };

    $scope.sortInfo = [
    	{
    		col: 'ID',
    		order: 'DESCENDING'
    	}
    ];

    $scope.gridOptions = {
		data : 'data',
		enableSelectAll : false,
		enableRowSelection : true,
		enableRowHeaderSelection : false,
		enableFullRowSelection : true,
		enableFooterTotalSelected : false,
		multiSelect : false,
		enableSelectionBatchEvent: false,
		noUnselect: true,

		onRegisterApi : function(gridApi) {
			$scope.gridApi = gridApi;
			gridApi.selection.on.rowSelectionChanged($scope, function(rows) {
				$scope.selected = gridApi.selection.getSelectedRows();
			});
			
			gridApi.core.on.sortChanged($scope, function(grid, sortColumns) {
				$scope.sortInfo.length = 0;
				$.each(sortColumns, function() {
					$scope.sortInfo.push({col: this.colDef.sortField, order: this.sort.direction === "desc" ? "DESCENDING" : "ASCENDING"});
				});
						        
		        $scope.getDataAsync();
	      });
			
	      gridApi.pagination.on.paginationChanged($scope, function (newPage, pageSize) {
	    	  $scope.pagingOptions.currentPage= newPage;
	    	  $scope.pagingOptions.pageSize = pageSize;
	        $scope.getDataAsync();
	      });
		},

		enableColumnMenus : false,
		enableCellEditOnFocus : false,
		virtualizationThreshold : 20,
		enableHorizontalScrollbar : 0,
		showFooter : true,
		
		paginationPageSizes: [ 10, 15, 20, 30, 40, 50, 100 ],
		paginationPageSize: 20,		
		useExternalPagination: true,
	    useExternalSorting: true,
		
        rowTemplate: '<div ng-dblclick="grid.appScope.showDetail(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.uid" ui-grid-one-bind-id-grid="rowRenderIndex + \'-\' + col.uid + \'-cell\'" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" role="{{col.isRowHeader ? \'rowheader\' : \'gridcell\'}}" ui-grid-cell> </div>',
	    
        columnDefs : [ {
            field : 'id',
            displayName : 'ID',
            width : '*',
            sortField: 'ID',
            sort: {direction: "desc"},
        }, {
            field : 'applicationName',
            displayName : 'Application',
            width : '***',
            sortField : 'APPLICATIONNAME',
        }, {
            field : 'queueName',
            displayName : 'Queue',
            width : '**',
            sortField : 'QUEUENAME',
        }, {
            field : 'state',
            displayName : 'Status',
            width : '*',
            sortField : 'STATUS',
            cellTemplate: '<div ng-class="{\'bg-success\': row.entity[col.field] == \'ENDED\', \
                                           \'bg-info\': row.entity[col.field] == \'RUNNING\', \
                                           \'bg-danger\': row.entity[col.field] == \'CRASHED\' || row.entity[col.field] == \'KILLED\' || row.entity[col.field] == \'CANCELLED\',\
                                           \'bg-warning\': row.entity[col.field] == \'SUBMITTED\' }"> \
                                           <div class="ui-grid-cell-contents">{{row.entity[col.field]}}</div></div>',
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
            enableSorting : false,
        }, {
            field : 'sessionID',
            displayName : 'Session ID',
            width : '*',
            enableSorting : false,
        }, ]
    };


    function scale(s)
    {
    	$scope.daterangemin = $scope.now - s;
    	if ($scope.datemax < $scope.daterangemin)
		{
    		$scope.datemax = $scope.daterangemax;
		}
    	if ($scope.datemin < $scope.daterangemin)
		{
    		$scope.datemin = $scope.daterangemin;
		}
    }
    $scope.$watch('scale', function(newVal, oldVal)
	{
    	scale(newVal);
	});
    $scope.$watch('[target, ko, running, filterOptions, filterDate]', function(newVal, oldVal)
    {
        if (newVal !== oldVal)
        {
            $scope.selected.length = 0;
            $scope.pagingOptions.currentPage = 1;
            $scope.getDataAsync();
        }
    }, true);
    $scope.$watch('datemin', function(newVal, oldVal)
    {
        // Different watch - this one is debounced manually, as the slider does not support ng-model-options
        setTimeout(function()
		{
        	if (newVal === $scope.datemin && $scope.filterDate)
    		{
        		// Has not changed in the past xx milliseconds => do the query.
        		$scope.selected.length = 0;
                $scope.pagingOptions.currentPage = 1;
                $scope.getDataAsync();
    		}
		}, 100);
    }, true);
    $scope.$watch('datemax', function(newVal, oldVal)
    {
        // Different watch - this one is debounced manually, as the slider does not support ng-model-options
        setTimeout(function()
		{
        	if (newVal === $scope.datemax && $scope.filterDate)
    		{
        		// Has not changed in the past xx milliseconds => do the query.
        		$scope.selected.length = 0;
                $scope.pagingOptions.currentPage = 1;
                $scope.getDataAsync();
    		}
		}, 100);
    }, true);

    $scope.showDetail = function()
    {
    	$uibModal.open({
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

    $scope.newLaunch = function()
    {
        var modalInstance = $uibModal.open({
            templateUrl : './template/new_launch.html',
            controller : 'jiNew',
            size : 'lg',
        });

        modalInstance.result.then(function()
        {
            $scope.target = "queues";
        }, function()
        {
            $scope.getDataAsync();
        });

    };

    // Buttons
    $scope.relaunch = function()
    {
        var ji = $scope.selected[0];
        $http.post("ws/client/ji/" + ji.id).success($scope.getDataAsync);
    };
    
    $scope.kill = function()
    {
        var ji = $scope.selected[0];
        $http.post("ws/client/ji/killed/" + ji.id).success($scope.getDataAsync);
        $scope.selected.length = 0;
    };
    
    $scope.changeQueue = function(newqueueid)
    {
        var ji = $scope.selected[0];
        $http.post("ws/client/q/" + newqueueid + "/" + ji.id).success($scope.getDataAsync);
    };
    
    $scope.pause = function()
    {
        var ji = $scope.selected[0];
        $http.post("ws/client/ji/paused/" + ji.id).success($scope.getDataAsync);
        $scope.selected.length = 0;
    };
    
    $scope.resume = function()
    {
        var ji = $scope.selected[0];
        $http.delete("ws/client/ji/paused/" + ji.id).success($scope.getDataAsync);
        $scope.selected.length = 0;
    };
    
    $scope.getDataAsync();
});

jqmApp.controller('historyDetail', function($scope, $http, $uibModal, ji)
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
    };
    
    $scope.showlog = function(url)
    {
    	$uibModal.open({
            templateUrl : './template/file_reader.html',
            controller : 'fileReader',
            size : 'lg',

            resolve : {
                url : function()
                {
                    return url;
                },
            },
        });
    };

    $scope.getdel();
});

jqmApp.controller('jiNew', function($scope, µUserJdDto, $uibModalInstance, $http)
{
    $scope.jds = µUserJdDto.query();
    $scope.selectedJd = null;
    
    $scope.data = {
        selectedJd : null,
        newKey : null,
        newValue : null
    };

    $scope.request = {
        user : 'webuser',
        sessionID : 0,
        parameters : [],
    };

    $scope.addPrm = function()
    {
        var np = {};
        np.key = $scope.data.newKey;
        np.value = $scope.data.newValue;
        $scope.request.parameters.push(np);
    };

    $scope.postOk = function()
    {
        $uibModalInstance.close();
    };

    $scope.ok = function()
    {
        $scope.request.applicationName = $scope.selectedJd.applicationName;
        $http.post("ws/client/ji", $scope.request).success($scope.postOk);
    };

    $scope.cancel = function()
    {
        $uibModalInstance.dismiss('cancel');
    };
});
