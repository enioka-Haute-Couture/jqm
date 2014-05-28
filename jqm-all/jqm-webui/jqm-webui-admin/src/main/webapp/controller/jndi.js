'use strict';

var jqmControllers = angular.module('jqmControllers');

jqmControllers.controller('µJndiListCtrl', function($scope, µJndiDto, jndiOracle, jndiFile, jndiUrl, jndiPs, jndiHsqlDb, jndiMySql, jndiMqQcf, jndiMqQ, jndiAmqQcf, jndiAmqQ,
        jndiGeneric, jndiOtherDb)
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

    $scope.newgeneric = function()
    {
        $scope.newResourceTemplate(jndiGeneric);
    };

    $scope.newResourceTemplate = function(template)
    {
        var r = {};
        angular.copy(template, r);
        $scope.resources.push(new µJndiDto(r));
    };

    $scope.save = function()
    {
        µQueueMappingDto.saveAll({}, $scope.mappings, $scope.refresh);
    };

    $scope.savealias = function()
    {
        console.debug($scope.selected[0]);
        µJndiDto.save({}, $scope.selected[0], $scope.refresh);
    };

    $scope.refresh = function()
    {
        console.debug('init');
        $scope.resources = µJndiDto.query();
    };

    $scope.removealias = function()
    {
        var q = $scope.selected[0];
        $scope.resources.splice($scope.resources.indexOf(q), 1);
        $scope.selected.splice($scope.resources.indexOf(q), 1);
        // $scope.selected2 = [];

    };

    $scope.removeprms = function()
    {
        var prm = null;
        console.debug('eee');
        for (var i = 0; i < $scope.selected2.length; i++)
        {
            prm = $scope.selected2[i];
            $scope.selected[0].parameters.splice($scope.selected[0].parameters.indexOf(prm), 1);
        }
    };

    $scope.addprm = function()
    {
        $scope.selected[0].parameters.push({
            'key' : 'name',
            'value' : 'value'
        });
    };

    $scope.gridOptions = {
        data : 'resources',
        enableCellSelection : true,
        enableRowSelection : true,
        enableCellEditOnFocus : true,
        multiSelect : false,
        showSelectionCheckbox : false,
        selectWithCheckboxOnly : false,
        selectedItems : $scope.selected,
        showGroupPanel : true,

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
            cellTemplate : '<div class="ngSelectionCell" ng-class="col.colIndex()"><input type="checkbox" ng-input="COL_FIELD" ng-model="COL_FIELD"/></div>',
        }, ]
    };

    $scope.gridOptions2 = {
        data : 'selected[0].parameters',
        enableCellSelection : true,
        enableRowSelection : true,
        enableCellEditOnFocus : true,
        multiSelect : true,
        showSelectionCheckbox : true,
        selectWithCheckboxOnly : true,
        selectedItems : $scope.selected2,

        columnDefs : [ {
            field : 'key',
            displayName : 'Resource parameter',
            width : 180,
        }, {
            field : 'value',
            displayName : 'Value',
        }, ]
    };

    $scope.refresh();
});
