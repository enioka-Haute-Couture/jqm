'use strict';

var jqmControllers = angular.module('jqmControllers');

jqmControllers.controller('µQueueListCtrl', function($scope, $http, µQueueDto, jqmCellTemplateBoolean, jqmCellEditorTemplateBoolean, uiGridConstants, $interval) {
	$scope.queues = null;
	$scope.selected = [];

	$scope.newqueue = function() {
		var t = new µQueueDto({
			name : 'new queue',
			description : 'enter description',
			defaultQueue : false
		});
		$scope.queues.push(t);
		$scope.gridApi.selection.selectRow(t);
        $interval(function() {
            $scope.gridApi.cellNav.scrollToFocus(t, $scope.gridOptions.columnDefs[0]);
        }, 0, 1);
	};

	$scope.save = function() {
		// Save and refresh the table - ID may have been
		// generated by the server.
		µQueueDto.saveAll({}, $scope.queues, $scope.refresh);
	};

	$scope.refresh = function() {
		$scope.selected.length = 0;
		$scope.queues = µQueueDto.query();
	};

	$scope.remove = function() {
		var q = null;
		for (var i = 0; i < $scope.selected.length; i++) {
			q = $scope.selected[i];
			if (q.id !== null && q.id !== undefined) {
				q.$remove({
					id : q.id
				});
			}
			$scope.queues.splice($scope.queues.indexOf(q), 1);
		}
		$scope.selected.length = 0;
	};

	$scope.gridOptions = {
		data : 'queues',

		enableSelectAll : false,
		enableRowSelection : true,
		enableRowHeaderSelection : true,
		enableFullRowSelection : false,
		enableFooterTotalSelected : false,
		multiSelect : true,
		enableSelectionBatchEvent: false,
		noUnselect: false,
		
		enableColumnMenus : false,
		enableCellEditOnFocus : true,
		virtualizationThreshold : 20,
		enableHorizontalScrollbar : 0,

		onRegisterApi : function(gridApi) {
			$scope.gridApi = gridApi;
			gridApi.selection.on.rowSelectionChanged($scope, function(rows) {
				$scope.selected = gridApi.selection.getSelectedRows();
			});
			
			/*gridApi.cellNav.on.navigate($scope,function(newRowCol, oldRowCol){
				if (newRowCol !== oldRowCol)
				{
					gridApi.selection.selectRow(newRowCol.row.entity);
				}
            });*/
			
			$scope.gridApi.grid.registerRowsProcessor(createGlobalFilter($scope, [ 'name', 'description' ]), 200);
		},

		columnDefs : [ {
			field : 'name',
			displayName : 'Name',
			width : '**',
			sort : {
				direction : uiGridConstants.DESC,
				priority : 0
			},
		}, {
			field : 'description',
			displayName : 'Description',
			width : '*****',
		}, {
			field : 'defaultQueue',
			displayName : 'Is default',
			cellTemplate : jqmCellTemplateBoolean,
			editableCellTemplate : jqmCellEditorTemplateBoolean,
			width : '*',
		} ]
	};

	$scope.refresh();
});