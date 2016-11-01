'use strict';

var jqmControllers = angular.module('jqmControllers');

jqmControllers.controller('µJndiListCtrl', function($scope, µJndiDto, jndiOracle, jndiFile, jndiUrl, jndiPs, jndiHsqlDb, jndiMySql, jndiMqQcf, jndiMqQ, jndiAmqQcf, jndiAmqQ,
        jndiGeneric, jndiOtherDb, jndiString, jndiMail, jqmCellTemplateBoolean, jqmCellEditorTemplateBoolean, uiGridEditConstants,uiGridConstants, $interval)
{
    $scope.resources = null;
    $scope.selected = [];
    $scope.selected2 = [];
    $scope.second = null;

    $scope.neworaclepool = function()
    {
        $scope.newResourceTemplate(jndiOracle);
    };

    $scope.newmysqlpool = function()
    {
        $scope.newResourceTemplate(jndiMySql);
    };

    $scope.newhsqldbpool = function()
    {
        $scope.newResourceTemplate(jndiHsqlDb);
    };

    $scope.newpspool = function()
    {
        $scope.newResourceTemplate(jndiPs);
    };

    $scope.newotherpool = function()
    {
        $scope.newResourceTemplate(jndiOtherDb);
    };

    $scope.newfile = function()
    {
        $scope.newResourceTemplate(jndiFile);
    };

    $scope.newurl = function()
    {
        $scope.newResourceTemplate(jndiUrl);
    };

    $scope.newmqqcf = function()
    {
        $scope.newResourceTemplate(jndiMqQcf);
    };

    $scope.newmqq = function()
    {
        $scope.newResourceTemplate(jndiMqQ);
    };

    $scope.newamqqcf = function()
    {
        $scope.newResourceTemplate(jndiAmqQcf);
    };

    $scope.newamqq = function()
    {
        $scope.newResourceTemplate(jndiAmqQ);
    };
    
    $scope.newstring = function()
    {
        $scope.newResourceTemplate(jndiString);
    };
    
    $scope.newmail = function()
    {
        $scope.newResourceTemplate(jndiMail);
    };

    $scope.newgeneric = function()
    {
        $scope.newResourceTemplate(jndiGeneric);
    };

    $scope.newResourceTemplate = function(template)
    {
        var r = {};
        angular.copy(template, r);
        var tmp = new µJndiDto(r);
        $scope.resources.push(tmp);
        $scope.gridApi.selection.selectRow(tmp);
        $interval(function() {
            $scope.gridApi.cellNav.scrollToFocus(tmp, $scope.gridOptions.columnDefs[0]);
        }, 0, 1);
    };

    $scope.save = function()
    {
        µQueueMappingDto.saveAll({}, $scope.mappings, $scope.refresh);
    };

    $scope.savealias = function()
    {
        µJndiDto.save({}, $scope.selected[0], $scope.refresh);
    };

    $scope.refresh = function()
    {
        $scope.resources = µJndiDto.query();
        $scope.selected.length = 0;
    };

    $scope.removealias = function()
    {
    	var q = $scope.selected[0];
    	
    	q.parameters.length = 0;
        
    	$scope.selected2.length = 0;
    	$interval(function() {
    		$scope.selected.length = 0;
        }, 0, 1);
        
        $scope.resources.splice($scope.resources.indexOf(q), 1);        
    };

    $scope.removeprms = function()
    {
        var prm = null;
        for (var i = 0; i < $scope.selected2.length; i++)
        {
            prm = $scope.selected2[i];
            $scope.selected[0].parameters.splice($scope.selected[0].parameters.indexOf(prm), 1);
        }
    };

    $scope.addprm = function()
    {
        var t = {
            'key' : 'name',
            'value' : 'value'
        };
        $scope.selected[0].parameters.push(t);
		$scope.gridApi2.selection.selectRow(t);
        $interval(function() {
            $scope.gridApi2.cellNav.scrollToFocus(t, $scope.gridOptions2.columnDefs[0]);
        }, 0, 1);
    };
    
    $scope.gridOptions = {
		data: 'resources',
		
		// TODO: re-enable grouping when stable in uigrid library.
		enableSelectAll : false,
		enableRowSelection : true,
		enableRowHeaderSelection : false,
		enableFullRowSelection : true,
		enableFooterTotalSelected : false,
		multiSelect : false,
		enableSelectionBatchEvent: false,
		noUnselect: true,
		
		enableColumnMenus : false,
		enableCellEditOnFocus : true,
		virtualizationThreshold : 20,
		enableHorizontalScrollbar : 0,

		onRegisterApi : function(gridApi) {
			$scope.gridApi = gridApi;
			gridApi.selection.on.rowSelectionChanged($scope, function(rows) {
				$scope.selected = gridApi.selection.getSelectedRows();
			});
			
			gridApi.cellNav.on.navigate($scope,function(newRowCol, oldRowCol){
	              if (newRowCol !== oldRowCol)
            	  {
	            	  gridApi.selection.selectRow(newRowCol.row.entity); //$scope.gridOptions.data[0]); // $scope.resources[0]
            	  }
            });
		},

        columnDefs : [ {
            field : 'name',
            displayName : 'JNDI alias',
            width : 150,
        }, {
            field : 'type',
            displayName : 'Type (class qualified name)',
        }, {
            field : 'factory',
            displayName : 'Factory',
        }, {
            field : 'description',
            displayName : 'Description',
        }, {
            field : 'singleton',
            displayName : 'S',
            width : 50,
            cellTemplate : jqmCellTemplateBoolean,
			editableCellTemplate : jqmCellEditorTemplateBoolean,
        }, ]
    };

    $scope.gridOptions2 = {
		data : 'selected[0].parameters',
		
		enableSelectAll : false,
		enableRowSelection : true,
		enableRowHeaderSelection : true,
		enableFullRowSelection : false,
		enableFooterTotalSelected : false,
		multiSelect : true,
		
		enableColumnMenus : false,
		enableCellEditOnFocus : true,
		virtualizationThreshold : 20,
		enableHorizontalScrollbar : 0,

		onRegisterApi : function(gridApi) {
			$scope.gridApi2 = gridApi;
			gridApi.selection.on.rowSelectionChanged($scope, function(rows) {
				$scope.selected2 = gridApi.selection.getSelectedRows();
			});
		},
		
        columnDefs : [ {
            field : 'key',
            displayName : 'Resource parameter',
            width : 160,
        }, {
            field : 'value',
            displayName : 'Value',
        }, ]
    };

    $scope.refresh();
});
