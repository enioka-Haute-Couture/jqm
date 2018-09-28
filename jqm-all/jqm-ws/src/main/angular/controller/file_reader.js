'use strict';

var jqmControllers = angular.module('jqmControllers');

jqmControllers.controller('fileReader', function($scope, $http, $uibModalInstance, url)
{
    $scope.url = url;
    $scope.data = null;

    $scope.getData = function()
    {
        $http.get($scope.url).success($scope.getDataOk).error($scope.getDataKo);
    };

    $scope.getDataOk = function(data, status, headers, config)
    {
        $scope.data = data;
    };
    
    $scope.getDataKo = function()
    {
    	$uibModalInstance.dismiss('cancel');
    };

    $scope.getData();
});
