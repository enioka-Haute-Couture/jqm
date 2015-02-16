'use strict';

var jqmControllers = angular.module('jqmControllers');

jqmControllers.controller('fileReader', function($scope, $http, $modalInstance, url)
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
        $modalInstance.dismiss('cancel');
    };

    $scope.getData();
});
