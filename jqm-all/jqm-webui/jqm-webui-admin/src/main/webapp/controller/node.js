'use strict';

var jqmControllers = angular.module('jqmControllers', []);

jqmControllers.controller('µNodeListCtrl', function($scope, $http, µNodeService)
{
	$scope.nodes = µNodeService.query();

	$scope.sortvar = 'jmxRegistryPort';
});

jqmControllers.controller('µNodeDetailCtrl', [ '$scope', '$routeParams', 'µNodeService', function($scope, $routeParams, µNodeService)
{
	$scope.nodeId = $routeParams.nodeId;
	$scope.node = µNodeService.get(
	{
		nodeId : $routeParams.nodeId
	});
} ]);
