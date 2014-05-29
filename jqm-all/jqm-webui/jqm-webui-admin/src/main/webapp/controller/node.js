'use strict';

var jqmControllers = angular.module('jqmControllers', [ 'jqmConstants', 'jqmServices' ]);

jqmControllers.controller('µNodeListCtrl', function($scope, $http, µNodeDto)
{
    $scope.nodes = µNodeDto.query();

    $scope.sortvar = 'jmxRegistryPort';
});

jqmControllers.controller('µNodeDetailCtrl', [ '$scope', '$routeParams', 'µNodeDto', function($scope, $routeParams, µNodeDto)
{
    $scope.nodeId = $routeParams.nodeId;
    $scope.node = µNodeDto.get({
        id : $routeParams.nodeId
    });
} ]);
