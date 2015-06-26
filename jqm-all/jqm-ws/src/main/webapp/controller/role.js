'use strict';

var jqmControllers = angular.module('jqmControllers');

jqmControllers.controller('µRoleListCtrl', function($scope, $http, µRoleDto)
{
    $scope.roles = null;
    $scope.selected = [];

    $scope.newitem = function()
    {
        var t = new µRoleDto({
            description : 'what the role does',
            name : "rolename",
            permissions : [],
        });
        $scope.roles.push(t);
        $scope.role = t;
    };

    $scope.saveitem = function()
    {
        $scope.role.$save($scope.refresh);
    };

    $scope.refresh = function()
    {
        $scope.selected.length = 0;
        $scope.roles = µRoleDto.query($scope.refreshdone);
    };

    $scope.refreshdone = function()
    {
        if ($scope.roles.length > 0)
        {
            $scope.role = $scope.roles[0];
        }
        else
        {
            $scope.roles = null;
        }
    };

    $scope.deleteitem = function()
    {
        if ($scope.role == null)
        {
            return;
        }
        if ($scope.role.id != undefined)
        {
            $scope.role.$remove({id : $scope.role.id});
        }
        $scope.roles.splice($scope.roles.indexOf($scope.role), 1);
        $scope.role = null;
    };

    $scope.addperm = function()
    {
        $scope.role.permissions.push($scope.noun + ":" + $scope.verb);
        console.debug($scope.role);
    };

    $scope.removeperm = function()
    {
        if ($scope.role == null || $scope.perm == null)
        {
            return;
        }
        $scope.role.permissions.splice($scope.role.permissions.indexOf($scope.perm), 1);
        $scope.perm = null;
    };

    $scope.refresh();
});
