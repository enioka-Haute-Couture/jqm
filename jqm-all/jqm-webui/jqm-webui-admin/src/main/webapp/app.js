'use strict';

var jqmApp = angular.module('jqmApp', [ 'ngRoute', 'jqmControllers', 'jqmServices', 'ui.bootstrap', 'ngGrid' ]);

jqmApp.config([ '$routeProvider', function($routeProvider)
{
    $routeProvider.when('/node', {
        templateUrl : 'template/node_list.html',
        controller : 'µNodeListCtrl'
    }).when('/node/:nodeId', {
        templateUrl : 'template/node_detail.html',
        controller : 'µNodeDetailCtrl'
    }).when('/q', {
        templateUrl : 'template/q_list.html',
        controller : 'µQueueListCtrl'
    }).when('/qmapping', {
        templateUrl : 'template/mapping_list.html',
        controller : 'µQueueMappingListCtrl'
    }).when('/jndi', {
        templateUrl : 'template/jndi_list.html',
        controller : 'µJndiListCtrl'
    }).when('/prm', {
        templateUrl : 'template/prm_list.html',
        controller : 'µPrmListCtrl'
    }).when('/jd', {
        templateUrl : 'template/jd_list.html',
        controller : 'µJdListCtrl'
    }).otherwise({
        redirectTo : '/jd'
    });
} ]);

function TabsCtrl($scope, $location)
{
    $scope.tabs = [ {
        link : '#/node',
        label : 'Nodes'
    }, {
        link : '#/q',
        label : 'Queues'
    }, {
        link : '#/qmapping',
        label : 'Queue Mappings'
    }, {
        link : '#/jndi',
        label : 'Resources JNDI'
    }, {
        link : '#/prm',
        label : 'Cluster-wide parameters'
    }, {
        link : '#/jd',
        label : 'Job definitions'
    }, ];

    $scope.selectedTab = $scope.tabs[0];
    var i = 0;
    for (; i < $scope.tabs.length; i++)
    {
        if ($scope.tabs[i].link === "#" + $location.path())
        {
            $scope.selectedTab = $scope.tabs[i];
            break;
        }
    }

    $scope.setSelectedTab = function(tab)
    {
        $scope.selectedTab = tab;
    };

    $scope.tabClass = function(tab)
    {
        if ($scope.selectedTab == tab)
        {
            return "active";
        }
        else
        {
            return "";
        }
    };
}
